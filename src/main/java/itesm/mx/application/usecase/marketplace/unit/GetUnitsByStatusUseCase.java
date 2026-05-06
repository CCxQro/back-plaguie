package itesm.mx.application.usecase.marketplace.unit;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import itesm.mx.domain.models.marketplace.Unit;
import itesm.mx.domain.repository.marketplace.UnitRepository;

import java.util.List;

@ApplicationScoped
public class GetUnitsByStatusUseCase {

    @Inject
    UnitRepository unitRepository;

    public List<Unit> execute(Long statusId) {
        if (statusId == null) {
            throw new IllegalArgumentException("Status id is required");
        }
        return unitRepository.findAllByStatusId(statusId);
    }
}