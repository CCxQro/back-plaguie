package itesm.mx.infrastructure.persistence.repository.location;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import itesm.mx.domain.models.location.Location;
import itesm.mx.domain.repository.location.LocationRepository;
import itesm.mx.infrastructure.mapper.location.LocationMapper;
import itesm.mx.infrastructure.persistence.entity.location.LocationEntity;

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
    public Location register(Location location) {
        LocationEntity entity = LocationMapper.toEntity(location);
        persistAndFlush(entity);

        return findDetailedById(entity.locationId)
                .map(LocationMapper::toDomain)
                .orElseThrow(() -> new IllegalStateException("No se pudo recuperar la ubicacion recien registrada"));
    }

    @Override
    public Location update(Location location) {
        LocationEntity entity = findByIdOptional(location.getLocationId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Ubicacion no encontrada con id: " + location.getLocationId()));

        entity.coordinates = location.getCoordinates();
        entity.stateId = location.getState().getStateId();
        entity.municipalityId = location.getMunicipality().getMunicipalityId();
        entity.localityId = location.getLocality().getLocalityId();
        entity.propertyId = location.getProperty().getPropertyId();
        persistAndFlush(entity);

        return findDetailedById(entity.locationId)
                .map(LocationMapper::toDomain)
                .orElseThrow(() -> new IllegalStateException("No se pudo recuperar la ubicacion actualizada"));
    }

    @Override
    public Optional<Location> findLocationById(Long locationId) {
        return findDetailedById(locationId).map(LocationMapper::toDomain);
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
