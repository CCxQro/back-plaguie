package itesm.mx.domain.repository.ingestion;

import itesm.mx.domain.models.ingestion.IngestionRun;

import java.util.List;
import java.util.Optional;

public interface IngestionRunRepository {
    IngestionRun save(IngestionRun run);
    IngestionRun update(IngestionRun run);
    Optional<IngestionRun> findRunById(Long id);
    List<IngestionRun> findAllOrderedByStartedAtDesc();
}
