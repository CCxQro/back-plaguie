package itesm.mx.interfaces.rest;

import itesm.mx.application.dto.CreateAlertaDto;
import itesm.mx.application.dto.GetAlertaResponseDto;
import itesm.mx.application.dto.ValidateVigilanciaDto;
import itesm.mx.application.security.AuthenticatedUserContext;
import itesm.mx.application.usecase.alerta.CreateAlertaUseCase;
import itesm.mx.application.usecase.alerta.GetAlertaByIdUseCase;
import itesm.mx.application.usecase.alerta.GetAllAlertasUseCase;
import itesm.mx.application.usecase.alerta.ValidateAlertaUseCase;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
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

    @Inject
    GetAllAlertasUseCase getAllAlertasUseCase;

    @Inject
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
