package itesm.mx.application.usecase;

import itesm.mx.application.dto.GetLocationResponseDto;
import itesm.mx.application.dto.RegisterLocationDto;
import itesm.mx.application.dto.UpdateUserDto;
import itesm.mx.application.usecase.location.location.RegisterLocationUseCase;
import itesm.mx.application.usecase.location.location.UpdateLocationUseCase;
import itesm.mx.application.usecase.users.UpdateUserUseCase;
import itesm.mx.application.usecase.users.UserLocationEnricher;
import itesm.mx.domain.models.location.Location;
import itesm.mx.domain.models.location.LocationData;
import itesm.mx.domain.models.user.Farmer;
import itesm.mx.domain.models.user.RoleConstants;
import itesm.mx.domain.models.user.TechnicalSeller;
import itesm.mx.domain.models.user.User;
import itesm.mx.domain.repository.marketplace.ProductRepository;
import itesm.mx.domain.repository.parcela.ParcelaRepository;
import itesm.mx.domain.repository.user.AdministratorRepository;
import itesm.mx.domain.repository.user.FarmerRepository;
import itesm.mx.domain.repository.user.TechnicalSellerRepository;
import itesm.mx.domain.repository.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateUserUseCaseTest {

    @Mock UserRepository userRepository;
    @Mock FarmerRepository farmerRepository;
    @Mock ParcelaRepository parcelaRepository;
    @Mock TechnicalSellerRepository technicalSellerRepository;
    @Mock ProductRepository productRepository;
    @Mock AdministratorRepository administratorRepository;
    @Mock RegisterLocationUseCase registerLocationUseCase;
    @Mock UpdateLocationUseCase updateLocationUseCase;
    @Mock UserLocationEnricher userLocationEnricher;

    @InjectMocks UpdateUserUseCase updateUserUseCase;

    private static final Long USER_ID = 10L;

    private UpdateUserDto dtoWithLocationAndRole(Integer newRoleId) {
        UpdateUserDto dto = new UpdateUserDto();
        dto.roleId = newRoleId;
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

    private User adminWithLocation(Long locationId) {
        User user = new User(USER_ID, "uid-admin", "Old Admin", "admin@itesm.mx", RoleConstants.ADMIN, true);
        if (locationId != null) {
            Location location = new Location();
            location.setLocationId(locationId);
            user.setLocation(location);
        }
        return user;
    }

    private User echoUpdate(Long locationId) {
        User updated = new User(USER_ID, "uid-admin", "Old Admin", "admin@itesm.mx", RoleConstants.FARMER, true);
        if (locationId != null) {
            Location location = new Location();
            location.setLocationId(locationId);
            updated.setLocation(location);
        }
        return updated;
    }

    @Test
    void execute_WhenAdminWithoutLocationBecomesFarmer_RegistersNewLocationAndAssignsItToUser() {
        Long newLocationId = 42L;
        when(userRepository.findUserById(USER_ID)).thenReturn(Optional.of(adminWithLocation(null)));
        when(administratorRepository.findByIdUser(USER_ID)).thenReturn(Optional.empty());
        when(farmerRepository.findByIdUser(USER_ID)).thenReturn(Optional.empty());
        when(registerLocationUseCase.execute(any(LocationData.class))).thenReturn(locationResponse(newLocationId));
        when(userRepository.update(any(User.class))).thenReturn(echoUpdate(newLocationId));

        updateUserUseCase.execute(USER_ID, dtoWithLocationAndRole(RoleConstants.FARMER));

        verify(registerLocationUseCase).execute(any(LocationData.class));
        verify(updateLocationUseCase, never()).execute(anyLong(), any(LocationData.class));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).update(captor.capture());
        assertNotNull(captor.getValue().getLocation(),
                "userToUpdate must carry the newly registered location so UserRepository.update writes it onto the row");
        assertEquals(newLocationId, captor.getValue().getLocation().getLocationId());

        verify(farmerRepository).save(any(Farmer.class));
    }

    @Test
    void execute_WhenAdminWithExistingLocationBecomesFarmer_UpdatesLocationInPlaceAndKeepsSameId() {
        Long existingLocationId = 5L;
        when(userRepository.findUserById(USER_ID)).thenReturn(Optional.of(adminWithLocation(existingLocationId)));
        when(administratorRepository.findByIdUser(USER_ID)).thenReturn(Optional.empty());
        when(farmerRepository.findByIdUser(USER_ID)).thenReturn(Optional.empty());
        when(updateLocationUseCase.execute(eq(existingLocationId), any(LocationData.class)))
                .thenReturn(locationResponse(existingLocationId));
        when(userRepository.update(any(User.class))).thenReturn(echoUpdate(existingLocationId));

        updateUserUseCase.execute(USER_ID, dtoWithLocationAndRole(RoleConstants.FARMER));

        verify(updateLocationUseCase).execute(eq(existingLocationId), any(LocationData.class));
        verify(registerLocationUseCase, never()).execute(any(LocationData.class));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).update(captor.capture());
        assertNull(captor.getValue().getLocation(),
                "userToUpdate must NOT set a location — the user keeps the same id_ubicacion; only the Ubicacion row's columns change");

        verify(farmerRepository).save(any(Farmer.class));
    }

    @Test
    void execute_WhenAdminWithoutLocationBecomesFarmerAndNoLocationProvided_ThrowsAndPersistsNothing() {
        when(userRepository.findUserById(USER_ID)).thenReturn(Optional.of(adminWithLocation(null)));

        UpdateUserDto dto = new UpdateUserDto();
        dto.roleId = RoleConstants.FARMER;

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> updateUserUseCase.execute(USER_ID, dto)
        );

        assertTrue(ex.getMessage().toLowerCase().contains("ubicacion"));
        verify(registerLocationUseCase, never()).execute(any(LocationData.class));
        verify(updateLocationUseCase, never()).execute(anyLong(), any(LocationData.class));
        verify(userRepository, never()).update(any(User.class));
        verify(farmerRepository, never()).save(any(Farmer.class));
    }

    // ── Input validation ──────────────────────────────────────────────────────

    @Test
    void execute_WhenUserIdIsNull_ThrowsIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> updateUserUseCase.execute(null, new UpdateUserDto()));
        assertEquals("El ID de usuario no es válido", ex.getMessage());
    }

    @Test
    void execute_WhenUserIdIsZero_ThrowsIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> updateUserUseCase.execute(0L, new UpdateUserDto()));
        assertEquals("El ID de usuario no es válido", ex.getMessage());
    }

    @Test
    void execute_WhenDtoIsNull_ThrowsIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> updateUserUseCase.execute(USER_ID, null));
        assertEquals("El cuerpo de la solicitud es requerido", ex.getMessage());
    }

    @Test
    void execute_WhenNameIsBlank_ThrowsIllegalArgumentException() {
        UpdateUserDto dto = new UpdateUserDto();
        dto.name = "   ";
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> updateUserUseCase.execute(USER_ID, dto));
        assertEquals("El nombre no puede estar vacío", ex.getMessage());
    }

    @Test
    void execute_WhenRoleIdIsOutOfRange_ThrowsIllegalArgumentException() {
        UpdateUserDto dto = new UpdateUserDto();
        dto.roleId = 5;
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> updateUserUseCase.execute(USER_ID, dto));
        assertTrue(ex.getMessage().contains("El roleId debe ser"));
    }

    @Test
    void execute_WhenUserNotFound_ThrowsIllegalStateException() {
        when(userRepository.findUserById(USER_ID)).thenReturn(Optional.empty());
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> updateUserUseCase.execute(USER_ID, new UpdateUserDto()));
        assertTrue(ex.getMessage().contains(String.valueOf(USER_ID)));
    }

    @Test
    void execute_WhenFarmerRoleChangeAttempted_ThrowsIllegalArgumentException() {
        when(userRepository.findUserById(USER_ID))
                .thenReturn(Optional.of(userWithRole(USER_ID, RoleConstants.FARMER)));
        UpdateUserDto dto = new UpdateUserDto();
        dto.roleId = RoleConstants.ADMIN;
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> updateUserUseCase.execute(USER_ID, dto));
        assertEquals("No se puede cambiar el rol de un Agricultor", ex.getMessage());
    }

    // ── Seller deactivation cascade ───────────────────────────────────────────

    @Test
    void execute_WhenRoleChangesFromSeller_DeactivatesSellerAndSetsProductsInactive() {
        TechnicalSeller seller = sellerWithId(5L);
        when(userRepository.findUserById(USER_ID))
                .thenReturn(Optional.of(userWithRole(USER_ID, RoleConstants.SELLER)));
        when(technicalSellerRepository.findByIdUser(USER_ID)).thenReturn(Optional.of(seller));
        when(administratorRepository.findByIdUser(USER_ID)).thenReturn(Optional.empty());
        when(userRepository.update(any())).thenReturn(userWithRole(USER_ID, RoleConstants.ADMIN));

        UpdateUserDto dto = new UpdateUserDto();
        dto.roleId = RoleConstants.ADMIN; // ADMIN needs no location, so no location validation

        updateUserUseCase.execute(USER_ID, dto);

        assertFalse(seller.getActive());
        verify(technicalSellerRepository).update(seller);
        verify(productRepository).setActiveBySellerId(5L, false);
    }

    // ── Seller activation cascade ─────────────────────────────────────────────

    @Test
    void execute_WhenRoleChangesToSellerAndSellerExists_ActivatesSellerAndSetsProductsActive() {
        TechnicalSeller seller = sellerWithId(7L);
        seller.setActive(false);
        when(userRepository.findUserById(USER_ID))
                .thenReturn(Optional.of(adminWithLocation(10L)));
        when(administratorRepository.findByIdUser(USER_ID)).thenReturn(Optional.empty());
        when(technicalSellerRepository.findByIdUser(USER_ID)).thenReturn(Optional.of(seller));
        when(userRepository.update(any())).thenReturn(adminWithLocation(10L));

        UpdateUserDto dto = new UpdateUserDto();
        dto.roleId = RoleConstants.SELLER;

        updateUserUseCase.execute(USER_ID, dto);

        assertTrue(seller.getActive());
        verify(technicalSellerRepository).update(seller);
        verify(productRepository).setActiveBySellerId(7L, true);
    }

    @Test
    void execute_WhenRoleChangesToSellerAndNoSellerExists_CreatesNewSellerWithoutProductCall() {
        when(userRepository.findUserById(USER_ID))
                .thenReturn(Optional.of(adminWithLocation(10L)));
        when(administratorRepository.findByIdUser(USER_ID)).thenReturn(Optional.empty());
        when(technicalSellerRepository.findByIdUser(USER_ID)).thenReturn(Optional.empty());
        when(userRepository.update(any())).thenReturn(adminWithLocation(10L));

        UpdateUserDto dto = new UpdateUserDto();
        dto.roleId = RoleConstants.SELLER;

        updateUserUseCase.execute(USER_ID, dto);

        verify(technicalSellerRepository).save(any(TechnicalSeller.class));
        verifyNoInteractions(productRepository);
    }

    // ── Farmer activation cascade ─────────────────────────────────────────────

    @Test
    void execute_WhenRoleChangesToFarmerAndFarmerExists_ActivatesFarmerAndSetsParcelsActive() {
        Farmer farmer = farmerWithId(3L);
        farmer.setActive(false);
        when(userRepository.findUserById(USER_ID))
                .thenReturn(Optional.of(adminWithLocation(10L)));
        when(administratorRepository.findByIdUser(USER_ID)).thenReturn(Optional.empty());
        when(farmerRepository.findByIdUser(USER_ID)).thenReturn(Optional.of(farmer));
        when(userRepository.update(any())).thenReturn(adminWithLocation(10L));

        UpdateUserDto dto = new UpdateUserDto();
        dto.roleId = RoleConstants.FARMER;

        updateUserUseCase.execute(USER_ID, dto);

        assertTrue(farmer.getActive());
        verify(farmerRepository).update(farmer);
        verify(parcelaRepository).setActiveByFarmerId(3L, true);
    }

    // ── No role change ────────────────────────────────────────────────────────

    @Test
    void execute_WhenNoRoleChange_SkipsAllRoleRepositories() {
        User existing = adminWithLocation(10L);
        when(userRepository.findUserById(USER_ID)).thenReturn(Optional.of(existing));
        when(userRepository.update(any())).thenReturn(existing);

        UpdateUserDto dto = new UpdateUserDto();
        dto.name = "Nombre Actualizado";

        updateUserUseCase.execute(USER_ID, dto);

        verifyNoInteractions(technicalSellerRepository, productRepository,
                farmerRepository, parcelaRepository, administratorRepository);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private User userWithRole(Long id, Integer roleId) {
        User user = new User(id, "uid-" + id, "Usuario", "user@itesm.mx", roleId, true);
        return user;
    }

    private TechnicalSeller sellerWithId(Long id) {
        TechnicalSeller seller = new TechnicalSeller();
        seller.setTechnicalSellerId(id);
        seller.setActive(true);
        return seller;
    }

    private Farmer farmerWithId(Long id) {
        Farmer farmer = new Farmer();
        farmer.setFarmerId(id);
        farmer.setActive(true);
        return farmer;
    }

    private GetLocationResponseDto locationResponse(Long locationId) {
        return new GetLocationResponseDto(
                locationId, 20.67, -103.35,
                1L, "jalisco",
                2L, "guadalajara",
                3L, "centro",
                4L, "predio norte"
        );
    }
}
