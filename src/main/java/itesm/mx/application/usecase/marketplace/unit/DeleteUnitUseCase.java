package itesm.mx.application.usecase.marketplace.unit;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import itesm.mx.domain.repository.marketplace.UnitRepository;

@ApplicationScoped
public class DeleteUnitUseCase {

    @Inject
    UnitRepository unitRepository;

    @Transactional
    public void execute(Long unitId) {
        if (unitId == null) {
            throw new IllegalArgumentException("Unit id is required");
        }
        unitRepository.delete(unitId);
    }
}