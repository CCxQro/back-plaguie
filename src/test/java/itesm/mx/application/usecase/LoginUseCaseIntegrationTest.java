package itesm.mx.application.usecase;

import com.google.firebase.auth.FirebaseAuthException;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import itesm.mx.application.dto.LoginDto;
import itesm.mx.application.dto.LoginResponseDto;
import itesm.mx.application.usecase.users.LoginUseCase;
import itesm.mx.infrastructure.firebase.FirebaseTokenVerifier;
import itesm.mx.infrastructure.persistence.entity.users.UserEntity;
import itesm.mx.infrastructure.persistence.repository.user.UserRepositoryImpl;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@QuarkusTest
@TestProfile(UseCaseIntegrationTestProfile.class)
class LoginUseCaseIntegrationTest {

    @Inject
    LoginUseCase loginUseCase;

    @Inject
    UserRepositoryImpl userRepository;

    @InjectMock
    FirebaseTokenVerifier firebaseTokenVerifier;

    @BeforeEach
    @Transactional
    void setup() {
        userRepository.delete("email", "login.integration@itesm.mx");

        UserEntity userEntity = new UserEntity();
        userEntity.firebaseUuid = "uid-login-integration";
        userEntity.name = "Login Integration User";
        userEntity.email = "login.integration@itesm.mx";
        userEntity.roleId = 2;
        userEntity.isActive = true;
        userRepository.persist(userEntity);
    }

    @Test
    void execute_WhenValidTokenAndUserExists_ReturnsUserData() throws FirebaseAuthException {
        LoginDto dto = new LoginDto();
        dto.firebaseToken = "valid-token-login";

        when(firebaseTokenVerifier.verifyTokenAndGetUid("valid-token-login"))
                .thenReturn("uid-login-integration");

        LoginResponseDto response = loginUseCase.execute(dto);

        assertNotNull(response);
        assertEquals("Login Integration User", response.name);
        assertEquals("login.integration@itesm.mx", response.email);
        assertEquals(2, response.roleId);
    }

    @Test
    void execute_WhenTokenIsInvalid_ThrowsSecurityException() throws FirebaseAuthException {
        LoginDto dto = new LoginDto();
        dto.firebaseToken = "invalid-token-login";

        FirebaseAuthException firebaseAuthException = mock(FirebaseAuthException.class);
        when(firebaseAuthException.getMessage()).thenReturn("token invalido");
        when(firebaseTokenVerifier.verifyTokenAndGetUid("invalid-token-login"))
                .thenThrow(firebaseAuthException);

        SecurityException ex = assertThrows(SecurityException.class, () -> loginUseCase.execute(dto));

        assertTrue(ex.getMessage().contains("La verificación del token de Firebase falló"));
    }

    @Test
    void execute_WhenUserIsNotRegistered_ThrowsSecurityException() throws FirebaseAuthException {
        LoginDto dto = new LoginDto();
        dto.firebaseToken = "valid-token-without-db-user";

        when(firebaseTokenVerifier.verifyTokenAndGetUid("valid-token-without-db-user"))
                .thenReturn("uid-missing-in-db");

        SecurityException ex = assertThrows(SecurityException.class, () -> loginUseCase.execute(dto));

        assertEquals("Usuario no encontrado en la base de datos con este UUID de Firebase", ex.getMessage());
    }
}
