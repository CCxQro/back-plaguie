package itesm.mx.interfaces.rest;

import itesm.mx.application.dto.CreateRegionInteresDto;
import itesm.mx.application.dto.GetAlertaResponseDto;
import itesm.mx.application.dto.GetRegionInteresResponseDto;
import itesm.mx.application.security.AuthenticatedUserContext;
import itesm.mx.application.usecase.region.CreateRegionInteresUseCase;
import itesm.mx.application.usecase.region.DeleteRegionInteresUseCase;
import itesm.mx.application.usecase.region.GetAlertasByRegionesInteresUseCase;
import itesm.mx.application.usecase.region.GetRegionesInteresByUserUseCase;
import itesm.mx.domain.models.user.RoleConstants;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Set;
import org.jboss.logging.Logger;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import static itesm.mx.interfaces.rest.utils.ErrorResponseUtils.errorResponse;

/**
 * HU-26: regiones de interés del vendedor y alertas tempranas en ellas.
 * Solo accesible por vendedores (roleId=3) y administradores (roleId=1).
 */
@Path("/api/regiones-interes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Regiones de interés", description = "Regiones de interés del vendedor y alertas tempranas (HU-26)")
public class RegionInteresResource {

    private static final Logger LOG = Logger.getLogger(RegionInteresResource.class);

    private static final Set<Integer> ALLOWED_ROLE_IDS = Set.of(RoleConstants.ADMIN, RoleConstants.SELLER);

    @Inject
    GetRegionesInteresByUserUseCase getRegionesInteresByUserUseCase;

    @Inject
    CreateRegionInteresUseCase createRegionInteresUseCase;

    @Inject
    DeleteRegionInteresUseCase deleteRegionInteresUseCase;

    @Inject
    GetAlertasByRegionesInteresUseCase getAlertasByRegionesInteresUseCase;

    @Inject
    AuthenticatedUserContext authenticatedUserContext;

    @GET
    @Operation(summary = "Listar regiones de interés",
            description = "Devuelve las regiones de interés configuradas por el vendedor autenticado.")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Regiones devueltas",
                    content = @Content(schema = @Schema(implementation = GetRegionInteresResponseDto[].class))),
            @APIResponse(responseCode = "401", description = "Autenticación requerida"),
            @APIResponse(responseCode = "403", description = "Rol no autorizado"),
            @APIResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public Response getMisRegiones() {
        Response auth = enforceAuth();
        if (auth != null) {
            return auth;
        }
        try {
            Long userId = authenticatedUserContext.getCurrentUser().getUserId();
            return Response.ok(getRegionesInteresByUserUseCase.execute(userId)).build();
        } catch (IllegalArgumentException e) {
            return errorResponse(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (RuntimeException e) {
            LOG.errorf(e, "Error listando regiones de interés");
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }

    @POST
    @Operation(summary = "Agregar región de interés",
            description = "Configura un estado como región de interés para el vendedor autenticado.")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = CreateRegionInteresDto.class)))
    @APIResponses({
            @APIResponse(responseCode = "201", description = "Región creada",
                    content = @Content(schema = @Schema(implementation = GetRegionInteresResponseDto.class))),
            @APIResponse(responseCode = "400", description = "Cuerpo inválido o estado inexistente"),
            @APIResponse(responseCode = "401", description = "Autenticación requerida"),
            @APIResponse(responseCode = "403", description = "Rol no autorizado"),
            @APIResponse(responseCode = "409", description = "El estado ya está configurado"),
            @APIResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public Response agregarRegion(@Valid CreateRegionInteresDto dto) {
        Response auth = enforceAuth();
        if (auth != null) {
            return auth;
        }
        if (dto == null) {
            return errorResponse(Response.Status.BAD_REQUEST, "El cuerpo de la solicitud es requerido");
        }
        try {
            Long userId = authenticatedUserContext.getCurrentUser().getUserId();
            GetRegionInteresResponseDto created = createRegionInteresUseCase.execute(userId, dto);
            return Response.status(Response.Status.CREATED).entity(created).build();
        } catch (IllegalArgumentException e) {
            return errorResponse(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (IllegalStateException e) {
            return errorResponse(Response.Status.CONFLICT, e.getMessage());
        } catch (RuntimeException e) {
            LOG.errorf(e, "Error agregando región de interés");
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Eliminar región de interés",
            description = "Elimina una región de interés del vendedor autenticado.")
    @APIResponses({
            @APIResponse(responseCode = "204", description = "Región eliminada"),
            @APIResponse(responseCode = "400", description = "ID inválido"),
            @APIResponse(responseCode = "401", description = "Autenticación requerida"),
            @APIResponse(responseCode = "403", description = "Rol no autorizado"),
            @APIResponse(responseCode = "404", description = "Región no encontrada"),
            @APIResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public Response eliminarRegion(@PathParam("id") Long id) {
        Response auth = enforceAuth();
        if (auth != null) {
            return auth;
        }
        try {
            Long userId = authenticatedUserContext.getCurrentUser().getUserId();
            deleteRegionInteresUseCase.execute(userId, id);
            return Response.noContent().build();
        } catch (IllegalArgumentException e) {
            return errorResponse(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (IllegalStateException e) {
            return errorResponse(Response.Status.NOT_FOUND, e.getMessage());
        } catch (RuntimeException e) {
            LOG.errorf(e, "Error eliminando región de interés id=%s", id);
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }

    @GET
    @Path("/alertas")
    @Operation(summary = "Alertas tempranas en mis regiones",
            description = "Devuelve las alertas validadas ubicadas en los estados configurados como regiones de interés del vendedor.")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Alertas devueltas",
                    content = @Content(schema = @Schema(implementation = GetAlertaResponseDto[].class))),
            @APIResponse(responseCode = "401", description = "Autenticación requerida"),
            @APIResponse(responseCode = "403", description = "Rol no autorizado"),
            @APIResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public Response getAlertasTempranas() {
        Response auth = enforceAuth();
        if (auth != null) {
            return auth;
        }
        try {
            Long userId = authenticatedUserContext.getCurrentUser().getUserId();
            List<GetAlertaResponseDto> alertas = getAlertasByRegionesInteresUseCase.execute(userId);
            return Response.ok(alertas).build();
        } catch (IllegalArgumentException e) {
            return errorResponse(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (RuntimeException e) {
            LOG.errorf(e, "Error obteniendo alertas tempranas por región");
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }

    private Response enforceAuth() {
        if (authenticatedUserContext.getCurrentUser() == null) {
            return errorResponse(Response.Status.UNAUTHORIZED, "Se requiere autenticación");
        }
        Integer roleId = authenticatedUserContext.getCurrentUser().getRoleId();
        if (roleId == null || !ALLOWED_ROLE_IDS.contains(roleId)) {
            return errorResponse(Response.Status.FORBIDDEN,
                    "Solo vendedores y administradores pueden gestionar regiones de interés");
        }
        return null;
    }
}
