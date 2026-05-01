package itesm.mx.infrastructure.persistence.repository.location;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import itesm.mx.domain.models.location.Location;
import itesm.mx.domain.repository.location.LocationRepository;
import itesm.mx.infrastructure.mapper.location.LocationMapper;
import itesm.mx.infrastructure.persistence.entity.location.LocationEntity;
import org.locationtech.jts.geom.Point;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class LocationRepositoryImpl implements PanacheRepositoryBase<LocationEntity, Long>, LocationRepository {

    @Override
    public List<Location> findAllLocations() {
        return find(
                """
                select l
                from LocationEntity l
                left join fetch l.state
                left join fetch l.municipality
                left join fetch l.locality
                left join fetch l.property
                """
        ).list().stream()
                .map(LocationMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Location> findByResolvedData(
            Point coordinates,
            Long stateId,
            Long municipalityId,
            Long localityId,
            Long propertyId
    ) {
        return find(
                """
                select l
                from LocationEntity l
                left join fetch l.state
                left join fetch l.municipality
                left join fetch l.locality
                left join fetch l.property
                where l.coordinates = ?1
                  and l.stateId = ?2
                  and l.municipalityId = ?3
                  and l.localityId = ?4
                  and l.propertyId = ?5
                """,
                coordinates,
                stateId,
                municipalityId,
                localityId,
                propertyId
        ).firstResultOptional().map(LocationMapper::toDomain);
    }

    @Override
    public Location register(Location location) {
        LocationEntity entity = LocationMapper.toEntity(location);
        persistAndFlush(entity);

        return findDetailedById(entity.locationId)
                .map(LocationMapper::toDomain)
                .orElseThrow(() -> new IllegalStateException("No se pudo recuperar la ubicacion recien registrada"));
    }

    private Optional<LocationEntity> findDetailedById(Long locationId) {
        return find(
                """
                select l
                from LocationEntity l
                left join fetch l.state
                left join fetch l.municipality
                left join fetch l.locality
                left join fetch l.property
                where l.locationId = ?1
                """,
                locationId
        ).firstResultOptional();
    }
}
