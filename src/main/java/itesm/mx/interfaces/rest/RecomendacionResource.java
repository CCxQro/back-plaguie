package itesm.mx.interfaces.rest;

import itesm.mx.application.dto.CreateRecomendacionDto;
import itesm.mx.application.dto.GetRecomendacionResponseDto;
import itesm.mx.application.dto.ValidateVigilanciaDto;
import itesm.mx.application.security.AuthenticatedUserContext;
import itesm.mx.application.usecase.recomendacion.CreateRecomendacionUseCase;
import itesm.mx.application.usecase.recomendacion.GetRecomendacionByIdUseCase;
import itesm.mx.application.usecase.recomendacion.GetAllRecomendacionesUseCase;
import itesm.mx.application.usecase.recomendacion.ValidateRecomendacionUseCase;
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

@Path("/api/recomendaciones")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Recomendaciones", description = "Pest treatment recommendation endpoints")
public class RecomendacionResource {
    private static final Logger LOG = Logger.getLogger(RecomendacionResource.class);

    private static final Integer ADMIN_ROLE_ID = 1;

    @Inject
    GetAllRecomendacionesUseCase getAllRecomendacionesUseCase;

    @Inject
    GetRecomendacionByIdUseCase getRecomendacionByIdUseCase;

    @Inject
    CreateRecomendacionUseCase createRecomendacionUseCase;

    @Inject
    ValidateRecomendacionUseCase validateRecomendacionUseCase;

    @Inject
    AuthenticatedUserContext authenticatedUserContext;

    @GET
    @Operation(summary = "List recomendaciones", description = "Returns every recomendacion for authenticated users.")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "List of recomendaciones", content = @Content(schema = @Schema(implementation = GetRecomendacionResponseDto[].class))),
            @APIResponse(responseCode = "401", description = "Authentication required"),
            @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response getAll() {
        if (authenticatedUserContext.getCurrentUser() == null) {
            return errorResponse(Response.Status.UNAUTHORIZED, "Se requiere autenticación");
        }

        try {
            List<GetRecomendacionResponseDto> recomendaciones = getAllRecomendacionesUseCase.execute();
            return Response.ok(recomendaciones).build();
        } catch (RuntimeException e) {
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Get recomendacion by id", description = "Returns a single recomendacion by its ID.")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Recomendacion found", content = @Content(schema = @Schema(implementation = GetRecomendacionResponseDto.class))),
            @APIResponse(responseCode = "400", description = "Invalid ID"),
            @APIResponse(responseCode = "401", description = "Authentication required"),
            @APIResponse(responseCode = "404", description = "Recomendacion not found"),
            @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response getById(@PathParam("id") Long id) {
        if (authenticatedUserContext.getCurrentUser() == null) {
            return errorResponse(Response.Status.UNAUTHORIZED, "Se requiere autenticación");
        }

        try {
            GetRecomendacionResponseDto recomendacion = getRecomendacionByIdUseCase.execute(id);
            return Response.ok(recomendacion).build();
        } catch (IllegalArgumentException e) {
            return errorResponse(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (IllegalStateException e) {
            return errorResponse(Response.Status.NOT_FOUND, e.getMessage());
        } catch (RuntimeException e) {
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }

    @POST
    @Operation(summary = "Create recomendacion", description = "Creates a new pest treatment recommendation. Admin-only endpoint.")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = CreateRecomendacionDto.class)))
    @APIResponses({
            @APIResponse(responseCode = "201", description = "Recomendacion created", content = @Content(schema = @Schema(implementation = GetRecomendacionResponseDto.class))),
            @APIResponse(responseCode = "400", description = "Invalid request body"),
            @APIResponse(responseCode = "401", description = "Authentication required"),
            @APIResponse(responseCode = "403", description = "Admin role required"),
            @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response create(@Valid CreateRecomendacionDto dto) {
        if (authenticatedUserContext.getCurrentUser() == null) {
            return errorResponse(Response.Status.UNAUTHORIZED, "Se requiere autenticación");
        }
        if (!ADMIN_ROLE_ID.equals(authenticatedUserContext.getCurrentUser().getRoleId())) {
            return errorResponse(Response.Status.FORBIDDEN, "Solo un administrador puede crear recomendaciones");
        }
        if (dto == null) {
            return errorResponse(Response.Status.BAD_REQUEST, "El cuerpo de la solicitud es requerido");
        }

        try {
            Long userId = authenticatedUserContext.getCurrentUser().getUserId();
            GetRecomendacionResponseDto created = createRecomendacionUseCase.execute(dto, userId);
            return Response.status(Response.Status.CREATED).entity(created).build();
        } catch (IllegalArgumentException e) {
            return errorResponse(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (RuntimeException e) {
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Delete recomendacion", description = "Deletes a recomendacion by ID. Admin-only endpoint.")
    @APIResponses({
            @APIResponse(responseCode = "204", description = "Recomendacion deleted"),
            @APIResponse(responseCode = "401", description = "Authentication required"),
            @APIResponse(responseCode = "403", description = "Admin role required"),
            @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response delete(@PathParam("id") Long id) {
        if (authenticatedUserContext.getCurrentUser() == null) {
            return errorResponse(Response.Status.UNAUTHORIZED, "Se requiere autenticación");
        }
        if (!ADMIN_ROLE_ID.equals(authenticatedUserContext.getCurrentUser().getRoleId())) {
            return errorResponse(Response.Status.FORBIDDEN, "Solo un administrador puede eliminar recomendaciones");
        }

        try {
            getRecomendacionByIdUseCase.execute(id); // verify exists
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
    @Operation(summary = "Validate recomendacion", description = "Sets the validation status of a recomendacion. Admin-only endpoint. Accepts statusId 1 (Accepted) or 3 (Rejected).")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = ValidateVigilanciaDto.class)))
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Recomendacion validated", content = @Content(schema = @Schema(implementation = GetRecomendacionResponseDto.class))),
            @APIResponse(responseCode = "400", description = "Invalid request body or statusId"),
            @APIResponse(responseCode = "401", description = "Authentication required"),
            @APIResponse(responseCode = "403", description = "Admin role required"),
            @APIResponse(responseCode = "404", description = "Recomendacion not found"),
            @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response validate(@PathParam("id") Long id, @Valid ValidateVigilanciaDto dto) {
        if (authenticatedUserContext.getCurrentUser() == null) {
            return errorResponse(Response.Status.UNAUTHORIZED, "Se requiere autenticación");
        }
        if (!ADMIN_ROLE_ID.equals(authenticatedUserContext.getCurrentUser().getRoleId())) {
            return errorResponse(Response.Status.FORBIDDEN, "Solo un administrador puede validar recomendaciones");
        }
        if (dto == null) {
            return errorResponse(Response.Status.BAD_REQUEST, "El cuerpo de la solicitud es requerido");
        }

        try {
            Long adminUserId = authenticatedUserContext.getCurrentUser().getUserId();
            GetRecomendacionResponseDto validated = validateRecomendacionUseCase.execute(id, dto.statusId, adminUserId);
            return Response.ok(validated).build();
        } catch (IllegalArgumentException e) {
            return errorResponse(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (IllegalStateException e) {
            return errorResponse(Response.Status.NOT_FOUND, e.getMessage());
        } catch (RuntimeException e) {
            LOG.errorf(e, "Error validating recomendacion id=%s", id);
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }
}
