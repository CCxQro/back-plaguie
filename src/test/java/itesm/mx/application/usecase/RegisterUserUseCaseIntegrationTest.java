package itesm.mx.application.usecase;

import com.google.firebase.auth.FirebaseAuthException;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import itesm.mx.application.dto.RegisterUserDto;
import itesm.mx.application.dto.RegisterUserResponseDto;
import itesm.mx.domain.models.user.User;
import itesm.mx.infrastructure.firebase.FirebaseUserManager;
import itesm.mx.infrastructure.persistence.entity.users.UserEntity;
import itesm.mx.application.usecase.users.RegisterUserUseCase;
import itesm.mx.infrastructure.persistence.repository.UserRepositoryImpl;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
@TestProfile(UseCaseIntegrationTestProfile.class)
class RegisterUserUseCaseIntegrationTest {

    @Inject
    RegisterUserUseCase registerUserUseCase;

    @Inject
    UserRepositoryImpl userRepository;

    @InjectMock
    FirebaseUserManager firebaseUserManager;

    @BeforeEach
    @Transactional
    void setup() {
        userRepository.delete("email", "register.integration@itesm.mx");
        userRepository.delete("email", "register.exists@itesm.mx");

        UserEntity existingUser = new UserEntity();
        existingUser.firebaseUuid = "uid-existing-register-integration";
        existingUser.name = "Existing Register User";
        existingUser.email = "register.exists@itesm.mx";
        existingUser.roleId = 1;
        existingUser.isActive = true;
        userRepository.persist(existingUser);
    }

    @Test
    void execute_WhenRequestIsValid_PersistsUserAndReturnsResponse() throws FirebaseAuthException {
        RegisterUserDto dto = new RegisterUserDto();
        dto.name = "Register Integration User";
        dto.email = "register.integration@itesm.mx";
        dto.password = "Password123!";
        dto.roleId = 2;

        when(firebaseUserManager.createFirebaseUser(dto.email, dto.password, dto.name))
                .thenReturn("uid-register-integration");
        when(firebaseUserManager.generateCustomToken("uid-register-integration"))
                .thenReturn("token-register-integration");

        RegisterUserResponseDto response = registerUserUseCase.execute(dto);

        assertNotNull(response);
        assertNotNull(response.userId);
        assertEquals("uid-register-integration", response.firebaseUuid);
        assertEquals(dto.name, response.name);
        assertEquals(dto.email, response.email);
        assertEquals(dto.roleId, response.roleId);
        assertEquals("token-register-integration", response.firebaseToken);

        Optional<User> persistedUser = userRepository.findByEmail(dto.email);
        assertTrue(persistedUser.isPresent());
        assertEquals("uid-register-integration", persistedUser.get().getFirebaseUuid());
    }

    @Test
    void execute_WhenEmailAlreadyExists_ThrowsIllegalStateException() {
        RegisterUserDto dto = new RegisterUserDto();
        dto.name = "Another User";
        dto.email = "register.exists@itesm.mx";
        dto.password = "Password123!";
        dto.roleId = 1;

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> registerUserUseCase.execute(dto));

        assertEquals("Ya existe un usuario registrado con este correo electrónico", ex.getMessage());
    }

    @Test
    void execute_WhenGenerateCustomTokenFails_RollsBackFirebaseUserAndThrowsSecurityException() throws FirebaseAuthException {
        RegisterUserDto dto = new RegisterUserDto();
        dto.name = "Rollback User";
        dto.email = "register.integration@itesm.mx";
        dto.password = "Password123!";
        dto.roleId = 2;

        when(firebaseUserManager.createFirebaseUser(dto.email, dto.password, dto.name))
                .thenReturn("uid-token-fail-integration");

        FirebaseAuthException firebaseAuthException = mock(FirebaseAuthException.class);
        when(firebaseAuthException.getMessage()).thenReturn("token generation failed");
        when(firebaseUserManager.generateCustomToken("uid-token-fail-integration"))
                .thenThrow(firebaseAuthException);

        SecurityException ex = assertThrows(SecurityException.class, () -> registerUserUseCase.execute(dto));

        assertTrue(ex.getMessage().contains("Error al generar token"));
        verify(firebaseUserManager).deleteFirebaseUser("uid-token-fail-integration");
    }

    @Test
    void execute_WhenFirebaseReturnsEmailAlreadyExists_ThrowsIllegalStateException() throws FirebaseAuthException {
        RegisterUserDto dto = new RegisterUserDto();
        dto.name = "Firebase Existing User";
        dto.email = "register.integration@itesm.mx";
        dto.password = "Password123!";
        dto.roleId = 2;

        FirebaseAuthException firebaseAuthException = mock(FirebaseAuthException.class);
        when(firebaseAuthException.getMessage()).thenReturn("EMAIL_ALREADY_EXISTS");
        when(firebaseUserManager.createFirebaseUser(anyString(), anyString(), anyString()))
                .thenThrow(firebaseAuthException);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> registerUserUseCase.execute(dto));

        assertEquals("El correo ya está registrado en Firebase", ex.getMessage());
    }
}
