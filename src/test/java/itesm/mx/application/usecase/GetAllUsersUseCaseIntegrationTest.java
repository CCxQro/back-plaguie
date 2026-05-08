package itesm.mx.application.usecase;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import itesm.mx.application.dto.UserPageResponseDto;
import itesm.mx.application.usecase.users.GetAllUsersUseCase;
import itesm.mx.infrastructure.firebase.FirebaseTokenVerifier;
import itesm.mx.infrastructure.firebase.FirebaseUserManager;
import itesm.mx.infrastructure.persistence.entity.users.UserEntity;
import itesm.mx.infrastructure.persistence.repository.user.UserRepositoryImpl;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

    // --- existing tests updated for new signature ---

    @Test
    void execute_ReturnsAllPersistedUsers_IncludingInactive() {
        UserPageResponseDto result = getAllUsersUseCase.execute(0, 10, null, null, null);

        assertEquals(3, result.content.size());
        assertEquals(3L, result.totalElements);
    }

    @Test
    void execute_ReturnedDtosContainCorrectFields() {
        UserPageResponseDto result = getAllUsersUseCase.execute(0, 10, null, null, null);

        var adminDto = result.content.stream()
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
        UserPageResponseDto result = getAllUsersUseCase.execute(0, 10, null, null, null);

        long inactiveCount = result.content.stream().filter(u -> Boolean.FALSE.equals(u.isActive)).count();
        assertEquals(1, inactiveCount);
    }

    @Test
    @Transactional
    void execute_WhenNoUsersExist_ReturnsEmptyContent() {
        userRepository.deleteAll();

        UserPageResponseDto result = getAllUsersUseCase.execute(0, 10, null, null, null);

        assertNotNull(result.content);
        assertTrue(result.content.isEmpty());
        assertEquals(0L, result.totalElements);
        assertEquals(0, result.totalPages);
    }

    // --- pagination ---

    @Test
    void execute_PaginationFirstPage_ReturnsPageSizeItems() {
        UserPageResponseDto result = getAllUsersUseCase.execute(0, 2, null, null, null);

        assertEquals(2, result.content.size());
        assertEquals(3L, result.totalElements);
        assertEquals(2, result.totalPages);
        assertEquals(0, result.page);
        assertEquals(2, result.size);
    }

    @Test
    void execute_PaginationSecondPage_ReturnsRemainingItems() {
        UserPageResponseDto result = getAllUsersUseCase.execute(1, 2, null, null, null);

        assertEquals(1, result.content.size());
        assertEquals(3L, result.totalElements);
    }

    @Test
    void execute_PaginationBeyondLastPage_ReturnsEmptyContent() {
        UserPageResponseDto result = getAllUsersUseCase.execute(99, 10, null, null, null);

        assertTrue(result.content.isEmpty());
        assertEquals(3L, result.totalElements);
    }

    // --- filter by name / email ---

    @Test
    void execute_FilterByName_ReturnsOnlyMatchingUser() {
        UserPageResponseDto result = getAllUsersUseCase.execute(0, 10, "Admin", null, null);

        assertEquals(1, result.content.size());
        assertEquals("uid-getall-admin", result.content.get(0).firebaseUuid);
        assertEquals(1L, result.totalElements);
    }

    @Test
    void execute_FilterByName_IsCaseInsensitive() {
        UserPageResponseDto result = getAllUsersUseCase.execute(0, 10, "admin", null, null);

        assertEquals(1, result.content.size());
    }

    @Test
    void execute_FilterByName_MatchesEmailField() {
        UserPageResponseDto result = getAllUsersUseCase.execute(0, 10, "getall.farmer", null, null);

        assertEquals(1, result.content.size());
        assertEquals("uid-getall-farmer", result.content.get(0).firebaseUuid);
    }

    @Test
    void execute_FilterByName_WhenNoMatch_ReturnsEmptyContent() {
        UserPageResponseDto result = getAllUsersUseCase.execute(0, 10, "nonexistent", null, null);

        assertTrue(result.content.isEmpty());
        assertEquals(0L, result.totalElements);
    }

    // --- filter by roleId ---

    @Test
    void execute_FilterByRoleId_ReturnsOnlyUsersWithThatRole() {
        UserPageResponseDto result = getAllUsersUseCase.execute(0, 10, null, 2, null);

        assertEquals(1, result.content.size());
        assertEquals(2, result.content.get(0).roleId);
    }

    @Test
    void execute_FilterByRoleId_WhenNoMatch_ReturnsEmptyContent() {
        UserPageResponseDto result = getAllUsersUseCase.execute(0, 10, null, 99, null);

        assertTrue(result.content.isEmpty());
    }

    // --- filter by isActive ---

    @Test
    void execute_FilterByIsActiveTrue_ReturnsOnlyActiveUsers() {
        UserPageResponseDto result = getAllUsersUseCase.execute(0, 10, null, null, true);

        assertEquals(2, result.content.size());
        assertTrue(result.content.stream().allMatch(u -> Boolean.TRUE.equals(u.isActive)));
    }

    @Test
    void execute_FilterByIsActiveFalse_ReturnsOnlyInactiveUsers() {
        UserPageResponseDto result = getAllUsersUseCase.execute(0, 10, null, null, false);

        assertEquals(1, result.content.size());
        assertFalse(result.content.get(0).isActive);
    }

    // --- combined filters ---

    @Test
    void execute_CombinedRoleIdAndIsActive_ReturnsMatchingSubset() {
        UserPageResponseDto result = getAllUsersUseCase.execute(0, 10, null, 1, true);

        assertEquals(1, result.content.size());
        assertEquals(1, result.content.get(0).roleId);
        assertTrue(result.content.get(0).isActive);
    }

    @Test
    void execute_CombinedNameAndRoleId_WhenNoMatch_ReturnsEmptyContent() {
        UserPageResponseDto result = getAllUsersUseCase.execute(0, 10, "farmer", 1, null);

        assertTrue(result.content.isEmpty());
    }

    // --- validation ---

    @Test
    void execute_WhenPageIsNegative_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> getAllUsersUseCase.execute(-1, 10, null, null, null));
    }

    @Test
    void execute_WhenSizeIsZero_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> getAllUsersUseCase.execute(0, 0, null, null, null));
    }
}
