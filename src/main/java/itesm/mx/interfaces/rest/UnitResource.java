package itesm.mx.interfaces.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import itesm.mx.application.dto.GetUnitResponseDto;
import itesm.mx.application.dto.RegisterUnitDto;
import itesm.mx.application.dto.UpdateUnitDto;
import itesm.mx.application.security.AuthenticatedUserContext;
import itesm.mx.application.usecase.marketplace.unit.*;
import itesm.mx.domain.models.marketplace.Status;
import itesm.mx.domain.models.marketplace.Unit;
import itesm.mx.domain.models.user.RoleConstants;
import itesm.mx.domain.models.user.User;

import java.util.List;

import static itesm.mx.interfaces.rest.utils.ErrorResponseUtils.errorResponse;

@Path("/api/units")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UnitResource {

    private static final Long STATUS_ACCEPTED = 1L;
    private static final Long STATUS_REVISION = 2L;

    @Inject RegisterUnitUseCase registerUnitUseCase;
    @Inject UpdateUnitUseCase updateUnitUseCase;
    @Inject DeleteUnitUseCase deleteUnitUseCase;
    @Inject GetAllUnitsUseCase getAllUnitsUseCase;
    @Inject GetUnitsByStatusUseCase getUnitsByStatusUseCase;
    @Inject GetUnitByIdUseCase getUnitByIdUseCase;
    @Inject GetUnitsByUserUseCase getUnitsByUserUseCase;
    @Inject AuthenticatedUserContext authenticatedUserContext;

    @GET
    @Path("/me")
    public Response getMyUnits() {
        if (authenticatedUserContext.getCurrentUser() == null) {
            return errorResponse(Response.Status.UNAUTHORIZED, "Se requiere autenticacion");
        }
        Integer role = authenticatedUserContext.getCurrentUser().getRoleId();
        if (!RoleConstants.ADMIN.equals(role) && !RoleConstants.SELLER.equals(role)) {
            return errorResponse(Response.Status.FORBIDDEN, "Solo un administrador o tecnico vendedor puede consultar sus unidades");
        }

        try {
            Long currentUserId = authenticatedUserContext.getCurrentUser().getUserId();
            List<GetUnitResponseDto> units = getUnitsByUserUseCase.execute(currentUserId)
                    .stream()
                    .map(this::toResponseDto)
                    .toList();
            return Response.ok(units).build();
        } catch (IllegalArgumentException e) {
            return errorResponse(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (RuntimeException e) {
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }

    @GET
    public Response getAllUnits(@QueryParam("statusId") Long statusId) {
        if (authenticatedUserContext.getCurrentUser() == null) {
            return errorResponse(Response.Status.UNAUTHORIZED, "Se requiere autenticacion");
        }
        Integer role = authenticatedUserContext.getCurrentUser().getRoleId();
        if (!RoleConstants.ADMIN.equals(role) && !RoleConstants.SELLER.equals(role)) {
            return errorResponse(Response.Status.FORBIDDEN, "Solo un administrador o tecnico vendedor puede consultar las unidades");
        }

        try {
            List<GetUnitResponseDto> units;
            if (RoleConstants.ADMIN.equals(role)) {
                units = (statusId != null
                        ? getUnitsByStatusUseCase.execute(statusId)
                        : getAllUnitsUseCase.execute())
                        .stream()
                        .map(this::toResponseDto)
                        .toList();
            } else {
                units = getUnitsByStatusUseCase.execute(STATUS_ACCEPTED)
                        .stream()
                        .map(this::toResponseDto)
                        .toList();
            }
            return Response.ok(units).build();
        } catch (IllegalArgumentException e) {
            return errorResponse(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (RuntimeException e) {
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }

    @POST
    public Response registerUnit(RegisterUnitDto dto) {
        if (dto == null) {
            return errorResponse(Response.Status.BAD_REQUEST, "El cuerpo de la solicitud es requerido");
        }
        if (authenticatedUserContext.getCurrentUser() == null) {
            return errorResponse(Response.Status.UNAUTHORIZED, "Se requiere autenticacion");
        }
        Integer role = authenticatedUserContext.getCurrentUser().getRoleId();
        if (!RoleConstants.ADMIN.equals(role) && !RoleConstants.SELLER.equals(role)) {
            return errorResponse(Response.Status.FORBIDDEN, "Solo un administrador o tecnico vendedor puede registrar unidades");
        }

        try {
            Long currentUserId = authenticatedUserContext.getCurrentUser().getUserId();
            Long statusId = RoleConstants.ADMIN.equals(role) ? STATUS_ACCEPTED : STATUS_REVISION;

            User user = new User();
            user.setUserId(currentUserId);

            Status status = new Status();
            status.setStatusId(statusId);

            Unit created = registerUnitUseCase.execute(new Unit(null, user, dto.name, status));
            return Response.status(Response.Status.CREATED).entity(toResponseDto(created)).build();
        } catch (IllegalArgumentException e) {
            return errorResponse(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (RuntimeException e) {
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }

    @PUT
    @Path("/{id}")
    public Response updateUnit(@PathParam("id") Long id, UpdateUnitDto dto) {
        if (dto == null) {
            return errorResponse(Response.Status.BAD_REQUEST, "El cuerpo de la solicitud es requerido");
        }
        if (authenticatedUserContext.getCurrentUser() == null) {
            return errorResponse(Response.Status.UNAUTHORIZED, "Se requiere autenticacion");
        }
        Integer role = authenticatedUserContext.getCurrentUser().getRoleId();
        if (!RoleConstants.ADMIN.equals(role) && !RoleConstants.SELLER.equals(role)) {
            return errorResponse(Response.Status.FORBIDDEN, "Solo un administrador o tecnico vendedor puede actualizar unidades");
        }

        try {
            Unit existing = getUnitByIdUseCase.execute(id).orElse(null);
            if (existing == null) {
                return errorResponse(Response.Status.NOT_FOUND, "Unidad no encontrada");
            }

            if (!RoleConstants.ADMIN.equals(role)) {
                Long currentUserId = authenticatedUserContext.getCurrentUser().getUserId();
                boolean isOwner = existing.getUser().getUserId().equals(currentUserId);
                boolean isRevision = STATUS_REVISION.equals(existing.getStatus().getStatusId());
                if (!isOwner || !isRevision) {
                    return errorResponse(Response.Status.FORBIDDEN, "No tienes permiso para actualizar esta unidad");
                }
            }

            Status status = new Status();
            status.setStatusId(dto.statusId != null ? dto.statusId : existing.getStatus().getStatusId());

            Unit updated = updateUnitUseCase.execute(id,
                    new Unit(null, existing.getUser(), dto.name, status));
            return Response.ok(toResponseDto(updated)).build();
        } catch (IllegalArgumentException e) {
            return errorResponse(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (RuntimeException e) {
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }

    @DELETE
    @Path("/{id}")
    public Response deleteUnit(@PathParam("id") Long id) {
        if (authenticatedUserContext.getCurrentUser() == null) {
            return errorResponse(Response.Status.UNAUTHORIZED, "Se requiere autenticacion");
        }
        Integer role = authenticatedUserContext.getCurrentUser().getRoleId();
        if (!RoleConstants.ADMIN.equals(role) && !RoleConstants.SELLER.equals(role)) {
            return errorResponse(Response.Status.FORBIDDEN, "Solo un administrador o tecnico vendedor puede eliminar unidades");
        }

        try {
            Unit existing = getUnitByIdUseCase.execute(id).orElse(null);
            if (existing == null) {
                return errorResponse(Response.Status.NOT_FOUND, "Unidad no encontrada");
            }

            if (!RoleConstants.ADMIN.equals(role)) {
                Long currentUserId = authenticatedUserContext.getCurrentUser().getUserId();
                boolean isOwner = existing.getUser().getUserId().equals(currentUserId);
                boolean isRevision = STATUS_REVISION.equals(existing.getStatus().getStatusId());
                if (!isOwner || !isRevision) {
                    return errorResponse(Response.Status.FORBIDDEN, "No tienes permiso para eliminar esta unidad");
                }
            }

            deleteUnitUseCase.execute(id);
            return Response.noContent().build();
        } catch (IllegalArgumentException e) {
            return errorResponse(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (RuntimeException e) {
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }

    private GetUnitResponseDto toResponseDto(Unit unit) {
        return new GetUnitResponseDto(
                unit.getUnitId(),
                unit.getName(),
                unit.getUser().getUserId(),
                unit.getStatus().getStatusId(),
                unit.getStatus().getName()
        );
    }
}