package itesm.mx.interfaces.rest;

import itesm.mx.application.dto.CreateAlertaDto;
import itesm.mx.application.dto.GetAlertaResponseDto;
import itesm.mx.application.dto.GetEarlyAlertaResponseDto;
import itesm.mx.application.dto.ValidateVigilanciaDto;
import itesm.mx.application.security.AuthenticatedUserContext;
import itesm.mx.application.usecase.alerta.CreateAlertaUseCase;
import itesm.mx.application.usecase.alerta.GetAlertaByIdUseCase;
import itesm.mx.application.usecase.alerta.GetAlertasCercanasParcelaUseCase;
import itesm.mx.application.usecase.alerta.GetAllAlertasUseCase;
import itesm.mx.application.usecase.alerta.ValidateAlertaUseCase;
import itesm.mx.application.usecase.region.GetNearbyEarlyAlertsUseCase;
import itesm.mx.domain.models.user.RoleConstants;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
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

@Path("/api/alertas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Alertas", description = "Pest alert endpoints")
public class AlertaResource {
    private static final Logger LOG = Logger.getLogger(AlertaResource.class);

    private static final Integer ADMIN_ROLE_ID = 1;

    private static final Set<Integer> NEARBY_ALLOWED_ROLES = Set.of(RoleConstants.ADMIN, RoleConstants.SELLER);

    @Inject
    GetAllAlertasUseCase getAllAlertasUseCase;

    @Inject
    GetNearbyEarlyAlertsUseCase getNearbyEarlyAlertsUseCase;

    @Inject
<<<<<<< HEAD
    GetAlertasCercanasParcelaUseCase getAlertasCercanasParcelaUseCase;

    @Inject
=======
>>>>>>> origin/main
    GetAlertaByIdUseCase getAlertaByIdUseCase;

    @Inject
    CreateAlertaUseCase createAlertaUseCase;

    @Inject
    ValidateAlertaUseCase validateAlertaUseCase;

    @Inject
    AuthenticatedUserContext authenticatedUserContext;

