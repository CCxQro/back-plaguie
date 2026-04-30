package itesm.mx.application.usecase;

import com.google.firebase.auth.FirebaseAuthException;
import itesm.mx.application.dto.RegisterUserDto;
import itesm.mx.application.dto.RegisterUserResponseDto;
import itesm.mx.domain.models.User;
import itesm.mx.domain.repository.UserRepository;
import itesm.mx.infrastructure.firebase.FirebaseUserManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterUserUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private FirebaseUserManager firebaseUserManager;

    @InjectMocks
    private RegisterUserUseCase registerUserUseCase;

    @Test
    void execute_WhenValidRequest_CreatesUserAndReturnsResponse() throws FirebaseAuthException {
        RegisterUserDto dto = new RegisterUserDto();
        dto.name = "Ana Lopez";
        dto.email = "ana@example.com";
        dto.password = "Password123!";
        dto.roleId = 2;

        when(userRepository.findByEmail(dto.email)).thenReturn(Optional.empty());
        when(firebaseUserManager.createFirebaseUser(dto.email, dto.password, dto.name)).thenReturn("uid_abc_123");

        User saved = new User(42L, "uid_abc_123", dto.name, dto.email, dto.roleId, true);
        when(userRepository.save(any(User.class))).thenReturn(saved);

        when(firebaseUserManager.generateCustomToken("uid_abc_123")).thenReturn("custom_token_xyz");

        RegisterUserResponseDto response = registerUserUseCase.execute(dto);

        assertNotNull(response);
        assertEquals(42L, response.userId);
        assertEquals("uid_abc_123", response.firebaseUuid);
        assertEquals(dto.name, response.name);
        assertEquals(dto.email, response.email);
        assertEquals(dto.roleId, response.roleId);
        assertEquals("custom_token_xyz", response.firebaseToken);

        verify(userRepository).findByEmail(dto.email);
        verify(firebaseUserManager).createFirebaseUser(dto.email, dto.password, dto.name);
        verify(userRepository).save(any(User.class));
        verify(firebaseUserManager).generateCustomToken("uid_abc_123");
    }

    @Test
    void execute_WhenDtoIsNull_ThrowsIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> registerUserUseCase.execute(null));
        assertEquals("El cuerpo de la solicitud es requerido", ex.getMessage());
    }

    @Test
    void execute_WhenEmailAlreadyExists_ThrowsIllegalStateException() {
        RegisterUserDto dto = new RegisterUserDto();
        dto.name = "X";
        dto.email = "exist@example.com";
        dto.password = "p";
        dto.roleId = 1;

        when(userRepository.findByEmail(dto.email)).thenReturn(Optional.of(new User()));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> registerUserUseCase.execute(dto));
        assertEquals("Ya existe un usuario registrado con este correo electrónico", ex.getMessage());
    }

    @Test
    void execute_WhenFirebaseCreateThrows_EmailExists_MapsToIllegalStateException() throws FirebaseAuthException {
        RegisterUserDto dto = new RegisterUserDto();
        dto.name = "Y";
        dto.email = "y@example.com";
        dto.password = "p";
        dto.roleId = 1;

        when(userRepository.findByEmail(dto.email)).thenReturn(Optional.empty());

        FirebaseAuthException mockEx = mock(FirebaseAuthException.class);
        when(mockEx.getMessage()).thenReturn("EMAIL_EXISTS");
        when(firebaseUserManager.createFirebaseUser(anyString(), anyString(), anyString())).thenThrow(mockEx);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> registerUserUseCase.execute(dto));
        assertEquals("El correo ya está registrado en Firebase", ex.getMessage());
    }

    @Test
    void execute_WhenSaveThrows_RollsBackAndThrowsIllegalStateException() throws FirebaseAuthException {
        RegisterUserDto dto = new RegisterUserDto();
        dto.name = "Z";
        dto.email = "z@example.com";
        dto.password = "p";
        dto.roleId = 1;

        when(userRepository.findByEmail(dto.email)).thenReturn(Optional.empty());
        when(firebaseUserManager.createFirebaseUser(anyString(), anyString(), anyString())).thenReturn("uid_to_rollback");
        when(userRepository.save(any(User.class))).thenThrow(new RuntimeException("DB error"));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> registerUserUseCase.execute(dto));
        assertTrue(ex.getMessage().contains("No se pudo guardar el usuario en la base de datos"));

        verify(firebaseUserManager).deleteFirebaseUser("uid_to_rollback");
    }

    @Test
    void execute_WhenGenerateTokenFails_RollsBackAndThrowsSecurityException() throws FirebaseAuthException {
        RegisterUserDto dto = new RegisterUserDto();
        dto.name = "W";
        dto.email = "w@example.com";
        dto.password = "p";
        dto.roleId = 1;

        when(userRepository.findByEmail(dto.email)).thenReturn(Optional.empty());
        when(firebaseUserManager.createFirebaseUser(anyString(), anyString(), anyString())).thenReturn("uid_gen_fail");

        User saved = new User(5L, "uid_gen_fail", dto.name, dto.email, dto.roleId, true);
        when(userRepository.save(any(User.class))).thenReturn(saved);

        FirebaseAuthException mockEx = mock(FirebaseAuthException.class);
        when(mockEx.getMessage()).thenReturn("some error generating token");
        when(firebaseUserManager.generateCustomToken("uid_gen_fail")).thenThrow(mockEx);

        SecurityException ex = assertThrows(SecurityException.class, () -> registerUserUseCase.execute(dto));
        assertTrue(ex.getMessage().contains("Error al generar token"));

        verify(firebaseUserManager).deleteFirebaseUser("uid_gen_fail");
    }
}
