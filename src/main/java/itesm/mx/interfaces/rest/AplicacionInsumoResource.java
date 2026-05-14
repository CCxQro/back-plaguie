package itesm.mx.interfaces.rest;

import itesm.mx.application.dto.AplicacionInsumoResponseDto;
import itesm.mx.application.dto.RegisterAplicacionInsumoDto;
import itesm.mx.application.security.AuthenticatedUserContext;
import itesm.mx.application.usecase.insumo.GetAplicacionesByFarmerUseCase;
import itesm.mx.application.usecase.insumo.RegisterAplicacionInsumoUseCase;
import itesm.mx.domain.models.user.Farmer;
import itesm.mx.domain.repository.user.FarmerRepository;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.Optional;

import static itesm.mx.interfaces.rest.utils.ErrorResponseUtils.errorResponse;

@Path("/api/insumos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Insumos", description = "Aplicaciones de insumos agrícolas")
public class AplicacionInsumoResource {

    private static final Integer FARMER_ROLE_ID = 2;

    @Inject
    RegisterAplicacionInsumoUseCase registerAplicacionInsumoUseCase;

    @Inject
    GetAplicacionesByFarmerUseCase getAplicacionesByFarmerUseCase;

    @Inject
    AuthenticatedUserContext authenticatedUserContext;

    @Inject
    FarmerRepository farmerRepository;

    @GET
    @Operation(summary = "List aplicaciones de insumos", description = "Returns all aplicaciones de insumos for the authenticated farmer.")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Aplicaciones returned",
                    content = @Content(schema = @Schema(implementation = AplicacionInsumoResponseDto[].class))),
            @APIResponse(responseCode = "401", description = "Authentication required"),
            @APIResponse(responseCode = "403", description = "Farmer role required"),
            @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response getAplicaciones() {
        if (authenticatedUserContext.getCurrentUser() == null) {
            return errorResponse(Response.Status.UNAUTHORIZED, "Se requiere autenticación");
        }
        if (!FARMER_ROLE_ID.equals(authenticatedUserContext.getCurrentUser().getRoleId())) {
            return errorResponse(Response.Status.FORBIDDEN, "Solo un agricultor puede consultar sus aplicaciones de insumos");
        }
        try {
            Long userId = authenticatedUserContext.getCurrentUser().getUserId();
            Optional<Farmer> farmerOpt = farmerRepository.findByIdUser(userId);
            if (farmerOpt.isEmpty()) {
                return errorResponse(Response.Status.NOT_FOUND, "Agricultor no encontrado");
            }
            Long farmerId = farmerOpt.get().getFarmerId();
            return Response.ok(getAplicacionesByFarmerUseCase.execute(farmerId)).build();
        } catch (IllegalArgumentException e) {
            return errorResponse(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (RuntimeException e) {
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }

    @POST
    @Operation(summary = "Register aplicacion de insumo", description = "Registers a new aplicacion de insumo for the authenticated farmer.")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = RegisterAplicacionInsumoDto.class)))
    @APIResponses({
            @APIResponse(responseCode = "201", description = "Aplicacion registered",
                    content = @Content(schema = @Schema(implementation = AplicacionInsumoResponseDto.class))),
            @APIResponse(responseCode = "400", description = "Invalid or missing request body"),
            @APIResponse(responseCode = "401", description = "Authentication required"),
            @APIResponse(responseCode = "403", description = "Farmer role required"),
            @APIResponse(responseCode = "404", description = "Farmer not found"),
            @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response registerAplicacion(@Valid RegisterAplicacionInsumoDto dto) {
        if (authenticatedUserContext.getCurrentUser() == null) {
            return errorResponse(Response.Status.UNAUTHORIZED, "Se requiere autenticación");
        }
        if (!FARMER_ROLE_ID.equals(authenticatedUserContext.getCurrentUser().getRoleId())) {
            return errorResponse(Response.Status.FORBIDDEN, "Solo un agricultor puede registrar aplicaciones de insumos");
        }
        if (dto == null) {
            return errorResponse(Response.Status.BAD_REQUEST, "El cuerpo de la solicitud es requerido");
        }
        try {
            Long userId = authenticatedUserContext.getCurrentUser().getUserId();
            Optional<Farmer> farmerOpt = farmerRepository.findByIdUser(userId);
            if (farmerOpt.isEmpty()) {
                return errorResponse(Response.Status.NOT_FOUND, "Agricultor no encontrado");
            }
            Long farmerId = farmerOpt.get().getFarmerId();
            AplicacionInsumoResponseDto result = registerAplicacionInsumoUseCase.execute(dto, farmerId);
            return Response.status(Response.Status.CREATED).entity(result).build();
        } catch (IllegalArgumentException e) {
            return errorResponse(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (IllegalStateException e) {
            return errorResponse(Response.Status.NOT_FOUND, e.getMessage());
        } catch (RuntimeException e) {
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }
}
