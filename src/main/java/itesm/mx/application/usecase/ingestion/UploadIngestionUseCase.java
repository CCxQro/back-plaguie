package itesm.mx.application.usecase.ingestion;

import itesm.mx.application.dto.ingestion.IngestionFileMessage;
import itesm.mx.domain.models.ingestion.IngestionFile;
import itesm.mx.domain.models.ingestion.IngestionRun;
import itesm.mx.domain.repository.ingestion.IngestionFileRepository;
import itesm.mx.domain.repository.ingestion.IngestionRunRepository;
import itesm.mx.infrastructure.ingestion.FileDetectionService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.logging.Logger;

import java.io.InputStream;
import java.time.LocalDateTime;

/**
 * EP-05 (upload variant): Accepts an uploaded CSV from the admin,
 * persists bookkeeping, and produces a Kafka message to topic ingestion.files.
 *
 * Replaces CheckIngestionUseCase (CKAN discovery path removed).
 */
@ApplicationScoped
public class UploadIngestionUseCase {

    private static final Logger LOG = Logger.getLogger(UploadIngestionUseCase.class);

<<<<<<< HEAD
    /** 300 MB upload cap enforced by the resource layer too; worker reads from local temp file. */
    public static final long MAX_UPLOAD_BYTES = 300L * 1024 * 1024;
=======
    /** 50 MB upload cap enforced by the resource layer too; worker reads from local temp file. */
    public static final long MAX_UPLOAD_BYTES = 50L * 1024 * 1024;
>>>>>>> origin/main

    @Inject
    FileDetectionService fileDetectionService;

    @Inject
    IngestionRunRepository ingestionRunRepository;

    @Inject
    IngestionFileRepository ingestionFileRepository;

    @Inject
<<<<<<< HEAD
    @Channel("ingestion-files")
=======
    @Channel("ingestion-files-out")
>>>>>>> origin/main
    Emitter<IngestionFileMessage> fileEmitter;

    /**
     * @param originalFilename  original name supplied by the browser
     * @param csvBytes          full bytes of the uploaded CSV (already size-validated by caller)
     * @param checksum          SHA-256 hex computed by caller from the same bytes
     * @param tempFilePath      absolute path to the persisted temp file (for the worker)
     * @return IngestionRun domain object for the caller to map to a DTO
     */
    @Transactional
    public IngestionRun execute(String originalFilename, byte[] csvBytes, String checksum, String tempFilePath) {
        // Dedup: if already ingested with same checksum, skip
        if (fileDetectionService.isAlreadyIngested(checksum)) {
            LOG.infof("CSV already ingested (checksum=%s), skipping", checksum);
            // Return a synthetic run indicating skip
            IngestionRun skipped = new IngestionRun();
            skipped.setStatus("SKIPPED");
            skipped.setFilesFound(0);
            skipped.setFilesProcessed(0);
            skipped.setStartedAt(LocalDateTime.now());
            skipped.setFinishedAt(LocalDateTime.now());
            return skipped;
        }

        // Create IngestionRun
        IngestionRun run = new IngestionRun();
        run.setStartedAt(LocalDateTime.now());
        run.setStatus("RUNNING");
        run.setFilesFound(1);
        run.setFilesProcessed(0);
        ingestionRunRepository.save(run);
        LOG.infof("IngestionRun %d started (upload, file=%s)", run.getId(), originalFilename);

        // Persist IngestionFile — reuse source_url column for original filename
        IngestionFile fileRecord = new IngestionFile();
        fileRecord.setRunId(run.getId());
        fileRecord.setSourceUrl(originalFilename);   // repurposed: stores original upload filename
        fileRecord.setFilename(originalFilename);
        fileRecord.setChecksum(checksum);
        fileRecord.setContentLength((long) csvBytes.length);
        fileRecord.setStatus("PENDING");
        fileRecord.setCreatedAt(LocalDateTime.now());
        ingestionFileRepository.save(fileRecord);

        // Produce Kafka message referencing the local temp file path
        IngestionFileMessage msg = new IngestionFileMessage(
                run.getId(), tempFilePath, originalFilename, null, checksum
        );
        fileEmitter.send(msg);

        run.setFilesProcessed(1);
        ingestionRunRepository.update(run);
        LOG.infof("IngestionRun %d: queued upload file %s (checksum=%s)", run.getId(), originalFilename, checksum);
        return run;
    }
}
