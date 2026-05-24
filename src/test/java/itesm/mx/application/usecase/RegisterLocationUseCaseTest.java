package itesm.mx.application.usecase;

import itesm.mx.application.dto.GetLocationResponseDto;
import itesm.mx.application.usecase.location.location.LocationCatalogResolver;
import itesm.mx.application.usecase.location.location.RegisterLocationUseCase;
import itesm.mx.domain.models.location.Locality;
import itesm.mx.domain.models.location.Location;
import itesm.mx.domain.models.location.LocationData;
import itesm.mx.domain.models.location.Municipality;
import itesm.mx.domain.models.location.Property;
import itesm.mx.domain.models.location.State;
import itesm.mx.domain.repository.location.LocationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterLocationUseCaseTest {

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

    @Mock
    LocationRepository locationRepository;

    @Mock
    LocationCatalogResolver catalogResolver;

    @InjectMocks
    RegisterLocationUseCase registerLocationUseCase;

    private void stubCatalogs(State state, Municipality municipality, Locality locality, Property property) {
        when(catalogResolver.resolveState(any())).thenReturn(state);
        when(catalogResolver.resolveMunicipality(any())).thenReturn(municipality);
        when(catalogResolver.resolveLocality(any())).thenReturn(locality);
        when(catalogResolver.resolveProperty(any())).thenReturn(property);
    }

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
    void execute_WhenIdenticalLocationExists_StillCreatesNewRow() {
        Point coordinates = GEOMETRY_FACTORY.createPoint(new Coordinate(-103.35, 20.67));
        LocationData locationData = new LocationData(coordinates, "Jalisco", "Guadalajara", "Centro", "Predio Norte");

        State state = new State(1L, "jalisco");
        Municipality municipality = new Municipality(2L, "guadalajara");
        Locality locality = new Locality(3L, "centro");
        Property property = new Property(4L, "predio norte");
        Location createdLocation = new Location(11L, coordinates, state, municipality, locality, property);

        stubCatalogs(state, municipality, locality, property);
        when(locationRepository.register(any(Location.class))).thenReturn(createdLocation);

        GetLocationResponseDto response = registerLocationUseCase.execute(locationData);

        assertEquals(11L, response.locationId);
        assertEquals(20.67, response.latitude);
        assertEquals(-103.35, response.longitude);
        assertEquals("jalisco", response.stateName);
        verify(locationRepository).register(any(Location.class));
    }

    @Test
    void execute_WhenCalledTwiceWithSameData_CreatesTwoSeparateRows() {
        Point coordinates = GEOMETRY_FACTORY.createPoint(new Coordinate(-103.35, 20.67));
        LocationData locationData = new LocationData(coordinates, "Jalisco", "Guadalajara", "Centro", "Predio Norte");

        State state = new State(1L, "jalisco");
        Municipality municipality = new Municipality(2L, "guadalajara");
        Locality locality = new Locality(3L, "centro");
        Property property = new Property(4L, "predio norte");
        Location firstRow = new Location(11L, coordinates, state, municipality, locality, property);
        Location secondRow = new Location(12L, coordinates, state, municipality, locality, property);

        stubCatalogs(state, municipality, locality, property);
        when(locationRepository.register(any(Location.class))).thenReturn(firstRow, secondRow);

        GetLocationResponseDto first = registerLocationUseCase.execute(locationData);
        GetLocationResponseDto second = registerLocationUseCase.execute(locationData);

        assertNotEquals(first.locationId, second.locationId);
        verify(locationRepository, times(2)).register(any(Location.class));
    }

    @Test
    void execute_WhenLocationDataIsNull_ThrowsIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> registerLocationUseCase.execute(null)
        );

        assertEquals("La ubicacion es requerida", exception.getMessage());
    }

    @Test
    void execute_WhenLongitudeIsInvalid_ThrowsIllegalArgumentException() {
        Point coordinates = GEOMETRY_FACTORY.createPoint(new Coordinate(200.00, 20.67));
        LocationData locationData = new LocationData(coordinates, "Jalisco", "Guadalajara", "Centro", "Predio");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> registerLocationUseCase.execute(locationData)
        );

        assertEquals("La longitud no es valida", exception.getMessage());
    }

    @Test
    void execute_WithValidLocationData_RegistersAndReturnsLocation() {
        Point coordinates = GEOMETRY_FACTORY.createPoint(new Coordinate(-103.40, 20.70));
        LocationData locationData = new LocationData(coordinates, "Jalisco", "Zapopan", "Arcos", "Predio Sur");

        State state = new State(1L, "jalisco");
        Municipality municipality = new Municipality(2L, "zapopan");
        Locality locality = new Locality(3L, "arcos");
        Property property = new Property(4L, "predio sur");
        Location createdLocation = new Location(10L, coordinates, state, municipality, locality, property);

        stubCatalogs(state, municipality, locality, property);
        when(locationRepository.register(any(Location.class))).thenReturn(createdLocation);

        GetLocationResponseDto response = registerLocationUseCase.execute(locationData);

        assertEquals(10L, response.locationId);
        assertEquals("jalisco", response.stateName);
        assertEquals("zapopan", response.municipalityName);
        assertEquals("arcos", response.localityName);
        assertEquals("predio sur", response.propertyName);
        verify(locationRepository).register(any(Location.class));
    }
}
