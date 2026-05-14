package itesm.mx.interfaces.rest;

import itesm.mx.application.dto.CreateVigilanciaFitosanitariaDto;
import itesm.mx.application.dto.GetVigilanciaFitosanitariaResponseDto;
import itesm.mx.application.dto.UpdateVigilanciaFitosanitariaDto;
import itesm.mx.application.dto.ValidateVigilanciaDto;
import itesm.mx.application.security.AuthenticatedUserContext;
import itesm.mx.application.usecase.vigilancia.CreateVigilanciaFitosanitariaUseCase;
import itesm.mx.application.usecase.vigilancia.DeleteVigilanciaFitosanitariaUseCase;
import itesm.mx.application.usecase.vigilancia.GetAllVigilanciasFitosanitariasUseCase;
import itesm.mx.application.usecase.vigilancia.GetVigilanciaFitosanitariaByIdUseCase;
import itesm.mx.application.usecase.vigilancia.UpdateVigilanciaFitosanitariaUseCase;
import itesm.mx.application.usecase.vigilancia.ValidateVigilanciaFitosanitariaUseCase;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
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

@Path("/api/vigilancias-fitosanitarias")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Vigilancia Fitosanitaria", description = "Fitosanitary monitoring endpoints")
public class VigilanciaFitosanitariaResource {
    private static final Logger LOG = Logger.getLogger(VigilanciaFitosanitariaResource.class);

    private static final Integer ADMIN_ROLE_ID = 1;

    @Inject
    GetAllVigilanciasFitosanitariasUseCase getAllVigilanciasFitosanitariasUseCase;

    @Inject
    GetVigilanciaFitosanitariaByIdUseCase getVigilanciaFitosanitariaByIdUseCase;

    @Inject
    CreateVigilanciaFitosanitariaUseCase createVigilanciaFitosanitariaUseCase;

    @Inject
    UpdateVigilanciaFitosanitariaUseCase updateVigilanciaFitosanitariaUseCase;

    @Inject
    DeleteVigilanciaFitosanitariaUseCase deleteVigilanciaFitosanitariaUseCase;

    @Inject
    ValidateVigilanciaFitosanitariaUseCase validateVigilanciaFitosanitariaUseCase;

    @Inject
    AuthenticatedUserContext authenticatedUserContext;

