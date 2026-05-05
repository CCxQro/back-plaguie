package itesm.mx.interfaces.rest;

import itesm.mx.application.dto.LoginDto;
import itesm.mx.application.dto.LoginResponseDto;
import itesm.mx.application.dto.RegisterUserDto;
import itesm.mx.application.dto.RegisterUserResponseDto;
import itesm.mx.application.dto.SignupDto;
import itesm.mx.application.security.AuthenticatedUserContext;
import itesm.mx.application.security.CurrentUser;
import itesm.mx.application.usecase.LoginUseCase;
import itesm.mx.application.usecase.RegisterUserUseCase;
import itesm.mx.domain.models.RoleConstants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.ArgumentCaptor;

import jakarta.ws.rs.core.Response;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthResourceTest {

    @Mock
    private LoginUseCase loginUseCase;

    @Mock
    private RegisterUserUseCase registerUserUseCase;

    @Mock
    private AuthenticatedUserContext authenticatedUserContext;

    @InjectMocks
    private AuthResource authResource;

    @Test
    void login_WhenDtoIsNull_Returns400BadRequest() {
        Response response = authResource.login(null);

        assertEquals(400, response.getStatus());
        assertEquals("El cuerpo de la solicitud es requerido", ((AuthResource.ErrorResponse) response.getEntity()).error);
        verify(loginUseCase, never()).execute(any());
    }

    @Test
    void login_WhenUseCaseReturnsData_Returns200Ok() {
        LoginDto requestDto = new LoginDto();
        requestDto.firebaseToken = "token-valido";
        LoginResponseDto expectedResponse = new LoginResponseDto("Juan Perez", "juan@correo.com", 1);

        when(loginUseCase.execute(requestDto)).thenReturn(expectedResponse);

        Response response = authResource.login(requestDto);

        assertEquals(200, response.getStatus());
        LoginResponseDto actualResponse = (LoginResponseDto) response.getEntity();
        assertEquals("Juan Perez", actualResponse.name);
        assertEquals("juan@correo.com", actualResponse.email);
        assertEquals(1, actualResponse.roleId);
    }

    @Test
    void login_WhenUseCaseThrowsIllegalArgumentException_Returns400BadRequest() {
        LoginDto requestDto = new LoginDto();
        requestDto.firebaseToken = "   ";
        when(loginUseCase.execute(requestDto)).thenThrow(new IllegalArgumentException("Se requiere un token de Firebase válido para iniciar sesión"));

        Response response = authResource.login(requestDto);

        assertEquals(400, response.getStatus());
        assertEquals("Se requiere un token de Firebase válido para iniciar sesión", ((AuthResource.ErrorResponse) response.getEntity()).error);
    }

    @Test
    void login_WhenUseCaseThrowsSecurityException_Returns401Unauthorized() {
        LoginDto requestDto = new LoginDto();
        requestDto.firebaseToken = "token-invalido";
        when(loginUseCase.execute(requestDto)).thenThrow(new SecurityException("Usuario no encontrado en la base de datos con este UUID de Firebase"));

        Response response = authResource.login(requestDto);

        assertEquals(401, response.getStatus());
        assertEquals("Usuario no encontrado en la base de datos con este UUID de Firebase", ((AuthResource.ErrorResponse) response.getEntity()).error);
    }

    @Test
    void login_WhenUseCaseThrowsRuntimeException_Returns500() {
        LoginDto requestDto = new LoginDto();
        requestDto.firebaseToken = "token-invalido";
        when(loginUseCase.execute(requestDto)).thenThrow(new RuntimeException("boom"));

        Response response = authResource.login(requestDto);

        assertEquals(500, response.getStatus());
        assertEquals("Error interno del servidor", ((AuthResource.ErrorResponse) response.getEntity()).error);
    }

    @Test
    void register_WhenDtoIsNull_Returns400BadRequest() {
        Response response = authResource.register(null);

        assertEquals(400, response.getStatus());
        assertEquals("El cuerpo de la solicitud es requerido", ((AuthResource.ErrorResponse) response.getEntity()).error);
        verify(registerUserUseCase, never()).execute(any());
    }

    @Test
    void register_WhenUserIsNotAuthenticated_Returns401Unauthorized() {
        when(authenticatedUserContext.getCurrentUser()).thenReturn(null);

        Response response = authResource.register(validRegisterUserDto());

        assertEquals(401, response.getStatus());
        assertEquals("Se requiere autenticación", ((AuthResource.ErrorResponse) response.getEntity()).error);
        verify(registerUserUseCase, never()).execute(any());
    }

    @Test
    void register_WhenUserIsNotAdmin_Returns403Forbidden() {
        when(authenticatedUserContext.getCurrentUser())
            .thenReturn(new CurrentUser(10L, "firebase-10", "Usuario", "user@itesm.mx", RoleConstants.FARMER));

        Response response = authResource.register(validRegisterUserDto());

        assertEquals(403, response.getStatus());
        assertEquals("Solo un administrador puede registrar usuarios", ((AuthResource.ErrorResponse) response.getEntity()).error);
        verify(registerUserUseCase, never()).execute(any());
    }

    @Test
    void register_WhenUseCaseReturnsData_Returns201Created() {
        when(authenticatedUserContext.getCurrentUser())
            .thenReturn(new CurrentUser(1L, "firebase-admin", "Admin", "admin@itesm.mx", RoleConstants.ADMIN));

        RegisterUserDto requestDto = validRegisterUserDto();
        RegisterUserResponseDto expectedResponse = new RegisterUserResponseDto(
            22L,
            "uuid-register-123",
            requestDto.name,
            requestDto.email,
            requestDto.roleId,
            "custom-token-register"
        );
        when(registerUserUseCase.execute(requestDto)).thenReturn(expectedResponse);

        Response response = authResource.register(requestDto);

        assertEquals(201, response.getStatus());
        RegisterUserResponseDto actualResponse = (RegisterUserResponseDto) response.getEntity();
        assertEquals(22L, actualResponse.userId);
        assertEquals("uuid-register-123", actualResponse.firebaseUuid);
        assertEquals(requestDto.name, actualResponse.name);
        assertEquals(requestDto.email, actualResponse.email);
        assertEquals(requestDto.roleId, actualResponse.roleId);
        assertEquals("custom-token-register", actualResponse.firebaseToken);
    }

    @Test
    void register_WhenUseCaseThrowsIllegalStateException_Returns409Conflict() {
        when(authenticatedUserContext.getCurrentUser())
            .thenReturn(new CurrentUser(1L, "firebase-admin", "Admin", "admin@itesm.mx", RoleConstants.ADMIN));
        RegisterUserDto requestDto = validRegisterUserDto();
        when(registerUserUseCase.execute(requestDto)).thenThrow(new IllegalStateException("Ya existe un usuario registrado con este correo electrónico"));

        Response response = authResource.register(requestDto);

        assertEquals(409, response.getStatus());
        assertEquals("Ya existe un usuario registrado con este correo electrónico", ((AuthResource.ErrorResponse) response.getEntity()).error);
    }

    @Test
    void register_WhenUseCaseThrowsSecurityException_Returns500() {
        when(authenticatedUserContext.getCurrentUser())
            .thenReturn(new CurrentUser(1L, "firebase-admin", "Admin", "admin@itesm.mx", RoleConstants.ADMIN));
        RegisterUserDto requestDto = validRegisterUserDto();
        when(registerUserUseCase.execute(requestDto)).thenThrow(new SecurityException("Error al generar token"));

        Response response = authResource.register(requestDto);

        assertEquals(500, response.getStatus());
        assertEquals("Error al generar token", ((AuthResource.ErrorResponse) response.getEntity()).error);
    }

    @Test
    void signup_WhenDtoIsNull_Returns400BadRequest() {
        Response response = authResource.signup(null);

        assertEquals(400, response.getStatus());
        assertEquals("El cuerpo de la solicitud es requerido", ((AuthResource.ErrorResponse) response.getEntity()).error);
        verify(registerUserUseCase, never()).execute(any());
    }

    @Test
    void signup_WhenUseCaseReturnsData_Returns201CreatedWithSellerRole() {
        SignupDto requestDto = validSignupDto();

        RegisterUserResponseDto expectedResponse = new RegisterUserResponseDto(
            33L,
            "uuid-signup-123",
            requestDto.name,
            requestDto.email,
            RoleConstants.SELLER,
            "custom-token-signup"
        );
        when(registerUserUseCase.execute(any(RegisterUserDto.class))).thenReturn(expectedResponse);

        Response response = authResource.signup(requestDto);

        assertEquals(201, response.getStatus());
        RegisterUserResponseDto actualResponse = (RegisterUserResponseDto) response.getEntity();
        assertEquals(RoleConstants.SELLER, actualResponse.roleId);
        assertEquals(requestDto.name, actualResponse.name);
        assertEquals(requestDto.email, actualResponse.email);

        ArgumentCaptor<RegisterUserDto> captor = ArgumentCaptor.forClass(RegisterUserDto.class);
        verify(registerUserUseCase).execute(captor.capture());
        assertEquals(requestDto.name, captor.getValue().name);
        assertEquals(requestDto.email, captor.getValue().email);
        assertEquals(requestDto.password, captor.getValue().password);
        assertEquals(RoleConstants.SELLER, captor.getValue().roleId);
    }

    @Test
    void signup_WhenUseCaseThrowsIllegalStateException_Returns409Conflict() {
        SignupDto requestDto = validSignupDto();
        when(registerUserUseCase.execute(any(RegisterUserDto.class))).thenThrow(new IllegalStateException("Ya existe un usuario registrado con este correo electrónico"));

        Response response = authResource.signup(requestDto);

        assertEquals(409, response.getStatus());
        assertEquals("Ya existe un usuario registrado con este correo electrónico", ((AuthResource.ErrorResponse) response.getEntity()).error);
    }

    @Test
    void signup_WhenUseCaseThrowsIllegalArgumentException_Returns400BadRequest() {
        SignupDto requestDto = validSignupDto();
        when(registerUserUseCase.execute(any(RegisterUserDto.class))).thenThrow(new IllegalArgumentException("Se requiere el nombre"));

        Response response = authResource.signup(requestDto);

        assertEquals(400, response.getStatus());
        assertEquals("Se requiere el nombre", ((AuthResource.ErrorResponse) response.getEntity()).error);
    }

    private RegisterUserDto validRegisterUserDto() {
        RegisterUserDto dto = new RegisterUserDto();
        dto.name = "Nuevo Usuario";
        dto.email = "nuevo.usuario@itesm.mx";
        dto.password = "Password123!";
        dto.roleId = RoleConstants.SELLER;
        return dto;
    }

    private SignupDto validSignupDto() {
        SignupDto dto = new SignupDto();
        dto.name = "Registro Publico";
        dto.email = "registro.publico@itesm.mx";
        dto.password = "Password123!";
        return dto;
    }
}
