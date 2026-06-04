package itesm.mx.infrastructure.ingestion;

import itesm.mx.application.dto.ingestion.IngestionFileMessage;
import itesm.mx.application.dto.ingestion.IngestionProgressEvent;
import itesm.mx.domain.models.ingestion.IngestionFile;
import itesm.mx.domain.repository.ingestion.IngestionFileRepository;
import itesm.mx.domain.repository.ingestion.IngestionRunRepository;
import itesm.mx.infrastructure.persistence.entity.vigilancia.VigilanciaFitosanitariaEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * SCRUM-316 (refactored EP-05): In-memory consumer for topic ingestion.files.
 *
 * For each message:
 * 1. Read the CSV from the local temp file path (no HTTP download).
 * 2. Parse with Apache Commons CSV.
 * 3. Resolve/get-or-create catalog FKs (Plaga, Hospedante, Especie).
 * 4. Map each row to VigilanciaFitosanitariaEntity (validatedAt=null → pending).
 * 5. Batch insert in chunks of BATCH_SIZE rows (@Transactional per chunk).
 * 6. Emit progress events to topic ingestion.progress every PROGRESS_EVERY rows.
 * 7. Delete the temp file when done (or on failure).
 */
@ApplicationScoped
public class IngestionWorker {

    private static final Logger LOG = Logger.getLogger(IngestionWorker.class);

    /** Batch size for chunked @Transactional inserts. */
    static final int BATCH_SIZE = 500;

    /** Emit progress event every N rows. */
    static final int PROGRESS_EVERY = 100;

    // Status ID for "Revisión" (pending admin validation) — matches existing seed data
    static final Long STATUS_REVISION = 2L;

    @Inject
    EntityManager em;

    @Inject
    IngestionFileRepository ingestionFileRepository;

    @Inject
    IngestionRunRepository ingestionRunRepository;

    @Inject
    CatalogResolver catalogResolver;

    @Inject
    @Channel("ingestion-progress")
    @io.smallrye.reactive.messaging.annotations.Broadcast
    Emitter<IngestionProgressEvent> progressEmitter;

    @Incoming("ingestion-files")
    public void processFile(IngestionFileMessage msg) {
        LOG.infof("Worker received file: runId=%d path=%s", msg.runId, msg.sourceUrl);

        // msg.sourceUrl now holds the local temp file path (repurposed field)
        String tempFilePath = msg.sourceUrl;

        Optional<IngestionFile> fileRecordOpt = ingestionFileRepository.findBySourceUrl(msg.filename);
        if (fileRecordOpt.isEmpty()) {
            // Fallback: look up by checksum
            fileRecordOpt = msg.checksum != null
                    ? ingestionFileRepository.findByChecksum(msg.checksum)
                    : Optional.empty();
        }
        if (fileRecordOpt.isEmpty()) {
            LOG.warnf("No IngestionFile record for filename=%s, skipping", msg.filename);
            deleteTempFile(tempFilePath);
            return;
        }
        IngestionFile fileRecord = fileRecordOpt.get();
        updateFileStatus(fileRecord, "PARSING", null);
        emitProgress(msg.runId, msg.filename, 0, 0, "PARSING");

        List<CSVRecord> records;
        try {
            records = readAndParse(tempFilePath);
        } catch (Exception e) {
            LOG.errorf(e, "Parse failed for %s", tempFilePath);
            updateFileStatus(fileRecord, "FAILED", truncate(e.getMessage(), 2000));
            emitProgress(msg.runId, msg.filename, 0, 0, "FAILED");
            deleteTempFile(tempFilePath);
            return;
        }

        int total = records.size();
        fileRecord.setRowsTotal(total);
        updateFileStatus(fileRecord, "INSERTING", null);
        emitProgress(msg.runId, msg.filename, 0, total, "INSERTING");

        int inserted = 0;
        int skipped = 0;
        List<VigilanciaFitosanitariaEntity> batch = new ArrayList<>(BATCH_SIZE);
        int rowNum = 0;

        for (CSVRecord record : records) {
            rowNum++;
            try {
                String plagaName    = SenasicaCsvMapper.getString(record, SenasicaCsvMapper.COL_PLAGA);
                String hospName     = SenasicaCsvMapper.getString(record, SenasicaCsvMapper.COL_HOSPEDANTE);
                String especieName  = SenasicaCsvMapper.getString(record, SenasicaCsvMapper.COL_ESPECIE);

                Long plagaId    = resolveCatalog(() -> catalogResolver.resolvePlaga(plagaName));
                Long hospId     = resolveCatalog(() -> catalogResolver.resolveHospedante(hospName));
                Long especieId  = resolveCatalog(() -> catalogResolver.resolveEspecie(especieName));

                VigilanciaFitosanitariaEntity entity = SenasicaCsvMapper.toEntity(
                        record, plagaId, hospId, especieId, null, STATUS_REVISION);

                if (entity == null) {
                    skipped++;
                    continue;
                }
                batch.add(entity);
            } catch (Exception e) {
                LOG.warnf("Skipping row %d: %s", rowNum, e.getMessage());
                skipped++;
            }

            if (batch.size() >= BATCH_SIZE) {
                int batchInserted = persistBatch(batch);
                inserted += batchInserted;
                skipped += (batch.size() - batchInserted);
                batch.clear();
            }

            if (rowNum % PROGRESS_EVERY == 0) {
                emitProgress(msg.runId, msg.filename, rowNum, total, "INSERTING");
            }
        }

        // Flush remaining batch
        if (!batch.isEmpty()) {
            int batchInserted = persistBatch(batch);
            inserted += batchInserted;
            skipped += (batch.size() - batchInserted);
        }

        fileRecord.setRowsInserted(inserted);
        fileRecord.setRowsSkipped(skipped);
        updateFileStatus(fileRecord, "DONE", null);
        emitProgress(msg.runId, msg.filename, total, total, "DONE");
        LOG.infof("File %s ingested: %d inserted, %d skipped", msg.filename, inserted, skipped);

        deleteTempFile(tempFilePath);
    }

