package itesm.mx.application.usecase;

import itesm.mx.application.dto.GetLocationResponseDto;
import itesm.mx.application.usecase.location.location.RegisterLocationUseCase;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterLocationUseCaseTest {

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

    @Mock
    LocationRepository locationRepository;

    @Mock
    StateRepository stateRepository;

    @Mock
    MunicipalityRepository municipalityRepository;

    @Mock
    LocalityRepository localityRepository;

    @Mock
    PropertyRepository propertyRepository;

    @InjectMocks
    RegisterLocationUseCase registerLocationUseCase;

    @Test
    void execute_WhenCoordinatesAreInvalid_ThrowsIllegalArgumentException() {
        Point coordinates = GEOMETRY_FACTORY.createPoint(new Coordinate(-103.35, 95.00));
        LocationData locationData = new LocationData(coordinates, "Jalisco", "Guadalajara", "Centro", "Predio");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> registerLocationUseCase.execute(locationData)
        );

        assertEquals("La latitud no es valida", exception.getMessage());
    }

    @Test
    void execute_WhenLocationAlreadyExists_ReturnsExistingLocation() {
        Point coordinates = GEOMETRY_FACTORY.createPoint(new Coordinate(-103.35, 20.67));
        LocationData locationData = new LocationData(coordinates, "Jalisco", "Guadalajara", "Centro", "Predio Norte");

        State state = new State(1L, "jalisco");
        Municipality municipality = new Municipality(2L, "guadalajara");
        Locality locality = new Locality(3L, "centro");
        Property property = new Property(4L, "predio norte");
        Location existingLocation = new Location(9L, coordinates, state, municipality, locality, property);

        when(stateRepository.findByName("jalisco")).thenReturn(Optional.of(state));
        when(municipalityRepository.findByName("guadalajara")).thenReturn(Optional.of(municipality));
        when(localityRepository.findByName("centro")).thenReturn(Optional.of(locality));
        when(propertyRepository.findByName("predio norte")).thenReturn(Optional.of(property));
        when(locationRepository.findByResolvedData(eq(coordinates), eq(1L), eq(2L), eq(3L), eq(4L)))
                .thenReturn(Optional.of(existingLocation));

        GetLocationResponseDto response = registerLocationUseCase.execute(locationData);

        assertEquals(9L, response.locationId);
        assertEquals(20.67, response.latitude);
        assertEquals(-103.35, response.longitude);
        assertEquals("jalisco", response.stateName);
        verify(locationRepository, never()).register(any(Location.class));
    }

    @Test
    void execute_WhenCatalogsAndLocationDoNotExist_RegistersEverythingNeeded() {
        Point coordinates = GEOMETRY_FACTORY.createPoint(new Coordinate(-103.40, 20.70));
        LocationData locationData = new LocationData(coordinates, "Jalisco", "Zapopan", "Arcos", "Predio Sur");

        State state = new State(1L, "jalisco");
        Municipality municipality = new Municipality(2L, "zapopan");
        Locality locality = new Locality(3L, "arcos");
        Property property = new Property(4L, "predio sur");
        Location createdLocation = new Location(10L, coordinates, state, municipality, locality, property);

        when(stateRepository.findByName("jalisco")).thenReturn(Optional.empty());
        when(stateRepository.register(any(State.class))).thenReturn(state);
        when(municipalityRepository.findByName("zapopan")).thenReturn(Optional.empty());
        when(municipalityRepository.register(any(Municipality.class))).thenReturn(municipality);
        when(localityRepository.findByName("arcos")).thenReturn(Optional.empty());
        when(localityRepository.register(any(Locality.class))).thenReturn(locality);
        when(propertyRepository.findByName("predio sur")).thenReturn(Optional.empty());
        when(propertyRepository.register(any(Property.class))).thenReturn(property);
        when(locationRepository.findByResolvedData(eq(coordinates), eq(1L), eq(2L), eq(3L), eq(4L)))
                .thenReturn(Optional.empty());
        when(locationRepository.register(any(Location.class))).thenReturn(createdLocation);

        GetLocationResponseDto response = registerLocationUseCase.execute(locationData);

        assertEquals(10L, response.locationId);
        assertEquals("jalisco", response.stateName);
        assertEquals("zapopan", response.municipalityName);
        assertEquals("arcos", response.localityName);
        assertEquals("predio sur", response.propertyName);
        verify(stateRepository).register(any(State.class));
        verify(municipalityRepository).register(any(Municipality.class));
        verify(localityRepository).register(any(Locality.class));
        verify(propertyRepository).register(any(Property.class));
        verify(locationRepository).register(any(Location.class));
    }
}
