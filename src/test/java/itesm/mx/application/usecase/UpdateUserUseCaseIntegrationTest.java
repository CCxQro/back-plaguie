package itesm.mx.application.usecase;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import itesm.mx.application.dto.GetUserResponseDto;
import itesm.mx.application.dto.UpdateUserDto;
import itesm.mx.application.usecase.users.UpdateUserUseCase;
import itesm.mx.domain.models.user.Administrator;
import itesm.mx.domain.models.user.TechnicalSeller;
import itesm.mx.domain.models.user.User;
import itesm.mx.infrastructure.firebase.FirebaseTokenVerifier;
import itesm.mx.infrastructure.firebase.FirebaseUserManager;
import itesm.mx.infrastructure.persistence.entity.location.LocalityEntity;
import itesm.mx.infrastructure.persistence.entity.location.LocationEntity;
import itesm.mx.infrastructure.persistence.entity.location.MunicipalityEntity;
import itesm.mx.infrastructure.persistence.entity.location.PropertyEntity;
import itesm.mx.infrastructure.persistence.entity.location.StateEntity;
import itesm.mx.infrastructure.persistence.entity.users.AdministratorEntity;
import itesm.mx.infrastructure.persistence.entity.users.TechnicalSellerEntity;
import itesm.mx.infrastructure.persistence.entity.users.UserEntity;
import itesm.mx.infrastructure.persistence.repository.user.AdministratorRepositoryImpl;
import itesm.mx.infrastructure.persistence.repository.user.TechnicalSellerRepositoryImpl;
import itesm.mx.infrastructure.persistence.repository.user.UserRepositoryImpl;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestProfile(UseCaseIntegrationTestProfile.class)
class UpdateUserUseCaseIntegrationTest {

    @Inject
    UpdateUserUseCase updateUserUseCase;

    @Inject
    UserRepositoryImpl userRepository;

    @Inject
    AdministratorRepositoryImpl administratorRepository;

    @Inject
    TechnicalSellerRepositoryImpl technicalSellerRepository;

    @Inject
    EntityManager em;

    @InjectMock
    FirebaseUserManager firebaseUserManager;

    @InjectMock
    FirebaseTokenVerifier firebaseTokenVerifier;

    private Long adminUserId;
    private Long farmerUserId;
    private Long sellerUserId;
    private Long adminWithSellerHistoryUserId;
    private Long historySellerProfileId;

    @BeforeEach
    @Transactional
    void setup() {
        em.createQuery("delete from FarmerEntity f where f.userId in "
                + "(select u.userId from UserEntity u where u.email like 'update.test%')").executeUpdate();
        em.createQuery("delete from TechnicalSellerEntity t where t.userId in "
                + "(select u.userId from UserEntity u where u.email like 'update.test%')").executeUpdate();
        em.createQuery("delete from AdministratorEntity a where a.userId in "
                + "(select u.userId from UserEntity u where u.email like 'update.test%')").executeUpdate();
        em.createQuery("delete from UserEntity u where u.email like 'update.test%'").executeUpdate();

        UserEntity admin = new UserEntity();
        admin.firebaseUuid = "uid-update-admin";
        admin.name = "Original Admin Name";
        admin.email = "update.test.admin@itesm.mx";
        admin.roleId = 1;
        admin.isActive = true;
        userRepository.persist(admin);
        adminUserId = admin.userId;

        UserEntity farmer = new UserEntity();
        farmer.firebaseUuid = "uid-update-farmer";
        farmer.name = "Original Farmer Name";
        farmer.email = "update.test.farmer@itesm.mx";
        farmer.roleId = 2;
        farmer.isActive = true;
        userRepository.persist(farmer);
        farmerUserId = farmer.userId;

        // Seller with an active Tecnico_Vendedor profile, no Administrador profile.
        UserEntity seller = new UserEntity();
        seller.firebaseUuid = "uid-update-seller";
        seller.name = "Original Seller Name";
        seller.email = "update.test.seller@itesm.mx";
        seller.roleId = 3;
        seller.isActive = true;
        userRepository.persist(seller);
        sellerUserId = seller.userId;

        TechnicalSellerEntity sellerProfile = new TechnicalSellerEntity();
        sellerProfile.userId = seller.userId;
        sellerProfile.isActive = true;
        em.persist(sellerProfile);

        // Admin that previously held the seller role: active Administrador profile
        // plus an inactive Tecnico_Vendedor profile left over from that earlier stint.
        UserEntity adminWithHistory = new UserEntity();
        adminWithHistory.firebaseUuid = "uid-update-admin-history";
        adminWithHistory.name = "Admin With Seller History";
        adminWithHistory.email = "update.test.history@itesm.mx";
        adminWithHistory.roleId = 1;
        adminWithHistory.isActive = true;
        userRepository.persist(adminWithHistory);
        adminWithSellerHistoryUserId = adminWithHistory.userId;

        AdministratorEntity adminProfile = new AdministratorEntity();
        adminProfile.userId = adminWithHistory.userId;
        adminProfile.isActive = true;
        em.persist(adminProfile);

        TechnicalSellerEntity oldSellerProfile = new TechnicalSellerEntity();
        oldSellerProfile.userId = adminWithHistory.userId;
        oldSellerProfile.isActive = false;
        em.persist(oldSellerProfile);
        historySellerProfileId = oldSellerProfile.technicalSellerId;
    }

