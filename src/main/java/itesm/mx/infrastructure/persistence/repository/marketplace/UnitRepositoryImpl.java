package itesm.mx.infrastructure.persistence.repository.marketplace;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import itesm.mx.domain.models.marketplace.Unit;
import itesm.mx.domain.repository.marketplace.UnitRepository;
import itesm.mx.infrastructure.mapper.marketplace.UnitMapper;
import itesm.mx.infrastructure.persistence.entity.marketplace.UnitEntity;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class UnitRepositoryImpl implements PanacheRepositoryBase<UnitEntity, Long>, UnitRepository {

    @Override
    public Unit save(Unit unit) {
        UnitEntity entity = UnitMapper.toEntity(unit);
        persistAndFlush(entity);
        return UnitMapper.toDomain(entity);
    }

    @Override
    public Unit update(Long unitId, Unit unit) {
        UnitEntity entity = findByIdOptional(unitId)
                .orElseThrow(() -> new IllegalArgumentException("Unit not found: " + unitId));
        entity.name = unit.getName();
        entity.userId = unit.getUser().getUserId();
        entity.statusId = unit.getStatus().getStatusId();
        flush();
        return UnitMapper.toDomain(entity);
    }

    @Override
    public void delete(Long unitId) {
        UnitEntity entity = findByIdOptional(unitId)
                .orElseThrow(() -> new IllegalArgumentException("Unit not found: " + unitId));
        delete(entity);
    }

    @Override
    public Optional<Unit> findByUnitId(Long unitId) {
        return findByIdOptional(unitId).map(UnitMapper::toDomain);
    }

    @Override
    public List<Unit> findAllUnits() {
        return listAll().stream()
                .map(UnitMapper::toDomain)
                .toList();
    }

    @Override
    public List<Unit> findAllByUserId(Long userId) {
        return find("userId", userId).stream()
                .map(UnitMapper::toDomain)
                .toList();
    }

    @Override
    public List<Unit> findAllByStatusId(Long statusId) {
        return find("statusId", statusId).stream()
                .map(UnitMapper::toDomain)
                .toList();
    }
}