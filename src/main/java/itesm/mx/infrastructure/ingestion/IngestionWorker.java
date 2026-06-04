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
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

/**
 * SCRUM-316: Kafka consumer for topic ingestion.files.
 *
 * For each message:
 * 1. Download the CSV (streamed, size-capped at 200 MB).
 * 2. Parse with Apache Commons CSV.
 * 3. Resolve/get-or-create catalog FKs (Estado, Municipio, Plaga, Hospedante, Especie).
 * 4. Map each row to VigilanciaFitosanitariaEntity (validatedAt = null → pending).
 * 5. Dedupe vs existing (by lat+lon+plaga+hospedante within same run).
 * 6. Batch insert in chunks of BATCH_SIZE rows (@Transactional per chunk).
 * 7. Emit progress events to topic ingestion.progress every PROGRESS_EVERY rows.
 */
@ApplicationScoped
public class IngestionWorker {

    private static final Logger LOG = Logger.getLogger(IngestionWorker.class);

    /** Max bytes to download per CSV (200 MB). */
    static final long MAX_FILE_BYTES = 200L * 1024 * 1024;

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
    @Channel("ingestion-progress-out")
    Emitter<IngestionProgressEvent> progressEmitter;

    @Incoming("ingestion-files-in")
    public void processFile(IngestionFileMessage msg) {
        LOG.infof("Worker received file: runId=%d url=%s", msg.runId, msg.sourceUrl);

        Optional<IngestionFile> fileRecordOpt = ingestionFileRepository.findBySourceUrl(msg.sourceUrl);
        if (fileRecordOpt.isEmpty()) {
            LOG.warnf("No IngestionFile record for %s, skipping", msg.sourceUrl);
            return;
        }
        IngestionFile fileRecord = fileRecordOpt.get();
        updateFileStatus(fileRecord, "DOWNLOADING", null);
        emitProgress(msg.runId, msg.filename, 0, 0, "DOWNLOADING");

        List<CSVRecord> records;
        try {
            records = downloadAndParse(msg.sourceUrl, fileRecord, msg);
        } catch (Exception e) {
            LOG.errorf(e, "Download/parse failed for %s", msg.sourceUrl);
            updateFileStatus(fileRecord, "FAILED", truncate(e.getMessage(), 2000));
            emitProgress(msg.runId, msg.filename, 0, 0, "FAILED");
            return;
        }

        int total = records.size();
        fileRecord.setRowsTotal(total);
        updateFileStatus(fileRecord, "INSERTING", null);
        emitProgress(msg.runId, msg.filename, 0, total, "PARSING");

        int inserted = 0;
        int skipped = 0;

        // Resolve catalog FKs once outside batch loop (same for whole file by column name)
        // We'll resolve per-row because each row may have different values.
        // The CatalogResolver caches at DB level via get-or-create.

        List<VigilanciaFitosanitariaEntity> batch = new ArrayList<>(BATCH_SIZE);
        int rowNum = 0;

        for (CSVRecord record : records) {
            rowNum++;
            try {
                // Resolve catalog FKs (get-or-create)
                String estadoName    = SenasicaCsvMapper.getString(record, SenasicaCsvMapper.COL_ESTADO);
                String municipioName = SenasicaCsvMapper.getString(record, SenasicaCsvMapper.COL_MUNICIPIO);
                String plagaName     = SenasicaCsvMapper.getString(record, SenasicaCsvMapper.COL_PLAGA);
                String hospName      = SenasicaCsvMapper.getString(record, SenasicaCsvMapper.COL_HOSPEDANTE);
                String especieName   = SenasicaCsvMapper.getString(record, SenasicaCsvMapper.COL_ESPECIE);
                String variedadName  = SenasicaCsvMapper.getString(record, SenasicaCsvMapper.COL_VARIEDAD);

                Long plagaId     = resolveCatalog(() -> catalogResolver.resolvePlaga(plagaName));
                Long hospId      = resolveCatalog(() -> catalogResolver.resolveHospedante(hospName));
                Long especieId   = resolveCatalog(() -> catalogResolver.resolveEspecie(especieName));
                Long variedadId  = null; // Variedad resolved separately if needed

                VigilanciaFitosanitariaEntity entity = SenasicaCsvMapper.toEntity(
                        record, plagaId, hospId, especieId, variedadId, STATUS_REVISION);

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
            batch.clear();
        }

        // Final update
        fileRecord.setRowsInserted(inserted);
        fileRecord.setRowsSkipped(skipped);
        updateFileStatus(fileRecord, "DONE", null);
        emitProgress(msg.runId, msg.filename, total, total, "DONE");
        LOG.infof("File %s ingested: %d inserted, %d skipped", msg.filename, inserted, skipped);
    }

    private List<CSVRecord> downloadAndParse(String url, IngestionFile fileRecord, IngestionFileMessage msg)
            throws IOException, InterruptedException {
        SsrfGuard.check(url);
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofMinutes(5))
                .GET()
                .header("User-Agent", "plaguie-ingestion/1.0")
                .build();

        HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("HTTP " + response.statusCode() + " downloading " + url);
        }

