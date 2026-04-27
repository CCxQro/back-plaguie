package itesm.mx.interfaces.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import itesm.mx.application.dto.GetUserResponseDto;
import itesm.mx.application.dto.UpdateUserDto;
import itesm.mx.application.security.AuthenticatedUserContext;
import itesm.mx.application.usecase.DeactivateUserUseCase;
import itesm.mx.application.usecase.GetAllUsersUseCase;
import itesm.mx.application.usecase.GetUserByIdUseCase;
import itesm.mx.application.usecase.UpdateUserUseCase;

import java.util.List;

@Path("/api/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

    private static final Integer ADMIN_ROLE_ID = 1;

    @Inject
    GetAllUsersUseCase getAllUsersUseCase;

    @Inject
    GetUserByIdUseCase getUserByIdUseCase;

    @Inject
    UpdateUserUseCase updateUserUseCase;

    @Inject
    DeactivateUserUseCase deactivateUserUseCase;

    @Inject
    AuthenticatedUserContext authenticatedUserContext;

    @GET
    public Response getAllUsers() {
        if (authenticatedUserContext.getCurrentUser() == null) {
            return errorResponse(Response.Status.UNAUTHORIZED, "Se requiere autenticación");
        }
        if (!ADMIN_ROLE_ID.equals(authenticatedUserContext.getCurrentUser().getRoleId())) {
            return errorResponse(Response.Status.FORBIDDEN, "Solo un administrador puede listar todos los usuarios");
        }

        try {
            List<GetUserResponseDto> users = getAllUsersUseCase.execute();
            return Response.ok(users).build();
        } catch (RuntimeException e) {
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }

    @GET
    @Path("/{id}")
    public Response getUserById(@PathParam("id") Long id) {
        if (authenticatedUserContext.getCurrentUser() == null) {
            return errorResponse(Response.Status.UNAUTHORIZED, "Se requiere autenticación");
        }

        boolean isAdmin = ADMIN_ROLE_ID.equals(authenticatedUserContext.getCurrentUser().getRoleId());
        boolean isSelf = authenticatedUserContext.getCurrentUser().getUserId().equals(id);

        if (!isAdmin && !isSelf) {
            return errorResponse(Response.Status.FORBIDDEN, "No tienes permiso para consultar este usuario");
        }

        try {
            GetUserResponseDto user = getUserByIdUseCase.execute(id);
            return Response.ok(user).build();
        } catch (IllegalArgumentException e) {
            return errorResponse(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (IllegalStateException e) {
            return errorResponse(Response.Status.NOT_FOUND, e.getMessage());
        } catch (RuntimeException e) {
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }

    @PUT
    @Path("/{id}")
    public Response updateUser(@PathParam("id") Long id, UpdateUserDto updateUserDto) {
        if (authenticatedUserContext.getCurrentUser() == null) {
            return errorResponse(Response.Status.UNAUTHORIZED, "Se requiere autenticación");
        }
        if (!ADMIN_ROLE_ID.equals(authenticatedUserContext.getCurrentUser().getRoleId())) {
            return errorResponse(Response.Status.FORBIDDEN, "Solo un administrador puede actualizar usuarios");
        }
        if (updateUserDto == null) {
            return errorResponse(Response.Status.BAD_REQUEST, "El cuerpo de la solicitud es requerido");
        }

        try {
            GetUserResponseDto updated = updateUserUseCase.execute(id, updateUserDto);
            return Response.ok(updated).build();
        } catch (IllegalArgumentException e) {
            return errorResponse(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (IllegalStateException e) {
            return errorResponse(Response.Status.NOT_FOUND, e.getMessage());
        } catch (RuntimeException e) {
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }

    @DELETE
    @Path("/{id}")
    public Response deactivateUser(@PathParam("id") Long id) {
        if (authenticatedUserContext.getCurrentUser() == null) {
            return errorResponse(Response.Status.UNAUTHORIZED, "Se requiere autenticación");
        }
        if (!ADMIN_ROLE_ID.equals(authenticatedUserContext.getCurrentUser().getRoleId())) {
            return errorResponse(Response.Status.FORBIDDEN, "Solo un administrador puede desactivar usuarios");
        }

        try {
            deactivateUserUseCase.execute(id);
            return Response.noContent().build();
        } catch (IllegalArgumentException e) {
            return errorResponse(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (IllegalStateException e) {
            return errorResponse(Response.Status.NOT_FOUND, e.getMessage());
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
