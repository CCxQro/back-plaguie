package itesm.mx.application.usecase;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import itesm.mx.application.usecase.users.DeactivateUserUseCase;
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
class DeactivateUserUseCaseIntegrationTest {

    @Inject
    DeactivateUserUseCase deactivateUserUseCase;

    @Inject
    UserRepositoryImpl userRepository;

    @InjectMock
    FirebaseUserManager firebaseUserManager;

    @InjectMock
    FirebaseTokenVerifier firebaseTokenVerifier;

    private Long activeUserId;
    private Long inactiveUserId;

    @BeforeEach
    @Transactional
    void setup() {
        userRepository.delete("email like 'deactivate.test%'");

        UserEntity active = new UserEntity();
        active.firebaseUuid = "uid-deactivate-active";
        active.name = "Active User";
        active.email = "deactivate.test.active@itesm.mx";
        active.roleId = 2;
        active.isActive = true;
        userRepository.persist(active);
        activeUserId = active.userId;

        UserEntity alreadyInactive = new UserEntity();
        alreadyInactive.firebaseUuid = "uid-deactivate-inactive";
        alreadyInactive.name = "Already Inactive User";
        alreadyInactive.email = "deactivate.test.inactive@itesm.mx";
        alreadyInactive.roleId = 3;
        alreadyInactive.isActive = false;
        userRepository.persist(alreadyInactive);
        inactiveUserId = alreadyInactive.userId;
    }

    @Test
    void execute_WhenUserIsActive_SetsIsActiveFalseInDb() {
        deactivateUserUseCase.execute(activeUserId);

        Optional<User> result = userRepository.findUserById(activeUserId);

        assertTrue(result.isPresent());
        assertFalse(result.get().getActive(),
                "isActive should be false after soft-delete");
    }

    @Test
    void execute_WhenUserIsAlreadyInactive_RemainsInactiveinDb() {
        deactivateUserUseCase.execute(inactiveUserId);

        Optional<User> result = userRepository.findUserById(inactiveUserId);

        assertTrue(result.isPresent());
        assertFalse(result.get().getActive(),
                "isActive should still be false after redundant soft-delete");
    }

    @Test
    void execute_WhenUserNotFound_ThrowsIllegalStateException() {
        long nonExistentId = 99999L;

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> deactivateUserUseCase.execute(nonExistentId)
        );

        assertTrue(ex.getMessage().contains(String.valueOf(nonExistentId)));
    }

    @Test
    void execute_WhenUserIdIsNull_ThrowsIllegalArgumentException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> deactivateUserUseCase.execute(null)
        );
    }

    @Test
    void execute_WhenUserIdIsZero_ThrowsIllegalArgumentException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> deactivateUserUseCase.execute(0L)
        );
    }

    @Test
    void execute_WhenUserIdIsNegative_ThrowsIllegalArgumentException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> deactivateUserUseCase.execute(-1L)
        );
    }

    @Test
    void execute_DoesNotRemoveUserFromDb_OnlySetsFlag() {
        deactivateUserUseCase.execute(activeUserId);

        Optional<User> result = userRepository.findUserById(activeUserId);

        assertTrue(result.isPresent(), "User record must still exist after soft-delete");
        assertEquals("uid-deactivate-active", result.get().getFirebaseUuid());
        assertEquals("deactivate.test.active@itesm.mx", result.get().getEmail());
    }
}
