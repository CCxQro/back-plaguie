package itesm.mx.application.usecase;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import itesm.mx.application.dto.GetUserResponseDto;
import itesm.mx.application.usecase.users.GetUserByIdUseCase;
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
class GetUserByIdUseCaseIntegrationTest {

    @Inject
    GetUserByIdUseCase getUserByIdUseCase;

    @Inject
    UserRepositoryImpl userRepository;

    @InjectMock
    FirebaseUserManager firebaseUserManager;

    @InjectMock
    FirebaseTokenVerifier firebaseTokenVerifier;

    private Long existingUserId;

    @BeforeEach
    @Transactional
    void setup() {
        userRepository.delete("email like 'getbyid.test%'");

        UserEntity user = new UserEntity();
        user.firebaseUuid = "uid-getbyid-test";
        user.name = "Get By Id User";
        user.email = "getbyid.test@itesm.mx";
        user.roleId = 2;
        user.isActive = true;
        userRepository.persist(user);
        existingUserId = user.userId;
    }

    @Test
    void execute_WhenUserExists_ReturnsDtoWithCorrectFields() {
        GetUserResponseDto result = getUserByIdUseCase.execute(existingUserId);

        assertNotNull(result);
        assertEquals(existingUserId, result.userId);
        assertEquals("uid-getbyid-test", result.firebaseUuid);
        assertEquals("Get By Id User", result.name);
        assertEquals("getbyid.test@itesm.mx", result.email);
        assertEquals(2, result.roleId);
        assertTrue(result.isActive);
    }

    @Test
    void execute_WhenUserNotFound_ThrowsIllegalStateException() {
        long nonExistentId = 88888L;

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> getUserByIdUseCase.execute(nonExistentId)
        );

        assertTrue(ex.getMessage().contains(String.valueOf(nonExistentId)));
    }

    @Test
    void execute_WhenUserIdIsNull_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> getUserByIdUseCase.execute(null));
    }

    @Test
    void execute_WhenUserIdIsZero_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> getUserByIdUseCase.execute(0L));
    }

    @Test
    void execute_WhenUserIdIsNegative_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> getUserByIdUseCase.execute(-5L));
    }
}