    @GET
    @Operation(summary = "List vigilancias", description = "Returns every vigilancia fitosanitaria for authenticated users.")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Vigilancias returned", content = @Content(schema = @Schema(implementation = GetVigilanciaFitosanitariaResponseDto[].class))),
            @APIResponse(responseCode = "401", description = "Authentication required"),
            @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response getAllVigilanciasFitosanitarias() {
        if (authenticatedUserContext.getCurrentUser() == null) {
            return errorResponse(Response.Status.UNAUTHORIZED, "Se requiere autenticación");
        }

        try {
            List<GetVigilanciaFitosanitariaResponseDto> vigilancias = getAllVigilanciasFitosanitariasUseCase.execute();
            return Response.ok(vigilancias).build();
        } catch (RuntimeException e) {
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Get vigilancia by id", description = "Returns a single vigilancia fitosanitaria by identifier.")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Vigilancia returned", content = @Content(schema = @Schema(implementation = GetVigilanciaFitosanitariaResponseDto.class))),
            @APIResponse(responseCode = "400", description = "Invalid vigilancia id"),
            @APIResponse(responseCode = "401", description = "Authentication required"),
            @APIResponse(responseCode = "404", description = "Vigilancia not found"),
            @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response getVigilanciaFitosanitariaById(@PathParam("id") Long id) {
        if (authenticatedUserContext.getCurrentUser() == null) {
            return errorResponse(Response.Status.UNAUTHORIZED, "Se requiere autenticación");
        }

        try {
            GetVigilanciaFitosanitariaResponseDto vigilancia = getVigilanciaFitosanitariaByIdUseCase.execute(id);
            return Response.ok(vigilancia).build();
        } catch (IllegalArgumentException e) {
            return errorResponse(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (IllegalStateException e) {
            return errorResponse(Response.Status.NOT_FOUND, e.getMessage());
        } catch (RuntimeException e) {
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }

    @POST
    @Operation(summary = "Create vigilancia", description = "Creates a vigilancia fitosanitaria. Admin-only endpoint.")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = CreateVigilanciaFitosanitariaDto.class)))
    @APIResponses({
            @APIResponse(responseCode = "201", description = "Vigilancia created", content = @Content(schema = @Schema(implementation = GetVigilanciaFitosanitariaResponseDto.class))),
            @APIResponse(responseCode = "400", description = "Invalid or missing request body"),
            @APIResponse(responseCode = "401", description = "Authentication required"),
            @APIResponse(responseCode = "403", description = "Admin role required"),
            @APIResponse(responseCode = "409", description = "Vigilancia already exists or business conflict"),
            @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response createVigilanciaFitosanitaria(@Valid CreateVigilanciaFitosanitariaDto dto) {
        if (authenticatedUserContext.getCurrentUser() == null) {
            return errorResponse(Response.Status.UNAUTHORIZED, "Se requiere autenticación");
        }
        if (!ADMIN_ROLE_ID.equals(authenticatedUserContext.getCurrentUser().getRoleId())) {
            return errorResponse(Response.Status.FORBIDDEN, "Solo un administrador puede crear vigilancias fitosanitarias");
        }
        if (dto == null) {
            return errorResponse(Response.Status.BAD_REQUEST, "El cuerpo de la solicitud es requerido");
        }

        try {
            GetVigilanciaFitosanitariaResponseDto created = createVigilanciaFitosanitariaUseCase.execute(dto);
            return Response.status(Response.Status.CREATED).entity(created).build();
        } catch (IllegalArgumentException e) {
            return errorResponse(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (IllegalStateException e) {
            return errorResponse(Response.Status.CONFLICT, e.getMessage());
        } catch (RuntimeException e) {
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "Update vigilancia", description = "Updates a vigilancia fitosanitaria. Admin-only endpoint.")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = UpdateVigilanciaFitosanitariaDto.class)))
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Vigilancia updated", content = @Content(schema = @Schema(implementation = GetVigilanciaFitosanitariaResponseDto.class))),
            @APIResponse(responseCode = "400", description = "Invalid or missing request body"),
            @APIResponse(responseCode = "401", description = "Authentication required"),
            @APIResponse(responseCode = "403", description = "Admin role required"),
            @APIResponse(responseCode = "404", description = "Vigilancia not found"),
            @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response updateVigilanciaFitosanitaria(@PathParam("id") Long id, @Valid UpdateVigilanciaFitosanitariaDto dto) {
        if (authenticatedUserContext.getCurrentUser() == null) {
            return errorResponse(Response.Status.UNAUTHORIZED, "Se requiere autenticación");
        }
        if (!ADMIN_ROLE_ID.equals(authenticatedUserContext.getCurrentUser().getRoleId())) {
            return errorResponse(Response.Status.FORBIDDEN, "Solo un administrador puede actualizar vigilancias fitosanitarias");
        }
        if (dto == null) {
            return errorResponse(Response.Status.BAD_REQUEST, "El cuerpo de la solicitud es requerido");
        }

        try {
            GetVigilanciaFitosanitariaResponseDto updated = updateVigilanciaFitosanitariaUseCase.execute(id, dto);
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
    @Operation(summary = "Delete vigilancia", description = "Deletes a vigilancia fitosanitaria. Admin-only endpoint.")
    @APIResponses({
            @APIResponse(responseCode = "204", description = "Vigilancia deleted"),
            @APIResponse(responseCode = "400", description = "Invalid vigilancia id"),
            @APIResponse(responseCode = "401", description = "Authentication required"),
            @APIResponse(responseCode = "403", description = "Admin role required"),
            @APIResponse(responseCode = "404", description = "Vigilancia not found"),
            @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response deleteVigilanciaFitosanitaria(@PathParam("id") Long id) {
        if (authenticatedUserContext.getCurrentUser() == null) {
            return errorResponse(Response.Status.UNAUTHORIZED, "Se requiere autenticación");
        }
        if (!ADMIN_ROLE_ID.equals(authenticatedUserContext.getCurrentUser().getRoleId())) {
            return errorResponse(Response.Status.FORBIDDEN, "Solo un administrador puede eliminar vigilancias fitosanitarias");
        }

        try {
            deleteVigilanciaFitosanitariaUseCase.execute(id);
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
    @Operation(summary = "Validate vigilancia", description = "Sets the validation status of a vigilancia fitosanitaria. Admin-only endpoint. Accepts statusId 1 (Accepted) or 3 (Rejected).")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = ValidateVigilanciaDto.class)))
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Vigilancia validated", content = @Content(schema = @Schema(implementation = GetVigilanciaFitosanitariaResponseDto.class))),
            @APIResponse(responseCode = "400", description = "Invalid request body or statusId"),
            @APIResponse(responseCode = "401", description = "Authentication required"),
            @APIResponse(responseCode = "403", description = "Admin role required"),
            @APIResponse(responseCode = "404", description = "Vigilancia not found"),
            @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response validateVigilanciaFitosanitaria(@PathParam("id") Long id, @Valid ValidateVigilanciaDto dto) {
        if (authenticatedUserContext.getCurrentUser() == null) {
            return errorResponse(Response.Status.UNAUTHORIZED, "Se requiere autenticación");
        }
        if (!ADMIN_ROLE_ID.equals(authenticatedUserContext.getCurrentUser().getRoleId())) {
            return errorResponse(Response.Status.FORBIDDEN, "Solo un administrador puede validar vigilancias fitosanitarias");
        }
        if (dto == null) {
            return errorResponse(Response.Status.BAD_REQUEST, "El cuerpo de la solicitud es requerido");
        }

        try {
            Long adminUserId = authenticatedUserContext.getCurrentUser().getUserId();
            GetVigilanciaFitosanitariaResponseDto validated = validateVigilanciaFitosanitariaUseCase.execute(id, dto.statusId, adminUserId);
            return Response.ok(validated).build();
        } catch (IllegalArgumentException e) {
            return errorResponse(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (IllegalStateException e) {
            return errorResponse(Response.Status.NOT_FOUND, e.getMessage());
        } catch (RuntimeException e) {
            LOG.errorf(e, "Error validating vigilancia fitosanitaria id=%s", id);
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }
}
