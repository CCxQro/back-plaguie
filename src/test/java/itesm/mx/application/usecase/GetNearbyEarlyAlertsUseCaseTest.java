package itesm.mx.application.usecase;

import itesm.mx.application.dto.GetEarlyAlertaResponseDto;
import itesm.mx.application.usecase.region.GetNearbyEarlyAlertsUseCase;
import itesm.mx.domain.models.alerta.Alerta;
import itesm.mx.domain.models.location.Location;
import itesm.mx.domain.models.user.User;
import itesm.mx.domain.repository.alerta.AlertaRepository;
import itesm.mx.domain.repository.location.LocationRepository;
import itesm.mx.domain.repository.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetNearbyEarlyAlertsUseCaseTest {

    private static final GeometryFactory GF = new GeometryFactory();

    @Mock
    private UserRepository userRepository;

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private AlertaRepository alertaRepository;

    @InjectMocks
    private GetNearbyEarlyAlertsUseCase useCase;

    private Point point(double lat, double lng) {
        return GF.createPoint(new Coordinate(lng, lat)); // X=lng, Y=lat
    }

    private User sellerWithLocation(long locationId) {
        User user = new User();
        user.setUserId(11L);
        Location loc = new Location();
        loc.setLocationId(locationId);
        user.setLocation(loc);
        return user;
    }

    private Alerta alertaAt(long id, double lat, double lng) {
        Alerta a = new Alerta();
        a.setAlertaId(id);
        a.setTitulo("Alerta " + id);
        a.setSeveridad("critico");
        a.setStatusId(1L);
        a.setCreatedAt(LocalDateTime.now());
        a.setLatitude(lat);
        a.setLongitude(lng);
        return a;
    }

    @Test
    void execute_FiltersByRadiusAndSortsByDistance() {
        // Seller at Guadalajara, Jalisco.
        when(userRepository.findUserById(11L)).thenReturn(Optional.of(sellerWithLocation(1L)));
        Location sellerLoc = new Location();
        sellerLoc.setLocationId(1L);
        sellerLoc.setCoordinates(point(20.6597, -103.3496));
        when(locationRepository.findLocationById(1L)).thenReturn(Optional.of(sellerLoc));

        when(alertaRepository.findValidatedSince(any(LocalDateTime.class))).thenReturn(List.of(
                alertaAt(1L, 20.70, -103.30),   // ~7 km — within 100
                alertaAt(2L, 20.66, -103.40),   // ~5 km — within 100 (closest)
                alertaAt(3L, 19.4138, -102.0558) // Morelia, ~200 km — excluded
        ));

        List<GetEarlyAlertaResponseDto> result = useCase.execute(11L, 100.0);

        assertEquals(2, result.size());
        // Sorted ascending by distance: alert 2 (closest) first.
        assertEquals(2L, result.get(0).alertaId);
        assertTrue(result.get(0).distanceKm <= result.get(1).distanceKm);
        // Excluded the far one.
        assertTrue(result.stream().noneMatch(a -> a.alertaId == 3L));
    }

    @Test
    void execute_DefaultsRadiusWhenNullOrNonPositive() {
        when(userRepository.findUserById(11L)).thenReturn(Optional.of(sellerWithLocation(1L)));
        Location sellerLoc = new Location();
        sellerLoc.setLocationId(1L);
        sellerLoc.setCoordinates(point(20.6597, -103.3496));
        when(locationRepository.findLocationById(1L)).thenReturn(Optional.of(sellerLoc));
        when(alertaRepository.findValidatedSince(any(LocalDateTime.class)))
                .thenReturn(List.of(alertaAt(1L, 20.70, -103.30)));

        // null radius → default 100 km, the ~7km alert is included.
        assertEquals(1, useCase.execute(11L, null).size());
    }

    @Test
    void execute_WhenUserHasNoLocation_ThrowsIllegalArgument() {
        User user = new User();
        user.setUserId(11L);
        user.setLocation(null);
        when(userRepository.findUserById(11L)).thenReturn(Optional.of(user));

        assertThrows(IllegalArgumentException.class, () -> useCase.execute(11L, 100.0));
    }

    @Test
    void execute_InvalidUserId_ThrowsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(0L, 100.0));
    }
}
