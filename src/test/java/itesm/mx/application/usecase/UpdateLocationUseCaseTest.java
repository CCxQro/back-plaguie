package itesm.mx.application.usecase;

import itesm.mx.application.dto.GetLocationResponseDto;
import itesm.mx.application.usecase.location.location.LocationCatalogResolver;
import itesm.mx.application.usecase.location.location.UpdateLocationUseCase;
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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateLocationUseCaseTest {

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

    @Mock
    LocationRepository locationRepository;

    @Mock
    LocationCatalogResolver catalogResolver;

    @InjectMocks
    UpdateLocationUseCase updateLocationUseCase;

    private void stubCatalogs(State state, Municipality municipality, Locality locality, Property property) {
        when(catalogResolver.resolveState(any())).thenReturn(state);
        when(catalogResolver.resolveMunicipality(any())).thenReturn(municipality);
        when(catalogResolver.resolveLocality(any())).thenReturn(locality);
        when(catalogResolver.resolveProperty(any())).thenReturn(property);
    }

    @Test
    void execute_UpdatesExistingLocationInPlaceAndKeepsTheSameId() {
        Long existingId = 7L;
        Point coordinates = GEOMETRY_FACTORY.createPoint(new Coordinate(-103.40, 20.70));
        LocationData locationData = new LocationData(coordinates, "Jalisco", "Zapopan", "Arcos", "Predio Sur");

        State state = new State(1L, "jalisco");
        Municipality municipality = new Municipality(2L, "zapopan");
        Locality locality = new Locality(3L, "arcos");
        Property property = new Property(4L, "predio sur");
        Location updated = new Location(existingId, coordinates, state, municipality, locality, property);

        stubCatalogs(state, municipality, locality, property);
        when(locationRepository.update(any(Location.class))).thenReturn(updated);

        GetLocationResponseDto response = updateLocationUseCase.execute(existingId, locationData);

        ArgumentCaptor<Location> captor = ArgumentCaptor.forClass(Location.class);
        verify(locationRepository).update(captor.capture());
        assertEquals(existingId, captor.getValue().getLocationId());
        assertEquals(existingId, response.locationId);
        assertEquals(20.70, response.latitude);
        assertEquals(-103.40, response.longitude);
        assertEquals("zapopan", response.municipalityName);
        verify(locationRepository, never()).register(any(Location.class));
    }

    @Test
    void execute_WhenLocationIdIsNull_ThrowsIllegalArgumentException() {
        Point coordinates = GEOMETRY_FACTORY.createPoint(new Coordinate(-103.40, 20.70));
        LocationData locationData = new LocationData(coordinates, "Jalisco", "Zapopan", "Arcos", "Predio Sur");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> updateLocationUseCase.execute(null, locationData)
        );

        assertEquals("El ID de la ubicacion es requerido", exception.getMessage());
    }

    @Test
    void execute_WhenLocationDataIsNull_ThrowsIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> updateLocationUseCase.execute(7L, null)
        );

        assertEquals("La ubicacion es requerida", exception.getMessage());
    }

    @Test
    void execute_WhenLatitudeIsInvalid_ThrowsIllegalArgumentException() {
        Point coordinates = GEOMETRY_FACTORY.createPoint(new Coordinate(-103.35, 95.00));
        LocationData locationData = new LocationData(coordinates, "Jalisco", "Guadalajara", "Centro", "Predio");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> updateLocationUseCase.execute(7L, locationData)
        );

        assertEquals("La latitud no es valida", exception.getMessage());
    }
}
