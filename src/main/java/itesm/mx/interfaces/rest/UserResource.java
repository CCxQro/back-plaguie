package itesm.mx.interfaces.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import itesm.mx.application.dto.GetUserResponseDto;
import itesm.mx.application.dto.UpdateUserDto;
import itesm.mx.application.dto.UserPageResponseDto;
import itesm.mx.application.security.AuthenticatedUserContext;
import itesm.mx.application.usecase.users.DeactivateUserUseCase;
import itesm.mx.application.usecase.users.GetAllUsersUseCase;
import itesm.mx.application.usecase.users.GetUserByIdUseCase;
import itesm.mx.application.usecase.users.UpdateUserUseCase;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import static itesm.mx.interfaces.rest.utils.ErrorResponseUtils.errorResponse;

@Path("/api/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Users", description = "User administration endpoints")
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
    @Operation(summary = "List users", description = "Returns a paginated, filterable list of users. Admin-only endpoint.")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Users returned", content = @Content(schema = @Schema(implementation = UserPageResponseDto.class))),
            @APIResponse(responseCode = "400", description = "Invalid pagination parameters"),
            @APIResponse(responseCode = "401", description = "Authentication required"),
            @APIResponse(responseCode = "403", description = "Admin role required"),
            @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response getAllUsers(
            @Parameter(description = "0-based page number") @QueryParam("page") @DefaultValue("0") int page,
            @Parameter(description = "Page size") @QueryParam("size") @DefaultValue("10") int size,
            @Parameter(description = "Partial match on name or email (case-insensitive)") @QueryParam("name") String name,
            @Parameter(description = "Filter by exact role ID (1, 2 or 3)") @QueryParam("roleId") Integer roleId,
            @Parameter(description = "Filter by active status") @QueryParam("isActive") Boolean isActive
    ) {
        if (authenticatedUserContext.getCurrentUser() == null) {
            return errorResponse(Response.Status.UNAUTHORIZED, "Se requiere autenticación");
        }
        if (!ADMIN_ROLE_ID.equals(authenticatedUserContext.getCurrentUser().getRoleId())) {
            return errorResponse(Response.Status.FORBIDDEN, "Solo un administrador puede listar todos los usuarios");
        }

        try {
            UserPageResponseDto page_result = getAllUsersUseCase.execute(page, size, name, roleId, isActive);
            return Response.ok(page_result).build();
        } catch (IllegalArgumentException e) {
            return errorResponse(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (RuntimeException e) {
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Get user by id", description = "Returns a user when requested by an admin or by the same authenticated user.")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "User returned", content = @Content(schema = @Schema(implementation = GetUserResponseDto.class))),
            @APIResponse(responseCode = "400", description = "Invalid user id"),
            @APIResponse(responseCode = "401", description = "Authentication required"),
            @APIResponse(responseCode = "403", description = "Not allowed to access this user"),
            @APIResponse(responseCode = "404", description = "User not found"),
            @APIResponse(responseCode = "500", description = "Internal server error")
    })
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
    @Operation(summary = "Update user", description = "Updates a user record. Admin-only endpoint.")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = UpdateUserDto.class)))
    @APIResponses({
            @APIResponse(responseCode = "200", description = "User updated", content = @Content(schema = @Schema(implementation = GetUserResponseDto.class))),
            @APIResponse(responseCode = "400", description = "Invalid or missing request body"),
            @APIResponse(responseCode = "401", description = "Authentication required"),
            @APIResponse(responseCode = "403", description = "Admin role required"),
            @APIResponse(responseCode = "404", description = "User not found"),
            @APIResponse(responseCode = "500", description = "Internal server error")
    })
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
    @Operation(summary = "Deactivate user", description = "Soft-deletes a user by marking it inactive. Admin-only endpoint.")
    @APIResponses({
            @APIResponse(responseCode = "204", description = "User deactivated"),
            @APIResponse(responseCode = "400", description = "Invalid user id"),
            @APIResponse(responseCode = "401", description = "Authentication required"),
            @APIResponse(responseCode = "403", description = "Admin role required"),
            @APIResponse(responseCode = "404", description = "User not found"),
            @APIResponse(responseCode = "500", description = "Internal server error")
    })
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
}
