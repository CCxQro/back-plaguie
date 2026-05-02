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
import itesm.mx.domain.repository.location.LocalityRepository;
import itesm.mx.domain.repository.location.LocationRepository;
import itesm.mx.domain.repository.location.MunicipalityRepository;
import itesm.mx.domain.repository.location.PropertyRepository;
import itesm.mx.domain.repository.location.StateRepository;
import itesm.mx.domain.util.LocationNormalizer;

@ApplicationScoped
public class RegisterLocationUseCase {

    @Inject
    LocationRepository locationRepository;

    @Inject
    StateRepository stateRepository;

    @Inject
    MunicipalityRepository municipalityRepository;

    @Inject
    LocalityRepository localityRepository;

    @Inject
    PropertyRepository propertyRepository;

    @Transactional
    public GetLocationResponseDto execute(LocationData locationData) {
        if (locationData == null) {
            throw new IllegalArgumentException("La ubicacion es requerida");
        }

        validateCoordinates(locationData.getCoordinates());

        Location location = registerOrFindLocation(locationData);
        return LocationDtoMapper.toResponseDto(location);
    }

    private Location registerOrFindLocation(LocationData locationData) {
        if (locationData.getCoordinates() == null) {
            throw new IllegalArgumentException("Las coordenadas son requeridas");
        }

        State state = resolveState(locationData.getStateName());
        Municipality municipality = resolveMunicipality(locationData.getMunicipalityName());
        Locality locality = resolveLocality(locationData.getLocalityName());
        Property property = resolveProperty(locationData.getPropertyName());

        return locationRepository.findByResolvedData(
                        locationData.getCoordinates().getX(),
                        locationData.getCoordinates().getY(),
                        state.getStateId(),
                        municipality.getMunicipalityId(),
                        locality.getLocalityId(),
                        property.getPropertyId()
                )
                .orElseGet(() -> locationRepository.register(
                        new Location(
                                null,
                                locationData.getCoordinates(),
                                state,
                                municipality,
                                locality,
                                property
                        )
                ));
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

    private State resolveState(String stateName) {
        String normalizedName = LocationNormalizer.normalize(stateName);

        return stateRepository.findByName(normalizedName)
                .orElseGet(() -> stateRepository.register(new State(null, normalizedName)));
    }

    private Municipality resolveMunicipality(String municipalityName) {
        String normalizedName = LocationNormalizer.normalize(municipalityName);

        return municipalityRepository.findByName(normalizedName)
                .orElseGet(() -> municipalityRepository.register(new Municipality(null, normalizedName)));
    }

    private Locality resolveLocality(String localityName) {
        String normalizedName = LocationNormalizer.normalize(localityName);

        return localityRepository.findByName(normalizedName)
                .orElseGet(() -> localityRepository.register(new Locality(null, normalizedName)));
    }

    private Property resolveProperty(String propertyName) {
        String normalizedName = LocationNormalizer.normalize(propertyName);

        return propertyRepository.findByName(normalizedName)
                .orElseGet(() -> propertyRepository.register(new Property(null, normalizedName)));
    }
}
