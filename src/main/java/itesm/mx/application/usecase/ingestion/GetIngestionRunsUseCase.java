package itesm.mx.application.usecase.ingestion;

import itesm.mx.domain.models.ingestion.IngestionRun;
import itesm.mx.domain.repository.ingestion.IngestionRunRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

/**
 * SCRUM-317: Returns paginated history of all ingestion runs.
 */
@ApplicationScoped
public class GetIngestionRunsUseCase {

    @Inject
    IngestionRunRepository ingestionRunRepository;

    public List<IngestionRun> execute() {
        return ingestionRunRepository.findAllOrderedByStartedAtDesc();
    }
}