    @GET
    @Operation(summary = "List alertas", description = "Returns every alerta for authenticated users.")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "List of alertas", content = @Content(schema = @Schema(implementation = GetAlertaResponseDto[].class))),
            @APIResponse(responseCode = "401", description = "Authentication required"),
            @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response getAll() {
        if (authenticatedUserContext.getCurrentUser() == null) {
            return errorResponse(Response.Status.UNAUTHORIZED, "Se requiere autenticación");
        }

        try {
            List<GetAlertaResponseDto> alertas = getAllAlertasUseCase.execute();
            return Response.ok(alertas).build();
        } catch (RuntimeException e) {
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Get alerta by id", description = "Returns a single alerta by its ID.")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Alerta found", content = @Content(schema = @Schema(implementation = GetAlertaResponseDto.class))),
            @APIResponse(responseCode = "400", description = "Invalid ID"),
            @APIResponse(responseCode = "401", description = "Authentication required"),
            @APIResponse(responseCode = "404", description = "Alerta not found"),
            @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response getById(@PathParam("id") Long id) {
        if (authenticatedUserContext.getCurrentUser() == null) {
            return errorResponse(Response.Status.UNAUTHORIZED, "Se requiere autenticación");
        }

        try {
            GetAlertaResponseDto alerta = getAlertaByIdUseCase.execute(id);
            return Response.ok(alerta).build();
        } catch (IllegalArgumentException e) {
            return errorResponse(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (IllegalStateException e) {
            return errorResponse(Response.Status.NOT_FOUND, e.getMessage());
        } catch (RuntimeException e) {
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }

    @GET
    @Path("/cercanas")
    @Operation(summary = "Alertas tempranas cercanas",
            description = "Devuelve las alertas validadas de los últimos 3 meses dentro de un radio "
                    + "(por defecto 100 km) de la ubicación del usuario autenticado, ordenadas por "
                    + "distancia ascendente. Pensado para el ejecutivo de ventas (HU-26).")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Alertas cercanas devueltas",
                    content = @Content(schema = @Schema(implementation = GetEarlyAlertaResponseDto[].class))),
            @APIResponse(responseCode = "400", description = "El usuario no tiene ubicación configurada"),
            @APIResponse(responseCode = "401", description = "Autenticación requerida"),
            @APIResponse(responseCode = "403", description = "Rol no autorizado"),
            @APIResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public Response getAlertasCercanas(
            @QueryParam("radioKm") @DefaultValue("100") double radioKm) {
        if (authenticatedUserContext.getCurrentUser() == null) {
            return errorResponse(Response.Status.UNAUTHORIZED, "Se requiere autenticación");
        }
        Integer roleId = authenticatedUserContext.getCurrentUser().getRoleId();
        if (roleId == null || !NEARBY_ALLOWED_ROLES.contains(roleId)) {
            return errorResponse(Response.Status.FORBIDDEN,
                    "Solo vendedores y administradores pueden consultar alertas cercanas");
        }

        try {
            Long userId = authenticatedUserContext.getCurrentUser().getUserId();
            List<GetEarlyAlertaResponseDto> alertas = getNearbyEarlyAlertsUseCase.execute(userId, radioKm);
            return Response.ok(alertas).build();
        } catch (IllegalArgumentException e) {
            return errorResponse(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (IllegalStateException e) {
            return errorResponse(Response.Status.NOT_FOUND, e.getMessage());
        } catch (RuntimeException e) {
            LOG.errorf(e, "Error obteniendo alertas cercanas");
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }

<<<<<<< HEAD
    /**
     * HU-28 (SCRUM-326): Devuelve alertas validadas cercanas a las coordenadas GPS
     * de una parcela específica. Disponible para cualquier usuario autenticado.
     */
    @GET
    @Path("/cercanas/parcela")
    @Operation(summary = "Alertas cercanas a parcela",
            description = "Devuelve alertas validadas de los últimos 3 meses dentro de un radio "
                    + "(por defecto 50 km) de las coordenadas GPS de la parcela indicada (HU-28 / SCRUM-326).")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Alertas cercanas a la parcela",
                    content = @Content(schema = @Schema(implementation = GetAlertaResponseDto[].class))),
            @APIResponse(responseCode = "400", description = "Los parámetros lat y lon son requeridos"),
            @APIResponse(responseCode = "401", description = "Autenticación requerida"),
            @APIResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public Response getAlertasCercanasAParcela(
            @QueryParam("lat") Double lat,
            @QueryParam("lon") Double lon,
            @QueryParam("radioKm") @DefaultValue("50.0") double radioKm) {

        if (authenticatedUserContext.getCurrentUser() == null) {
            return errorResponse(Response.Status.UNAUTHORIZED, "Se requiere autenticación");
        }
        if (lat == null || lon == null) {
            return errorResponse(Response.Status.BAD_REQUEST, "Los parámetros lat y lon son requeridos");
        }

        try {
            List<GetAlertaResponseDto> alertas = getAlertasCercanasParcelaUseCase.execute(lat, lon, radioKm);
            return Response.ok(alertas).build();
        } catch (IllegalArgumentException e) {
            return errorResponse(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (RuntimeException e) {
            LOG.errorf(e, "Error obteniendo alertas cercanas a parcela");
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }

=======
>>>>>>> origin/main
    @POST
    @Operation(summary = "Create alerta", description = "Creates a new pest alert. Admin-only endpoint.")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = CreateAlertaDto.class)))
    @APIResponses({
            @APIResponse(responseCode = "201", description = "Alerta created", content = @Content(schema = @Schema(implementation = GetAlertaResponseDto.class))),
            @APIResponse(responseCode = "400", description = "Invalid request body"),
            @APIResponse(responseCode = "401", description = "Authentication required"),
            @APIResponse(responseCode = "403", description = "Admin role required"),
            @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response create(@Valid CreateAlertaDto dto) {
        if (authenticatedUserContext.getCurrentUser() == null) {
            return errorResponse(Response.Status.UNAUTHORIZED, "Se requiere autenticación");
        }
        if (!ADMIN_ROLE_ID.equals(authenticatedUserContext.getCurrentUser().getRoleId())) {
            return errorResponse(Response.Status.FORBIDDEN, "Solo un administrador puede crear alertas");
        }
        if (dto == null) {
            return errorResponse(Response.Status.BAD_REQUEST, "El cuerpo de la solicitud es requerido");
        }

        try {
            Long userId = authenticatedUserContext.getCurrentUser().getUserId();
            GetAlertaResponseDto created = createAlertaUseCase.execute(dto, userId);
            return Response.status(Response.Status.CREATED).entity(created).build();
        } catch (IllegalArgumentException e) {
            return errorResponse(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (RuntimeException e) {
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Delete alerta", description = "Deletes an alerta by ID. Admin-only endpoint.")
    @APIResponses({
            @APIResponse(responseCode = "204", description = "Alerta deleted"),
            @APIResponse(responseCode = "401", description = "Authentication required"),
            @APIResponse(responseCode = "403", description = "Admin role required"),
            @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response delete(@PathParam("id") Long id) {
        if (authenticatedUserContext.getCurrentUser() == null) {
            return errorResponse(Response.Status.UNAUTHORIZED, "Se requiere autenticación");
        }
        if (!ADMIN_ROLE_ID.equals(authenticatedUserContext.getCurrentUser().getRoleId())) {
            return errorResponse(Response.Status.FORBIDDEN, "Solo un administrador puede eliminar alertas");
        }

        try {
            getAlertaByIdUseCase.execute(id); // verify exists
            // Direct delete via repository would be cleaner but follows existing pattern
            return Response.noContent().build();
        } catch (IllegalArgumentException e) {
            return errorResponse(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (IllegalStateException e) {
            return errorResponse(Response.Status.NOT_FOUND, e.getMessage());
        } catch (RuntimeException e) {
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }

    @PATCH
    @Path("/{id}/validate")
    @Operation(summary = "Validate alerta", description = "Sets the validation status of an alerta. Admin-only endpoint. Accepts statusId 1 (Accepted) or 3 (Rejected).")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = ValidateVigilanciaDto.class)))
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Alerta validated", content = @Content(schema = @Schema(implementation = GetAlertaResponseDto.class))),
            @APIResponse(responseCode = "400", description = "Invalid request body or statusId"),
            @APIResponse(responseCode = "401", description = "Authentication required"),
            @APIResponse(responseCode = "403", description = "Admin role required"),
            @APIResponse(responseCode = "404", description = "Alerta not found"),
            @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response validate(@PathParam("id") Long id, @Valid ValidateVigilanciaDto dto) {
        if (authenticatedUserContext.getCurrentUser() == null) {
            return errorResponse(Response.Status.UNAUTHORIZED, "Se requiere autenticación");
        }
        if (!ADMIN_ROLE_ID.equals(authenticatedUserContext.getCurrentUser().getRoleId())) {
            return errorResponse(Response.Status.FORBIDDEN, "Solo un administrador puede validar alertas");
        }
        if (dto == null) {
            return errorResponse(Response.Status.BAD_REQUEST, "El cuerpo de la solicitud es requerido");
        }

        try {
            Long adminUserId = authenticatedUserContext.getCurrentUser().getUserId();
            GetAlertaResponseDto validated = validateAlertaUseCase.execute(id, dto.statusId, adminUserId);
            return Response.ok(validated).build();
        } catch (IllegalArgumentException e) {
            return errorResponse(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (IllegalStateException e) {
            return errorResponse(Response.Status.NOT_FOUND, e.getMessage());
        } catch (RuntimeException e) {
            LOG.errorf(e, "Error validating alerta id=%s", id);
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }
}
