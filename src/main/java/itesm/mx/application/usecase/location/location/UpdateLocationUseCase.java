package itesm.mx.application.usecase.location.location;

import itesm.mx.application.dto.GetLocationResponseDto;
import itesm.mx.application.mapper.location.LocationDtoMapper;
import itesm.mx.domain.models.location.Locality;
import itesm.mx.domain.models.location.Location;
import itesm.mx.domain.models.location.LocationData;
import itesm.mx.domain.models.location.Municipality;
import itesm.mx.domain.models.location.Property;
import itesm.mx.domain.models.location.State;
import itesm.mx.domain.repository.location.LocationRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.locationtech.jts.geom.Point;

@ApplicationScoped
public class UpdateLocationUseCase {

    @Inject
    LocationRepository locationRepository;

    @Inject
    LocationCatalogResolver catalogResolver;

    @Transactional
    public GetLocationResponseDto execute(Long existingLocationId, LocationData locationData) {
        if (existingLocationId == null || existingLocationId <= 0) {
            throw new IllegalArgumentException("El ID de la ubicacion es requerido");
        }
        if (locationData == null) {
            throw new IllegalArgumentException("La ubicacion es requerida");
        }

        validateCoordinates(locationData.getCoordinates());

        State state = catalogResolver.resolveState(locationData.getStateName());
        Municipality municipality = catalogResolver.resolveMunicipality(locationData.getMunicipalityName());
        Locality locality = catalogResolver.resolveLocality(locationData.getLocalityName());
        Property property = catalogResolver.resolveProperty(locationData.getPropertyName());

        Location updated = locationRepository.update(
                new Location(
                        existingLocationId,
                        locationData.getCoordinates(),
                        state,
                        municipality,
                        locality,
                        property
                )
        );
        return LocationDtoMapper.toResponseDto(updated);
    }

    private void validateCoordinates(Point coordinates) {
        if (coordinates == null) {
            throw new IllegalArgumentException("Las coordenadas son requeridas");
        }

        double latitude = coordinates.getY();
        double longitude = coordinates.getX();

        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("La latitud no es valida");
        }
        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("La longitud no es valida");
        }
    }
}
