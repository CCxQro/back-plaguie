package itesm.mx.application.usecase.marketplace.unit;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import itesm.mx.domain.models.marketplace.Unit;
import itesm.mx.domain.repository.marketplace.UnitRepository;

import java.util.List;

@ApplicationScoped
public class GetUnitsByUserUseCase {

    @Inject
    UnitRepository unitRepository;

    public List<Unit> execute(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User id is required");
        }
        return unitRepository.findAllByUserId(userId);
    }
}