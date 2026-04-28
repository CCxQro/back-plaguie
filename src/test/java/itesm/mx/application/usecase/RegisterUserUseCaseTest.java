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
    void execute_WhenRegisterUserDtoIsNull_ThrowsIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> registerUserUseCase.execute(null));

        assertEquals("El cuerpo de la solicitud es requerido", exception.getMessage());
        verifyNoInteractions(userRepository, firebaseUserManager);
    }

    @Test
    void execute_WhenEmailAlreadyExists_ThrowsIllegalStateException() {
        RegisterUserDto registerUserDto = validRegisterUserDto();
        User existingUser = new User(99L, "existing-uid", "Existing User", registerUserDto.email, 1, true);

        when(userRepository.findByEmail(registerUserDto.email)).thenReturn(Optional.of(existingUser));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> registerUserUseCase.execute(registerUserDto));

        assertEquals("Ya existe un usuario registrado con este correo electrónico", exception.getMessage());
        verify(userRepository).findByEmail(registerUserDto.email);
        verifyNoInteractions(firebaseUserManager);
    }

    @Test
    void execute_WhenDataIsValid_ReturnsRegisterUserResponseDto() throws FirebaseAuthException {
        RegisterUserDto registerUserDto = validRegisterUserDto();
        String firebaseUuid = "firebase-uid-789";
        String customToken = "custom-token-abc";
        User savedUser = new User(22L, firebaseUuid, registerUserDto.name, registerUserDto.email, registerUserDto.roleId, true);

        when(userRepository.findByEmail(registerUserDto.email)).thenReturn(Optional.empty());
        when(firebaseUserManager.createFirebaseUser(registerUserDto.email, registerUserDto.password, registerUserDto.name)).thenReturn(firebaseUuid);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(firebaseUserManager.generateCustomToken(firebaseUuid)).thenReturn(customToken);

        RegisterUserResponseDto response = registerUserUseCase.execute(registerUserDto);

        assertNotNull(response);
        assertEquals(22L, response.userId);
        assertEquals(firebaseUuid, response.firebaseUuid);
        assertEquals(registerUserDto.name, response.name);
        assertEquals(registerUserDto.email, response.email);
        assertEquals(registerUserDto.roleId, response.roleId);
        assertEquals(customToken, response.firebaseToken);

        verify(userRepository).findByEmail(registerUserDto.email);
        verify(firebaseUserManager).createFirebaseUser(registerUserDto.email, registerUserDto.password, registerUserDto.name);
        verify(userRepository).save(any(User.class));
        verify(firebaseUserManager).generateCustomToken(firebaseUuid);
    }

    private RegisterUserDto validRegisterUserDto() {
        RegisterUserDto registerUserDto = new RegisterUserDto();
        registerUserDto.name = "Juan Perez";
        registerUserDto.email = "juan.perez@example.com";
        registerUserDto.password = "StrongPassword123!";
        registerUserDto.roleId = 1;
        return registerUserDto;
    }
}