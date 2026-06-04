package itesm.mx.application.usecase.ingestion;

import itesm.mx.application.dto.ingestion.IngestionFileMessage;
import itesm.mx.domain.models.ingestion.DiscoveredFile;
import itesm.mx.domain.models.ingestion.IngestionFile;
import itesm.mx.domain.models.ingestion.IngestionRun;
import itesm.mx.domain.repository.ingestion.IngestionFileRepository;
import itesm.mx.domain.repository.ingestion.IngestionRunRepository;
import itesm.mx.infrastructure.ingestion.DiscoveryService;
import itesm.mx.infrastructure.ingestion.FileDetectionService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.logging.Logger;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * SCRUM-315: Orchestrates the "Buscar actualizaciones" flow.
 * 1. Creates an IngestionRun record.
 * 2. Calls DiscoveryService to find CSV files.
 * 3. Calls FileDetectionService to determine new/changed/unchanged.
 * 4. Produces one Kafka message per new/changed file to topic ingestion.files.
 * 5. Updates the IngestionRun with counts.
 */
@ApplicationScoped
public class CheckIngestionUseCase {

    private static final Logger LOG = Logger.getLogger(CheckIngestionUseCase.class);

    @Inject
    DiscoveryService discoveryService;

    @Inject
    FileDetectionService fileDetectionService;

    @Inject
    IngestionRunRepository ingestionRunRepository;

    @Inject
    IngestionFileRepository ingestionFileRepository;

    @Inject
    @Channel("ingestion-files-out")
    Emitter<IngestionFileMessage> fileEmitter;

    @Transactional
    public IngestionRun execute() {
        // Create run record
        IngestionRun run = new IngestionRun();
        run.setStartedAt(LocalDateTime.now());
        run.setStatus("RUNNING");
        run.setFilesFound(0);
        run.setFilesProcessed(0);
        ingestionRunRepository.save(run);
        LOG.infof("IngestionRun %d started", run.getId());

        List<DiscoveredFile> discovered;
        try {
            discovered = discoveryService.discoverFiles();
        } catch (Exception e) {
            LOG.errorf(e, "Discovery failed for run %d", run.getId());
            run.setStatus("FAILED");
            run.setFinishedAt(LocalDateTime.now());
            ingestionRunRepository.update(run);
            throw new RuntimeException("SENASICA discovery failed: " + e.getMessage(), e);
        }

        run.setFilesFound(discovered.size());
        int queued = 0;

        for (DiscoveredFile df : discovered) {
            FileDetectionService.IngestionFileHolder holder = new FileDetectionService.IngestionFileHolder();
            FileDetectionService.DetectionResult detection = fileDetectionService.detect(df, holder);

            if (detection == FileDetectionService.DetectionResult.UNCHANGED) {
                LOG.debugf("Unchanged, skipping: %s", df.getSourceUrl());
                continue;
            }

            // Persist or update the IngestionFile record
            IngestionFile fileRecord = new IngestionFile();
            fileRecord.setRunId(run.getId());
            fileRecord.setSourceUrl(df.getSourceUrl());
            fileRecord.setFilename(df.getFilename());
            fileRecord.setEtag(holder.etag);
            fileRecord.setLastModified(holder.lastModified);
            fileRecord.setContentLength(holder.contentLength);
            fileRecord.setChecksum(holder.checksum);
            fileRecord.setStatus("PENDING");
            fileRecord.setCreatedAt(LocalDateTime.now());
            ingestionFileRepository.save(fileRecord);

            IngestionFileMessage msg = new IngestionFileMessage(
                    run.getId(), df.getSourceUrl(), df.getFilename(), holder.etag, holder.checksum
            );
            fileEmitter.send(msg);
            queued++;
            LOG.infof("Queued %s file: %s", detection, df.getSourceUrl());
        }

        run.setFilesProcessed(queued);
        if (queued == 0 && discovered.isEmpty()) {
            run.setStatus("COMPLETED");
            run.setFinishedAt(LocalDateTime.now());
        } else if (queued > 0) {
            // Run stays RUNNING until the consumer finishes
        } else {
            run.setStatus("COMPLETED");
            run.setFinishedAt(LocalDateTime.now());
        }
        ingestionRunRepository.update(run);
        LOG.infof("IngestionRun %d: %d discovered, %d queued", run.getId(), discovered.size(), queued);
        return run;
    }
}