    private List<CSVRecord> readAndParse(String filePath) throws IOException {
        Path path = Path.of(filePath).normalize().toAbsolutePath();
        try (InputStream is = Files.newInputStream(path);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
             CSVParser parser = CSVFormat.DEFAULT
                     .builder()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .setIgnoreHeaderCase(true)
                     .setTrim(true)
                     .setIgnoreEmptyLines(true)
                     .build()
                     .parse(reader)) {
            return parser.getRecords();
        }
    }

    @Transactional
    int persistBatch(List<VigilanciaFitosanitariaEntity> batch) {
        int count = 0;
        for (VigilanciaFitosanitariaEntity entity : batch) {
            try {
                em.persist(entity);
                count++;
            } catch (Exception e) {
                LOG.warnf("Failed to persist entity: %s", e.getMessage());
            }
        }
        em.flush();
        em.clear();
        return count;
    }

    @Transactional
    void updateFileStatus(IngestionFile fileRecord, String status, String error) {
        fileRecord.setStatus(status);
        fileRecord.setError(error);
        try {
            ingestionFileRepository.update(fileRecord);
        } catch (Exception e) {
            LOG.warnf("Could not update file status: %s", e.getMessage());
        }
    }

    private void emitProgress(Long runId, String filename, int processed, int total, String status) {
        try {
            progressEmitter.send(new IngestionProgressEvent(runId, filename, processed, total, status));
        } catch (Exception e) {
            LOG.debugf("Progress emit failed (non-critical): %s", e.getMessage());
        }
    }

    @FunctionalInterface
    interface CatalogAction {
        Long resolve();
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    Long resolveCatalog(CatalogAction action) {
        return action.resolve();
    }

    private void deleteTempFile(String filePath) {
        try {
            Path path = Path.of(filePath).normalize().toAbsolutePath();
            Files.deleteIfExists(path);
            LOG.debugf("Deleted temp file: %s", path);
        } catch (Exception e) {
            LOG.warnf("Could not delete temp file %s: %s", filePath, e.getMessage());
        }
    }

    private static String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() > max ? s.substring(0, max) : s;
    }
}
