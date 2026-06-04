package itesm.mx.domain.repository.ingestion;

import itesm.mx.domain.models.ingestion.IngestionFile;

import java.util.List;
import java.util.Optional;

public interface IngestionFileRepository {
    IngestionFile save(IngestionFile file);
    IngestionFile update(IngestionFile file);
    Optional<IngestionFile> findBySourceUrl(String sourceUrl);
    Optional<IngestionFile> findByChecksum(String checksum);
    List<IngestionFile> findByRunId(Long runId);
}
