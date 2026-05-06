package itesm.mx.application.usecase.marketplace.unit;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import itesm.mx.domain.models.marketplace.Unit;
import itesm.mx.domain.repository.marketplace.UnitRepository;

import java.util.List;

@ApplicationScoped
public class GetAllUnitsUseCase {

    @Inject
    UnitRepository unitRepository;

    public List<Unit> execute() {
        return unitRepository.findAllUnits();
    }
}