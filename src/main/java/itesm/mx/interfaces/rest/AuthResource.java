package itesm.mx.interfaces.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import itesm.mx.application.dto.RegisterUserDto;
import itesm.mx.application.dto.RegisterUserResponseDto;
import itesm.mx.application.dto.SignupDto;
import itesm.mx.application.security.AuthenticatedUserContext;
import itesm.mx.application.dto.LoginDto;
import itesm.mx.application.dto.LoginResponseDto;
import itesm.mx.application.usecase.LoginUseCase;
import itesm.mx.application.usecase.RegisterUserUseCase;

@Path("/api/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    private static final Integer SELF_SIGNUP_ROLE_ID = 2;

    @Inject
    LoginUseCase loginUseCase;

    @Inject
    RegisterUserUseCase registerUserUseCase;

    @Inject
    AuthenticatedUserContext authenticatedUserContext;

    @POST
    @Path("/login")
    public Response login(LoginDto loginDto) {
        if (loginDto == null) {
            return errorResponse(Response.Status.BAD_REQUEST, "El cuerpo de la solicitud es requerido");
        }

        try {
            LoginResponseDto response = loginUseCase.execute(loginDto);
            return Response.ok(response)
                    .build();
        } catch (IllegalArgumentException e) {
            return errorResponse(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (SecurityException e) {
            return errorResponse(Response.Status.UNAUTHORIZED, e.getMessage());
        } catch (RuntimeException e) {
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }

    @POST
    @Path("/register")
    public Response register(RegisterUserDto registerUserDto) {
        if (registerUserDto == null) {
            return errorResponse(Response.Status.BAD_REQUEST, "El cuerpo de la solicitud es requerido");
        }

        if (authenticatedUserContext.getCurrentUser() == null) {
            return errorResponse(Response.Status.UNAUTHORIZED, "Se requiere autenticación");
        }

        if (!Integer.valueOf(1).equals(authenticatedUserContext.getCurrentUser().getRoleId())) {
            return errorResponse(Response.Status.FORBIDDEN, "Solo un administrador puede registrar usuarios");
        }

        try {
            RegisterUserResponseDto response = registerUserUseCase.execute(registerUserDto);
            return Response.status(Response.Status.CREATED)
                    .entity(response)
                    .build();
        } catch (IllegalArgumentException e) {
            return errorResponse(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (IllegalStateException e) {
            return errorResponse(Response.Status.CONFLICT, e.getMessage());
        } catch (SecurityException e) {
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage());
        } catch (RuntimeException e) {
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }

    @POST
    @Path("/signup")
    public Response signup(SignupDto signupDto) {
        if (signupDto == null) {
            return errorResponse(Response.Status.BAD_REQUEST, "El cuerpo de la solicitud es requerido");
        }

        RegisterUserDto registerUserDto = new RegisterUserDto();
        registerUserDto.name = signupDto.name;
        registerUserDto.email = signupDto.email;
        registerUserDto.password = signupDto.password;
        registerUserDto.roleId = signupDto.roleId;

        try {
            RegisterUserResponseDto response = registerUserUseCase.execute(registerUserDto);
            return Response.status(Response.Status.CREATED)
                    .entity(response)
                    .build();
        } catch (IllegalArgumentException e) {
            return errorResponse(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (IllegalStateException e) {
            return errorResponse(Response.Status.CONFLICT, e.getMessage());
        } catch (SecurityException e) {
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage());
        } catch (RuntimeException e) {
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }



    private Response errorResponse(Response.Status status, String message) {
        return Response.status(status)
                .entity(new ErrorResponse(message))
                .build();
    }

    public static class ErrorResponse {
        public String error;

        public ErrorResponse(String error) {
            this.error = error;
        }
    }
}