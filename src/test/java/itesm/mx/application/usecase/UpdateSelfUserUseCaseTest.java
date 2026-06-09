package itesm.mx.application.usecase;

import itesm.mx.application.dto.GetLocationResponseDto;
import itesm.mx.application.dto.RegisterLocationDto;
import itesm.mx.application.dto.UpdateSelfUserDto;
import itesm.mx.application.usecase.location.location.RegisterLocationUseCase;
import itesm.mx.application.usecase.location.location.UpdateLocationUseCase;
import itesm.mx.application.usecase.users.UpdateSelfUserUseCase;
import itesm.mx.application.usecase.users.UserLocationEnricher;
import itesm.mx.domain.models.location.Location;
import itesm.mx.domain.models.location.LocationData;
import itesm.mx.domain.models.user.RoleConstants;
import itesm.mx.domain.models.user.User;
import itesm.mx.domain.repository.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateSelfUserUseCaseTest {

    @Mock UserRepository userRepository;
    @Mock RegisterLocationUseCase registerLocationUseCase;
    @Mock UpdateLocationUseCase updateLocationUseCase;
    @Mock UserLocationEnricher userLocationEnricher;

    @InjectMocks UpdateSelfUserUseCase updateSelfUserUseCase;

    private static final Long USER_ID = 5L;

    private User farmerWithoutLocation() {
        return new User(USER_ID, "uid-farmer", "Farmer", "farmer@itesm.mx", RoleConstants.FARMER, true);
    }

    private User farmerWithLocation(Long locationId) {
        User user = new User(USER_ID, "uid-farmer", "Farmer", "farmer@itesm.mx", RoleConstants.FARMER, true);
        Location loc = new Location();
        loc.setLocationId(locationId);
        user.setLocation(loc);
        return user;
    }

    private UpdateSelfUserDto dtoWithNameOnly(String name) {
        UpdateSelfUserDto dto = new UpdateSelfUserDto();
        dto.name = name;
        return dto;
    }

    private UpdateSelfUserDto dtoWithLocation() {
        UpdateSelfUserDto dto = new UpdateSelfUserDto();
        RegisterLocationDto loc = new RegisterLocationDto();
        loc.latitude = 20.67;
        loc.longitude = -103.35;
        loc.stateName = "Jalisco";
        loc.municipalityName = "Guadalajara";
        loc.localityName = "Centro";
        loc.propertyName = "Predio Norte";
        dto.location = loc;
        return dto;
    }

    // ── Happy path: solo nombre ───────────────────────────────────────────────

    @Test
    void execute_WhenOnlyNameProvided_UpdatesNameWithoutTouchingLocation() {
        User existing = farmerWithLocation(10L);
        User updated = new User(USER_ID, "uid-farmer", "Nuevo Nombre", "farmer@itesm.mx", RoleConstants.FARMER, true);
        updated.setLocation(existing.getLocation());

        when(userRepository.findUserById(USER_ID)).thenReturn(Optional.of(existing));
        when(userRepository.update(any(User.class))).thenReturn(updated);

        updateSelfUserUseCase.execute(USER_ID, dtoWithNameOnly("Nuevo Nombre"));

        verify(registerLocationUseCase, never()).execute(any(LocationData.class));
        verify(updateLocationUseCase, never()).execute(anyLong(), any(LocationData.class));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).update(captor.capture());
        assertEquals("Nuevo Nombre", captor.getValue().getName());
        assertNull(captor.getValue().getLocation(),
                "Cuando no se envía location no se debe sobrescribir la ubicación existente");
    }

    // ── Happy path: con ubicación nueva ──────────────────────────────────────

    @Test
    void execute_WhenUserHasNoLocationAndLocationProvided_RegistersNewLocation() {
        Long newLocationId = 99L;
        when(userRepository.findUserById(USER_ID)).thenReturn(Optional.of(farmerWithoutLocation()));
        when(registerLocationUseCase.execute(any(LocationData.class)))
                .thenReturn(locationResponse(newLocationId));
        User updated = farmerWithLocation(newLocationId);
        when(userRepository.update(any(User.class))).thenReturn(updated);

        updateSelfUserUseCase.execute(USER_ID, dtoWithLocation());

        verify(registerLocationUseCase).execute(any(LocationData.class));
        verify(updateLocationUseCase, never()).execute(anyLong(), any(LocationData.class));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).update(captor.capture());
        assertNotNull(captor.getValue().getLocation());
        assertEquals(newLocationId, captor.getValue().getLocation().getLocationId());
    }

    // ── Happy path: actualizar ubicación existente ────────────────────────────

    @Test
    void execute_WhenUserHasExistingLocationAndLocationProvided_UpdatesLocationInPlace() {
        Long existingLocationId = 7L;
        when(userRepository.findUserById(USER_ID)).thenReturn(Optional.of(farmerWithLocation(existingLocationId)));
        when(updateLocationUseCase.execute(eq(existingLocationId), any(LocationData.class)))
                .thenReturn(locationResponse(existingLocationId));
        when(userRepository.update(any(User.class))).thenReturn(farmerWithLocation(existingLocationId));

        updateSelfUserUseCase.execute(USER_ID, dtoWithLocation());

        verify(updateLocationUseCase).execute(eq(existingLocationId), any(LocationData.class));
        verify(registerLocationUseCase, never()).execute(any(LocationData.class));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).update(captor.capture());
        assertNull(captor.getValue().getLocation(),
                "Al actualizar una ubicación existente, userToUpdate no debe cambiar la FK de ubicación");
    }

    // ── Validaciones de entrada ───────────────────────────────────────────────

    @Test
    void execute_WhenUserIdIsNull_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> updateSelfUserUseCase.execute(null, new UpdateSelfUserDto()));
    }

    @Test
    void execute_WhenUserIdIsZero_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> updateSelfUserUseCase.execute(0L, new UpdateSelfUserDto()));
    }

    @Test
    void execute_WhenDtoIsNull_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> updateSelfUserUseCase.execute(USER_ID, null));
    }

    @Test
    void execute_WhenNameIsBlank_ThrowsIllegalArgumentException() {
        UpdateSelfUserDto dto = new UpdateSelfUserDto();
        dto.name = "   ";
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> updateSelfUserUseCase.execute(USER_ID, dto));
        assertEquals("El nombre no puede estar vacío", ex.getMessage());
    }

    @Test
    void execute_WhenUserNotFound_ThrowsIllegalStateException() {
        when(userRepository.findUserById(USER_ID)).thenReturn(Optional.empty());
        assertThrows(IllegalStateException.class,
                () -> updateSelfUserUseCase.execute(USER_ID, new UpdateSelfUserDto()));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private GetLocationResponseDto locationResponse(Long locationId) {
        return new GetLocationResponseDto(
                locationId, 20.67, -103.35,
                1L, "Jalisco",
                2L, "Guadalajara",
                3L, "Centro",
                4L, "Predio Norte"
        );
    }
}
