package itesm.mx.application.usecase;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import itesm.mx.application.dto.GetUserResponseDto;
import itesm.mx.application.dto.UpdateUserDto;
import itesm.mx.application.usecase.users.UpdateUserUseCase;
import itesm.mx.domain.models.user.User;
import itesm.mx.infrastructure.firebase.FirebaseTokenVerifier;
import itesm.mx.infrastructure.firebase.FirebaseUserManager;
import itesm.mx.infrastructure.persistence.entity.users.UserEntity;
import itesm.mx.infrastructure.persistence.repository.user.UserRepositoryImpl;
import jakarta.inject.Inject;
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

    @InjectMock
    FirebaseUserManager firebaseUserManager;

    @InjectMock
    FirebaseTokenVerifier firebaseTokenVerifier;

    private Long adminUserId;
    private Long farmerUserId;

    @BeforeEach
    @Transactional
    void setup() {
        userRepository.delete("email like 'update.test%'");

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
    void execute_WhenRoleChanged_PersistsNewRoleToDb() {
        UpdateUserDto dto = new UpdateUserDto();
        dto.roleId = 3;

        GetUserResponseDto result = updateUserUseCase.execute(adminUserId, dto);

        assertEquals(3, result.roleId);

        Optional<User> fromDb = userRepository.findUserById(adminUserId);
        assertTrue(fromDb.isPresent());
        assertEquals(3, fromDb.get().getRoleId());
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
