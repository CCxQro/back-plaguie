package itesm.mx.application.usecase;

import com.google.firebase.auth.FirebaseAuthException;
import itesm.mx.application.dto.LoginDto;
import itesm.mx.application.dto.LoginResponseDto;
import itesm.mx.domain.models.User;
import itesm.mx.domain.repository.UserRepository;
import itesm.mx.infrastructure.firebase.FirebaseTokenVerifier;
import jakarta.enterprise.inject.Instance;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private Instance<FirebaseTokenVerifier> firebaseTokenVerifierInstance;

    @Mock
    private FirebaseTokenVerifier firebaseTokenVerifier;

    @InjectMocks
    private LoginUseCase loginUseCase;

    @Test
    void execute_WhenValidTokenAndUserExists_ReturnsLoginResponseDto() throws FirebaseAuthException {
        LoginDto loginDto = new LoginDto();
        loginDto.firebaseToken = "token_valido_de_prueba";
        String expectedUid = "uid_firebase_123";
        
        User mockUser = new User(1L, expectedUid, "Juan Perez", "juan@correo.com", 1, true);

        when(firebaseTokenVerifierInstance.isResolvable()).thenReturn(true);
        when(firebaseTokenVerifierInstance.get()).thenReturn(firebaseTokenVerifier);
        
        when(firebaseTokenVerifier.verifyTokenAndGetUid(anyString())).thenReturn(expectedUid);
        
        when(userRepository.findByFirebaseUuid(expectedUid)).thenReturn(Optional.of(mockUser));

        LoginResponseDto response = loginUseCase.execute(loginDto);

        assertNotNull(response);
        assertEquals("Juan Perez", response.name);
        assertEquals("juan@correo.com", response.email);
        
        verify(firebaseTokenVerifier).verifyTokenAndGetUid("token_valido_de_prueba");
        verify(userRepository).findByFirebaseUuid(expectedUid);
    }

    @Test
    void execute_WhenLoginDtoIsNull_ThrowsIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            loginUseCase.execute(null);
        });
        assertEquals("El cuerpo de la solicitud es requerido", exception.getMessage());
    }

    @Test
    void execute_WhenFirebaseTokenIsBlank_ThrowsIllegalArgumentException() {
        LoginDto loginDto = new LoginDto();
        loginDto.firebaseToken = "   "; 
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            loginUseCase.execute(loginDto);
        });
        assertEquals("Se requiere un token de Firebase válido para iniciar sesión", exception.getMessage());
    }

    @Test
    void execute_WhenVerifierIsNotResolvable_ThrowsSecurityException() {
        LoginDto loginDto = new LoginDto();
        loginDto.firebaseToken = "token_cualquiera";
        
        when(firebaseTokenVerifierInstance.isResolvable()).thenReturn(false);

        SecurityException exception = assertThrows(SecurityException.class, () -> {
            loginUseCase.execute(loginDto);
        });
        assertEquals("No hay un verificador de Firebase configurado en el entorno actual", exception.getMessage());
    }

    @Test
    void execute_WhenTokenIsInvalid_ThrowsSecurityException() throws FirebaseAuthException {
        LoginDto loginDto = new LoginDto();
        loginDto.firebaseToken = "token_invalido";
        
        when(firebaseTokenVerifierInstance.isResolvable()).thenReturn(true);
        when(firebaseTokenVerifierInstance.get()).thenReturn(firebaseTokenVerifier);
        
        FirebaseAuthException mockAuthException = mock(FirebaseAuthException.class);
        when(mockAuthException.getMessage()).thenReturn("El token ha expirado o no es válido");

        when(firebaseTokenVerifier.verifyTokenAndGetUid(anyString())).thenThrow(mockAuthException);

        SecurityException exception = assertThrows(SecurityException.class, () -> {
            loginUseCase.execute(loginDto);
        });
        assertTrue(exception.getMessage().contains("La verificación del token de Firebase falló:"));
    }

    @Test
    void execute_WhenUserIsNotFoundInDb_ThrowsSecurityException() throws FirebaseAuthException {
        LoginDto loginDto = new LoginDto();
        loginDto.firebaseToken = "token_valido";
        String expectedUid = "uid_no_registrado";

        when(firebaseTokenVerifierInstance.isResolvable()).thenReturn(true);
        when(firebaseTokenVerifierInstance.get()).thenReturn(firebaseTokenVerifier);
        when(firebaseTokenVerifier.verifyTokenAndGetUid(anyString())).thenReturn(expectedUid);
        
        when(userRepository.findByFirebaseUuid(expectedUid)).thenReturn(Optional.empty());

        SecurityException exception = assertThrows(SecurityException.class, () -> {
            loginUseCase.execute(loginDto);
        });
        assertEquals("Usuario no encontrado en la base de datos con este UUID de Firebase", exception.getMessage());
    }
}