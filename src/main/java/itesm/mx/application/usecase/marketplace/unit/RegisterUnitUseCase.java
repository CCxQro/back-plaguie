package itesm.mx.application.usecase.marketplace.unit;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import itesm.mx.domain.models.marketplace.Unit;
import itesm.mx.domain.repository.marketplace.UnitRepository;

@ApplicationScoped
public class RegisterUnitUseCase {

    @Inject
    UnitRepository unitRepository;

    @Transactional
    public Unit execute(Unit unit) {
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
        return unitRepository.save(unit);
    }
}