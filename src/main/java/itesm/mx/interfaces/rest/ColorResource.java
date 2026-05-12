package itesm.mx.interfaces.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import itesm.mx.application.dto.GetColorResponseDto;
import itesm.mx.application.dto.RegisterColorDto;
import itesm.mx.application.security.AuthenticatedUserContext;
import itesm.mx.application.usecase.marketplace.color.DeleteColorUseCase;
import itesm.mx.application.usecase.marketplace.color.GetAllColorsUseCase;
import itesm.mx.application.usecase.marketplace.color.RegisterColorUseCase;
import itesm.mx.domain.models.marketplace.Color;
import itesm.mx.domain.models.user.RoleConstants;

import java.util.List;

import static itesm.mx.interfaces.rest.utils.ErrorResponseUtils.errorResponse;

@Path("/api/colors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ColorResource {

    @Inject
    RegisterColorUseCase registerColorUseCase;

    @Inject
    GetAllColorsUseCase getAllColorsUseCase;

    @Inject
    DeleteColorUseCase deleteColorUseCase;

    @Inject
    AuthenticatedUserContext authenticatedUserContext;

    @GET
    public Response getAllColors() {
        if (authenticatedUserContext.getCurrentUser() == null) {
            return errorResponse(Response.Status.UNAUTHORIZED, "Se requiere autenticacion");
        }

        Integer role = authenticatedUserContext.getCurrentUser().getRoleId();
        if (!RoleConstants.ADMIN.equals(role) && !RoleConstants.SELLER.equals(role)) {
            return errorResponse(Response.Status.FORBIDDEN, "Solo un administrador o tecnico vendedor puede consultar los colores");
        }

        try {
            List<GetColorResponseDto> colors = getAllColorsUseCase.execute()
                    .stream()
                    .map(c -> new GetColorResponseDto(c.getColorId(), c.getName(), c.getHexa()))
                    .toList();
            return Response.ok(colors).build();
        } catch (RuntimeException e) {
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }

    @POST
    public Response registerColor(RegisterColorDto dto) {
        if (dto == null) {
            return errorResponse(Response.Status.BAD_REQUEST, "El cuerpo de la solicitud es requerido");
        }
        if (authenticatedUserContext.getCurrentUser() == null) {
            return errorResponse(Response.Status.UNAUTHORIZED, "Se requiere autenticacion");
        }
        if (!RoleConstants.ADMIN.equals(authenticatedUserContext.getCurrentUser().getRoleId())) {
            return errorResponse(Response.Status.FORBIDDEN, "Solo un administrador puede registrar colores");
        }

        try {
            Color created = registerColorUseCase.execute(new Color(null, dto.name, dto.hexa));
            return Response.status(Response.Status.CREATED)
                    .entity(new GetColorResponseDto(created.getColorId(), created.getName(), created.getHexa()))
                    .build();
        } catch (IllegalArgumentException e) {
            return errorResponse(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (RuntimeException e) {
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }

    @DELETE
    @Path("/{id}")
    public Response deleteColor(@PathParam("id") Long id) {
        if (authenticatedUserContext.getCurrentUser() == null) {
            return errorResponse(Response.Status.UNAUTHORIZED, "Se requiere autenticacion");
        }
        if (!RoleConstants.ADMIN.equals(authenticatedUserContext.getCurrentUser().getRoleId())) {
            return errorResponse(Response.Status.FORBIDDEN, "Solo un administrador puede eliminar colores");
        }

        try {
            deleteColorUseCase.execute(id);
            return Response.noContent().build();
        } catch (IllegalArgumentException e) {
            return errorResponse(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (RuntimeException e) {
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }
}