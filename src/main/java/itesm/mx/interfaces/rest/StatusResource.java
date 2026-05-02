package itesm.mx.interfaces.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import itesm.mx.application.dto.GetStatusResponseDto;
import itesm.mx.application.dto.RegisterStatusDto;
import itesm.mx.application.security.AuthenticatedUserContext;
import itesm.mx.application.usecase.marketplace.status.DeleteStatusUseCase;
import itesm.mx.application.usecase.marketplace.status.GetAllStatusesUseCase;
import itesm.mx.application.usecase.marketplace.status.RegisterStatusUseCase;
import itesm.mx.application.usecase.marketplace.status.UpdateStatusUseCase;
import itesm.mx.domain.models.marketplace.Status;
import itesm.mx.domain.models.user.RoleConstants;

import java.util.List;

import static itesm.mx.interfaces.rest.utils.ErrorResponseUtils.errorResponse;

@Path("/api/status")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class StatusResource {

    @Inject
    RegisterStatusUseCase registerStatusUseCase;

    @Inject
    GetAllStatusesUseCase getAllStatusesUseCase;

    @Inject
    UpdateStatusUseCase updateStatusUseCase;

    @Inject
    DeleteStatusUseCase deleteStatusUseCase;

    @Inject
    AuthenticatedUserContext authenticatedUserContext;

    @GET
    public Response getAllStatuses() {
        if (authenticatedUserContext.getCurrentUser() == null) {
            return errorResponse(Response.Status.UNAUTHORIZED, "Se requiere autenticacion");
        }

        Integer role = authenticatedUserContext.getCurrentUser().getRoleId();
        if (!RoleConstants.ADMIN.equals(role) && !RoleConstants.SELLER.equals(role)) {
            return errorResponse(Response.Status.FORBIDDEN, "Solo un administrador o tecnico vendedor puede consultar los estatus");
        }

        try {
            List<GetStatusResponseDto> statuses = getAllStatusesUseCase.execute()
                    .stream()
                    .map(s -> new GetStatusResponseDto(s.getStatusId(), s.getName()))
                    .toList();
            return Response.ok(statuses).build();
        } catch (RuntimeException e) {
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }

    @POST
    public Response registerStatus(RegisterStatusDto dto) {
        if (dto == null) {
            return errorResponse(Response.Status.BAD_REQUEST, "El cuerpo de la solicitud es requerido");
        }
        if (authenticatedUserContext.getCurrentUser() == null) {
            return errorResponse(Response.Status.UNAUTHORIZED, "Se requiere autenticacion");
        }
        if (!RoleConstants.ADMIN.equals(authenticatedUserContext.getCurrentUser().getRoleId())) {
            return errorResponse(Response.Status.FORBIDDEN, "Solo un administrador puede registrar estatus");
        }

        try {
            Status created = registerStatusUseCase.execute(new Status(null, dto.name));
            return Response.status(Response.Status.CREATED)
                    .entity(new GetStatusResponseDto(created.getStatusId(), created.getName()))
                    .build();
        } catch (IllegalArgumentException e) {
            return errorResponse(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (RuntimeException e) {
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }

    @PUT
    @Path("/{id}")
    public Response updateStatus(@PathParam("id") Long id, RegisterStatusDto dto) {
        if (dto == null) {
            return errorResponse(Response.Status.BAD_REQUEST, "El cuerpo de la solicitud es requerido");
        }
        if (authenticatedUserContext.getCurrentUser() == null) {
            return errorResponse(Response.Status.UNAUTHORIZED, "Se requiere autenticacion");
        }
        if (!RoleConstants.ADMIN.equals(authenticatedUserContext.getCurrentUser().getRoleId())) {
            return errorResponse(Response.Status.FORBIDDEN, "Solo un administrador puede actualizar estatus");
        }

        try {
            Status updated = updateStatusUseCase.execute(id, new Status(null, dto.name));
            return Response.ok(new GetStatusResponseDto(updated.getStatusId(), updated.getName())).build();
        } catch (IllegalArgumentException e) {
            return errorResponse(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (RuntimeException e) {
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }

    @DELETE
    @Path("/{id}")
    public Response deleteStatus(@PathParam("id") Long id) {
        if (authenticatedUserContext.getCurrentUser() == null) {
            return errorResponse(Response.Status.UNAUTHORIZED, "Se requiere autenticacion");
        }
        if (!RoleConstants.ADMIN.equals(authenticatedUserContext.getCurrentUser().getRoleId())) {
            return errorResponse(Response.Status.FORBIDDEN, "Solo un administrador puede eliminar estatus");
        }

        try {
            deleteStatusUseCase.execute(id);
            return Response.noContent().build();
        } catch (IllegalArgumentException e) {
            return errorResponse(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (RuntimeException e) {
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }
}