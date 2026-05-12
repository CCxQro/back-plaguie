package itesm.mx.application.usecase.marketplace.unit;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import itesm.mx.domain.models.marketplace.Unit;
import itesm.mx.domain.repository.marketplace.UnitRepository;

import java.util.Optional;

@ApplicationScoped
public class GetUnitByIdUseCase {

    @Inject
    UnitRepository unitRepository;

    public Optional<Unit> execute(Long unitId) {
        if (unitId == null) {
            throw new IllegalArgumentException("Unit id is required");
        }
        return unitRepository.findByUnitId(unitId);
    }
}