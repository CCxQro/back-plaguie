package itesm.mx.application.usecase.location.location;

import itesm.mx.application.dto.GetLocationResponseDto;
import itesm.mx.application.mapper.location.LocationDtoMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import itesm.mx.domain.models.location.Locality;
import itesm.mx.domain.models.location.Location;
import itesm.mx.domain.models.location.LocationData;
import itesm.mx.domain.models.location.Municipality;
import itesm.mx.domain.models.location.Property;
import itesm.mx.domain.models.location.State;
import itesm.mx.domain.repository.location.LocationRepository;

@ApplicationScoped
public class RegisterLocationUseCase {

    @Inject
    LocationRepository locationRepository;

    @Inject
    LocationCatalogResolver catalogResolver;

    @Transactional
    public GetLocationResponseDto execute(LocationData locationData) {
        if (locationData == null) {
            throw new IllegalArgumentException("La ubicacion es requerida");
        }

        validateCoordinates(locationData.getCoordinates());

        Location location = registerLocation(locationData);
        return LocationDtoMapper.toResponseDto(location);
    }

    private Location registerLocation(LocationData locationData) {
        if (locationData.getCoordinates() == null) {
            throw new IllegalArgumentException("Las coordenadas son requeridas");
        }

        State state = catalogResolver.resolveState(locationData.getStateName());
        Municipality municipality = catalogResolver.resolveMunicipality(locationData.getMunicipalityName());
        Locality locality = catalogResolver.resolveLocality(locationData.getLocalityName());
        Property property = catalogResolver.resolveProperty(locationData.getPropertyName());

        return locationRepository.register(
                new Location(
                        null,
                        locationData.getCoordinates(),
                        state,
                        municipality,
                        locality,
                        property
                )
        );
    }

    private void validateCoordinates(org.locationtech.jts.geom.Point coordinates) {
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