    // Seeds a location row. Coordinates are left null on purpose: the H2 test
    // profile cannot round-trip JTS geometry, and the role-change flow never
    // reads the coordinates anyway.
    private Long seedLocation() {
        StateEntity state = new StateEntity();
        state.name = "update-test-state";
        em.persist(state);

        MunicipalityEntity municipality = new MunicipalityEntity();
        municipality.name = "update-test-municipality";
        em.persist(municipality);

        LocalityEntity locality = new LocalityEntity();
        locality.name = "update-test-locality";
        em.persist(locality);

        PropertyEntity property = new PropertyEntity();
        property.name = "update-test-property";
        em.persist(property);

        LocationEntity location = new LocationEntity();
        location.stateId = state.stateId;
        location.municipalityId = municipality.municipalityId;
        location.localityId = locality.localityId;
        location.propertyId = property.propertyId;
        em.persist(location);
        return location.locationId;
    }

    @Test
    void execute_WhenNameChanged_PersistsNewNameToDb() {
        UpdateUserDto dto = new UpdateUserDto();
        dto.name = "Updated Admin Name";

        GetUserResponseDto result = updateUserUseCase.execute(adminUserId, dto);

        assertEquals("Updated Admin Name", result.name);

        Optional<User> fromDb = userRepository.findUserById(adminUserId);
        assertTrue(fromDb.isPresent());
        assertEquals("Updated Admin Name", fromDb.get().getName());
    }

    @Test
    void execute_WhenSellerBecomesAdmin_CreatesAdminProfileAndDeactivatesSellerProfile() {
        UpdateUserDto dto = new UpdateUserDto();
        dto.roleId = 1;

        GetUserResponseDto result = updateUserUseCase.execute(sellerUserId, dto);

        assertEquals(1, result.roleId);

        Optional<Administrator> adminProfile = administratorRepository.findByIdUser(sellerUserId);
        assertTrue(adminProfile.isPresent(), "A new Administrador profile should be created");
        assertTrue(adminProfile.get().getActive());

        Optional<TechnicalSeller> sellerProfile = technicalSellerRepository.findByIdUser(sellerUserId);
        assertTrue(sellerProfile.isPresent());
        assertFalse(sellerProfile.get().getActive(), "The previous Tecnico_Vendedor profile should be deactivated");
    }

    @Test
    void execute_WhenRoleChangedToPreviouslyHeldRole_ReactivatesExistingProfile() {
        UpdateUserDto dto = new UpdateUserDto();
        dto.roleId = 3;
        // No location supplied — the user already has a seller profile, so none is required.

        updateUserUseCase.execute(adminWithSellerHistoryUserId, dto);

        Optional<TechnicalSeller> sellerProfile =
                technicalSellerRepository.findByIdUser(adminWithSellerHistoryUserId);
        assertTrue(sellerProfile.isPresent());
        assertTrue(sellerProfile.get().getActive(), "The previously held seller profile should be reactivated");
        assertEquals(historySellerProfileId, sellerProfile.get().getTechnicalSellerId(),
                "Reactivation must reuse the existing profile, not create a new one");
        assertEquals(1, technicalSellerRepository.count("userId", adminWithSellerHistoryUserId));

        Optional<Administrator> adminProfile =
                administratorRepository.findByIdUser(adminWithSellerHistoryUserId);
        assertTrue(adminProfile.isPresent());
        assertFalse(adminProfile.get().getActive(), "The previous Administrador profile should be deactivated");
    }

    @Test
    void execute_WhenIsActiveChanged_PersistsFlagToDb() {
        UpdateUserDto dto = new UpdateUserDto();
        dto.isActive = false;

        GetUserResponseDto result = updateUserUseCase.execute(adminUserId, dto);

        assertFalse(result.isActive);

        Optional<User> fromDb = userRepository.findUserById(adminUserId);
        assertTrue(fromDb.isPresent());
        assertFalse(fromDb.get().getActive());
    }

    @Test
    void execute_WhenFarmerRoleChangedToNonFarmer_ThrowsIllegalArgumentException() {
        UpdateUserDto dto = new UpdateUserDto();
        dto.roleId = 1;

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> updateUserUseCase.execute(farmerUserId, dto)
        );

        assertTrue(ex.getMessage().contains("Agricultor"));
    }

    @Test
    void execute_WhenNullFields_DoesNotOverwriteExistingValues() {
        UpdateUserDto dto = new UpdateUserDto();
        // all fields null — nothing should change

        GetUserResponseDto result = updateUserUseCase.execute(adminUserId, dto);

        assertEquals("Original Admin Name", result.name);
        assertEquals(1, result.roleId);
    }

    @Test
    void execute_WhenBlankName_ThrowsIllegalArgumentException() {
        UpdateUserDto dto = new UpdateUserDto();
        dto.name = "   ";

        assertThrows(IllegalArgumentException.class, () -> updateUserUseCase.execute(adminUserId, dto));
    }

    @Test
    void execute_WhenInvalidRoleId_ThrowsIllegalArgumentException() {
        UpdateUserDto dto = new UpdateUserDto();
        dto.roleId = 99;

        assertThrows(IllegalArgumentException.class, () -> updateUserUseCase.execute(adminUserId, dto));
    }

    @Test
    void execute_WhenUserNotFound_ThrowsIllegalStateException() {
        UpdateUserDto dto = new UpdateUserDto();
        dto.name = "Ghost";

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> updateUserUseCase.execute(77777L, dto)
        );

        assertTrue(ex.getMessage().contains("77777"));
    }

    @Test
    void execute_WhenUserIdIsNull_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> updateUserUseCase.execute(null, new UpdateUserDto()));
    }

    @Test
    void execute_WhenUserIdIsZero_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> updateUserUseCase.execute(0L, new UpdateUserDto()));
    }
}
