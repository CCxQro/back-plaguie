package itesm.mx.application.usecase.marketplace.unit;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import itesm.mx.domain.models.marketplace.Unit;
import itesm.mx.domain.repository.marketplace.UnitRepository;

@ApplicationScoped
public class UpdateUnitUseCase {

    @Inject
    UnitRepository unitRepository;

    @Transactional
    public Unit execute(Long unitId, Unit unit) {
        if (unitId == null) {
            throw new IllegalArgumentException("Unit id is required");
        }
        if (unit == null) {
            throw new IllegalArgumentException("Unit is required");
        }
        if (unit.getName() == null || unit.getName().isBlank()) {
            throw new IllegalArgumentException("Unit name is required");
        }
        if (unit.getUser() == null || unit.getUser().getUserId() == null) {
            throw new IllegalArgumentException("User is required");
        }
        if (unit.getStatus() == null || unit.getStatus().getStatusId() == null) {
            throw new IllegalArgumentException("Status is required");
        }
        return unitRepository.update(unitId, unit);
    }
}