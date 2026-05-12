package itesm.mx.domain.repository.marketplace;

import itesm.mx.domain.models.marketplace.Unit;

import java.util.List;
import java.util.Optional;

public interface UnitRepository {
    Unit save(Unit unit);
    Unit update(Long unitId, Unit unit);
    void delete(Long unitId);
    Optional<Unit> findByUnitId(Long unitId);
    List<Unit> findAllUnits();
    List<Unit> findAllByUserId(Long userId);
    List<Unit> findAllByStatusId(Long statusId);
}