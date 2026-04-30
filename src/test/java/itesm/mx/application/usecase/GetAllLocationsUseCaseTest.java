package itesm.mx.application.usecase;

import itesm.mx.application.dto.GetLocationResponseDto;
import itesm.mx.application.usecase.location.location.GetAllLocationsUseCase;
import itesm.mx.domain.models.location.Locality;
import itesm.mx.domain.models.location.Location;
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

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetAllLocationsUseCaseTest {

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

    @Mock
    LocationRepository locationRepository;

    @InjectMocks
    GetAllLocationsUseCase getAllLocationsUseCase;

    @Test
    void execute_WhenLocationsExist_ReturnsMappedDtos() {
        Point coordinates = GEOMETRY_FACTORY.createPoint(new Coordinate(-103.35, 20.67));
        Location location = new Location(
                7L,
                coordinates,
                new State(1L, "jalisco"),
                new Municipality(2L, "guadalajara"),
                new Locality(3L, "centro"),
                new Property(4L, "predio norte")
        );

        when(locationRepository.findAllLocations()).thenReturn(List.of(location));

        List<GetLocationResponseDto> response = getAllLocationsUseCase.execute();

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals(7L, response.get(0).locationId);
        assertEquals(20.67, response.get(0).latitude);
        assertEquals(-103.35, response.get(0).longitude);
        assertEquals("jalisco", response.get(0).stateName);
        assertEquals("guadalajara", response.get(0).municipalityName);
        assertEquals("centro", response.get(0).localityName);
        assertEquals("predio norte", response.get(0).propertyName);
    }
}
