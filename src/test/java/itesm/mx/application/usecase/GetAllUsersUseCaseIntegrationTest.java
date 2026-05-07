package itesm.mx.application.usecase;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import itesm.mx.application.dto.GetUserResponseDto;
import itesm.mx.application.usecase.users.GetAllUsersUseCase;
import itesm.mx.infrastructure.firebase.FirebaseTokenVerifier;
import itesm.mx.infrastructure.firebase.FirebaseUserManager;
import itesm.mx.infrastructure.persistence.entity.users.UserEntity;
import itesm.mx.infrastructure.persistence.repository.user.UserRepositoryImpl;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestProfile(UseCaseIntegrationTestProfile.class)
class GetAllUsersUseCaseIntegrationTest {

    @Inject
    GetAllUsersUseCase getAllUsersUseCase;

    @Inject
    UserRepositoryImpl userRepository;

    @InjectMock
    FirebaseUserManager firebaseUserManager;

    @InjectMock
    FirebaseTokenVerifier firebaseTokenVerifier;

    @BeforeEach
    @Transactional
    void setup() {
        userRepository.deleteAll();

        UserEntity admin = new UserEntity();
        admin.firebaseUuid = "uid-getall-admin";
        admin.name = "Admin User";
        admin.email = "getall.admin@itesm.mx";
        admin.roleId = 1;
        admin.isActive = true;
        userRepository.persist(admin);

        UserEntity farmer = new UserEntity();
        farmer.firebaseUuid = "uid-getall-farmer";
        farmer.name = "Farmer User";
        farmer.email = "getall.farmer@itesm.mx";
        farmer.roleId = 2;
        farmer.isActive = true;
        userRepository.persist(farmer);

        UserEntity inactive = new UserEntity();
        inactive.firebaseUuid = "uid-getall-inactive";
        inactive.name = "Inactive User";
        inactive.email = "getall.inactive@itesm.mx";
        inactive.roleId = 3;
        inactive.isActive = false;
        userRepository.persist(inactive);
    }

    @Test
    void execute_ReturnsAllPersistedUsers_IncludingInactive() {
        List<GetUserResponseDto> result = getAllUsersUseCase.execute();

        assertEquals(3, result.size());
    }

    @Test
    void execute_ReturnedDtosContainCorrectFields() {
        List<GetUserResponseDto> result = getAllUsersUseCase.execute();

        GetUserResponseDto adminDto = result.stream()
                .filter(u -> "uid-getall-admin".equals(u.firebaseUuid))
                .findFirst()
                .orElseThrow();

        assertEquals("Admin User", adminDto.name);
        assertEquals("getall.admin@itesm.mx", adminDto.email);
        assertEquals(1, adminDto.roleId);
        assertTrue(adminDto.isActive);
    }

    @Test
    void execute_IncludesInactiveUsers() {
        List<GetUserResponseDto> result = getAllUsersUseCase.execute();

        long inactiveCount = result.stream().filter(u -> Boolean.FALSE.equals(u.isActive)).count();
        assertEquals(1, inactiveCount);
    }

    @Test
    @Transactional
    void execute_WhenNoUsersExist_ReturnsEmptyList() {
        userRepository.deleteAll();

        List<GetUserResponseDto> result = getAllUsersUseCase.execute();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
