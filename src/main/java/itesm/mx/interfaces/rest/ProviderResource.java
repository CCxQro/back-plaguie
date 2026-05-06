package itesm.mx.interfaces.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import itesm.mx.application.dto.GetProviderResponseDto;
import itesm.mx.application.dto.RegisterProviderDto;
import itesm.mx.application.dto.UpdateProviderDto;
import itesm.mx.application.security.AuthenticatedUserContext;
import itesm.mx.application.usecase.marketplace.provider.*;
import itesm.mx.domain.models.marketplace.Provider;
import itesm.mx.domain.models.user.RoleConstants;
import itesm.mx.domain.models.user.User;

import java.util.List;

import static itesm.mx.interfaces.rest.utils.ErrorResponseUtils.errorResponse;

@Path("/api/providers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProviderResource {

    @Inject RegisterProviderUseCase registerProviderUseCase;
    @Inject UpdateProviderUseCase updateProviderUseCase;
    @Inject DeleteProviderUseCase deleteProviderUseCase;
    @Inject GetAllProvidersUseCase getAllProvidersUseCase;
    @Inject GetProviderByIdUseCase getProviderByIdUseCase;
    @Inject GetProvidersByUserUseCase getProvidersByUserUseCase;
    @Inject AuthenticatedUserContext authenticatedUserContext;

    @GET
    public Response getProviders(@QueryParam("userId") Long userId) {
        if (authenticatedUserContext.getCurrentUser() == null) {
            return errorResponse(Response.Status.UNAUTHORIZED, "Se requiere autenticacion");
        }
        Integer role = authenticatedUserContext.getCurrentUser().getRoleId();
        if (!RoleConstants.ADMIN.equals(role) && !RoleConstants.SELLER.equals(role)) {
            return errorResponse(Response.Status.FORBIDDEN, "Solo un administrador o tecnico vendedor puede consultar los proveedores");
        }

        try {
            List<GetProviderResponseDto> providers;
            if (userId != null) {
                if (!RoleConstants.ADMIN.equals(role)) {
                    Long currentUserId = authenticatedUserContext.getCurrentUser().getUserId();
                    if (!userId.equals(currentUserId)) {
                        return errorResponse(Response.Status.FORBIDDEN, "Solo puedes consultar tus propios proveedores");
                    }
                }
                providers = getProvidersByUserUseCase.execute(userId)
                        .stream()
                        .map(this::toResponseDto)
                        .toList();
            } else {
                providers = getAllProvidersUseCase.execute()
                        .stream()
                        .map(this::toResponseDto)
                        .toList();
            }
            return Response.ok(providers).build();
        } catch (IllegalArgumentException e) {
            return errorResponse(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (RuntimeException e) {
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }

    @POST
    public Response registerProvider(RegisterProviderDto dto) {
        if (dto == null) {
            return errorResponse(Response.Status.BAD_REQUEST, "El cuerpo de la solicitud es requerido");
        }
        if (authenticatedUserContext.getCurrentUser() == null) {
            return errorResponse(Response.Status.UNAUTHORIZED, "Se requiere autenticacion");
        }
        Integer role = authenticatedUserContext.getCurrentUser().getRoleId();
        if (!RoleConstants.ADMIN.equals(role) && !RoleConstants.SELLER.equals(role)) {
            return errorResponse(Response.Status.FORBIDDEN, "Solo un administrador o tecnico vendedor puede registrar proveedores");
        }

        try {
            Long currentUserId = authenticatedUserContext.getCurrentUser().getUserId();

            User user = new User();
            user.setUserId(currentUserId);

            Provider created = registerProviderUseCase.execute(new Provider(null, user, dto.name));
            return Response.status(Response.Status.CREATED).entity(toResponseDto(created)).build();
        } catch (IllegalArgumentException e) {
            return errorResponse(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (RuntimeException e) {
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }

    @PUT
    @Path("/{id}")
    public Response updateProvider(@PathParam("id") Long id, UpdateProviderDto dto) {
        if (dto == null) {
            return errorResponse(Response.Status.BAD_REQUEST, "El cuerpo de la solicitud es requerido");
        }
        if (authenticatedUserContext.getCurrentUser() == null) {
            return errorResponse(Response.Status.UNAUTHORIZED, "Se requiere autenticacion");
        }
        Integer role = authenticatedUserContext.getCurrentUser().getRoleId();
        if (!RoleConstants.ADMIN.equals(role) && !RoleConstants.SELLER.equals(role)) {
            return errorResponse(Response.Status.FORBIDDEN, "Solo un administrador o tecnico vendedor puede actualizar proveedores");
        }

        try {
            Provider existing = getProviderByIdUseCase.execute(id).orElse(null);
            if (existing == null) {
                return errorResponse(Response.Status.NOT_FOUND, "Proveedor no encontrado");
            }

            if (!RoleConstants.ADMIN.equals(role)) {
                Long currentUserId = authenticatedUserContext.getCurrentUser().getUserId();
                boolean isOwner = existing.getUser().getUserId().equals(currentUserId);
                if (!isOwner) {
                    return errorResponse(Response.Status.FORBIDDEN, "No tienes permiso para actualizar este proveedor");
                }
            }

            Provider updated = updateProviderUseCase.execute(id,
                    new Provider(null, existing.getUser(), dto.name));
            return Response.ok(toResponseDto(updated)).build();
        } catch (IllegalArgumentException e) {
            return errorResponse(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (RuntimeException e) {
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }

    @DELETE
    @Path("/{id}")
    public Response deleteProvider(@PathParam("id") Long id) {
        if (authenticatedUserContext.getCurrentUser() == null) {
            return errorResponse(Response.Status.UNAUTHORIZED, "Se requiere autenticacion");
        }
        Integer role = authenticatedUserContext.getCurrentUser().getRoleId();
        if (!RoleConstants.ADMIN.equals(role)) {
            return errorResponse(Response.Status.FORBIDDEN, "Solo un administrador puede eliminar proveedores");
        }

        try {
            boolean deleted = deleteProviderUseCase.execute(id);
            if (!deleted) {
                return errorResponse(Response.Status.NOT_FOUND, "Proveedor no encontrado");
            }
            return Response.noContent().build();
        } catch (IllegalArgumentException e) {
            return errorResponse(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (RuntimeException e) {
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }

    private GetProviderResponseDto toResponseDto(Provider provider) {
        return new GetProviderResponseDto(
                provider.getProviderId(),
                provider.getName(),
                provider.getUser().getUserId()
        );
    }
}