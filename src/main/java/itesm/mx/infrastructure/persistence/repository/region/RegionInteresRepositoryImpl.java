package itesm.mx.infrastructure.persistence.repository.region;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import itesm.mx.domain.models.region.RegionInteres;
import itesm.mx.domain.repository.region.RegionInteresRepository;
import itesm.mx.infrastructure.mapper.region.RegionInteresMapper;
import itesm.mx.infrastructure.persistence.entity.region.RegionInteresEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class RegionInteresRepositoryImpl
        implements PanacheRepositoryBase<RegionInteresEntity, Long>, RegionInteresRepository {

    @Override
    public List<RegionInteres> findByUserId(Long userId) {
        return findDetailedQuery("where r.userId = ?1 order by r.createdAt desc", userId)
                .list()
                .stream()
                .map(RegionInteresMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<RegionInteres> findByIdAndUserId(Long regionInteresId, Long userId) {
        return findDetailedQuery("where r.regionInteresId = ?1 and r.userId = ?2", regionInteresId, userId)
                .firstResultOptional()
                .map(RegionInteresMapper::toDomain);
    }

    @Override
    public boolean existsByUserIdAndStateId(Long userId, Long stateId) {
        return count("userId = ?1 and stateId = ?2", userId, stateId) > 0;
    }

    @Override
    @Transactional
    public RegionInteres save(RegionInteres regionInteres) {
        RegionInteresEntity entity = RegionInteresMapper.toEntity(regionInteres);
        persistAndFlush(entity);
        getEntityManager().clear();
        return findDetailedQuery("where r.regionInteresId = ?1", entity.regionInteresId)
                .firstResultOptional()
                .map(RegionInteresMapper::toDomain)
                .orElseThrow(() -> new IllegalStateException(
                        "No se pudo recuperar la región de interés recién registrada"));
    }

    @Override
    @Transactional
    public boolean deleteByIdAndUserId(Long regionInteresId, Long userId) {
        return delete("regionInteresId = ?1 and userId = ?2", regionInteresId, userId) > 0;
    }

    private io.quarkus.hibernate.orm.panache.PanacheQuery<RegionInteresEntity> findDetailedQuery(
            String whereClause, Object... parameters) {
        String query = """
                select r
                from RegionInteresEntity r
                left join fetch r.state
                """ + whereClause;
        return find(query, parameters);
    }
}