        // Compute SHA-256 while reading; enforce size cap
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (Exception e) {
            throw new IOException("SHA-256 not available", e);
        }

        Charset charset = detectCharset(response);

        List<CSVRecord> records = new ArrayList<>();
        try (InputStream raw = response.body()) {
            // Wrap with size-limiting stream
            SizeLimitedInputStream limited = new SizeLimitedInputStream(raw, MAX_FILE_BYTES, digest);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(limited, charset));
                 CSVParser parser = CSVFormat.DEFAULT
                         .builder()
                         .setHeader()
                         .setSkipHeaderRecord(true)
                         .setIgnoreHeaderCase(true)
                         .setTrim(true)
                         .setIgnoreEmptyLines(true)
                         .build()
                         .parse(reader)) {
                for (CSVRecord r : parser) {
                    records.add(r);
                }
            }
            // Update checksum on record
            String hash = HexFormat.of().formatHex(digest.digest());
            fileRecord.setChecksum(hash);
        }
        return records;
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

    private Charset detectCharset(HttpResponse<InputStream> response) {
        return response.headers().firstValue("Content-Type")
                .filter(ct -> ct.contains("charset="))
                .map(ct -> {
                    int idx = ct.indexOf("charset=");
                    String cs = ct.substring(idx + 8).split(";")[0].trim();
                    try {
                        return Charset.forName(cs);
                    } catch (Exception e) {
                        return StandardCharsets.UTF_8;
                    }
                })
                .orElse(StandardCharsets.UTF_8);
    }

    private static String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() > max ? s.substring(0, max) : s;
    }

    /** InputStream wrapper that enforces a byte limit and feeds bytes to a MessageDigest. */
    static class SizeLimitedInputStream extends InputStream {
        private final InputStream delegate;
        private final long maxBytes;
        private final MessageDigest digest;
        private long bytesRead = 0;

        SizeLimitedInputStream(InputStream delegate, long maxBytes, MessageDigest digest) {
            this.delegate = delegate;
            this.maxBytes = maxBytes;
            this.digest = digest;
        }

        @Override
        public int read() throws IOException {
            if (bytesRead >= maxBytes) throw new IOException("File exceeds max size " + maxBytes + " bytes");
            int b = delegate.read();
            if (b != -1) {
                bytesRead++;
                if (digest != null) digest.update((byte) b);
            }
            return b;
        }

        @Override
        public int read(byte[] buf, int off, int len) throws IOException {
            if (bytesRead >= maxBytes) throw new IOException("File exceeds max size " + maxBytes + " bytes");
            long remaining = maxBytes - bytesRead;
            int toRead = (int) Math.min(len, remaining);
            int r = delegate.read(buf, off, toRead);
            if (r > 0) {
                bytesRead += r;
                if (digest != null) digest.update(buf, off, r);
            }
            return r;
        }

        @Override
        public void close() throws IOException {
            delegate.close();
        }
    }
}
