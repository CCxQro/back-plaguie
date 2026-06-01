package itesm.mx.interfaces.rest;

import itesm.mx.application.dto.ParcelaCatalogItemDto;
import itesm.mx.application.dto.ParcelaResponseDto;
import itesm.mx.application.dto.RegisterParcelaDto;
import itesm.mx.application.security.AuthenticatedUserContext;
import itesm.mx.application.security.CurrentUser;
import itesm.mx.application.usecase.parcela.GetParcelaCatalogsUseCase;
import itesm.mx.application.usecase.parcela.GetParcelasByFarmerUseCase;
import itesm.mx.application.usecase.parcela.RegisterParcelaUseCase;
import itesm.mx.domain.models.user.Farmer;
import itesm.mx.domain.models.user.RoleConstants;
import itesm.mx.domain.repository.user.FarmerRepository;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;
import java.util.Optional;

import static itesm.mx.interfaces.rest.utils.ErrorResponseUtils.errorResponse;

@Path("/api/parcelas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Parcelas", description = "Gestión de parcelas agrícolas")
public class ParcelaResource {

    @Inject
    GetParcelasByFarmerUseCase getParcelasByFarmerUseCase;

    @Inject
    RegisterParcelaUseCase registerParcelaUseCase;

    @Inject
    GetParcelaCatalogsUseCase getParcelaCatalogsUseCase;

    @Inject
    FarmerRepository farmerRepository;

    @Inject
    AuthenticatedUserContext authenticatedUserContext;

    @GET
    @Path("/farmer/{userId}")
    @Operation(
            summary = "Get parcelas by user",
            description = "Returns all parcelas for the farmer associated with the given user id. Requires authentication.")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Parcelas returned",
                    content = @Content(schema = @Schema(implementation = ParcelaResponseDto[].class))),
            @APIResponse(responseCode = "401", description = "Authentication required"),
            @APIResponse(responseCode = "404", description = "Farmer not found for user"),
            @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response getParcelasByUser(
            @Parameter(description = "User id (id_usuario)") @PathParam("userId") Long userId) {

        if (authenticatedUserContext.getCurrentUser() == null) {
            return errorResponse(Response.Status.UNAUTHORIZED, "Se requiere autenticación");
        }

        try {
            Optional<Farmer> farmerOpt = farmerRepository.findByIdUser(userId);
            if (farmerOpt.isEmpty()) {
                return errorResponse(Response.Status.NOT_FOUND, "Agricultor no encontrado para el usuario indicado");
            }
            Long farmerId = farmerOpt.get().getFarmerId();
            List<ParcelaResponseDto> parcelas = getParcelasByFarmerUseCase.execute(farmerId);
            return Response.ok(parcelas).build();
        } catch (IllegalArgumentException e) {
            return errorResponse(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (RuntimeException e) {
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }

    @POST
    @Operation(
            summary = "Register parcela",
            description = "Registers a new parcela for the authenticated farmer. The location is resolved/created "
                    + "from the embedded ubicacion. Only users with the farmer role may register parcelas.")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = RegisterParcelaDto.class)))
    @APIResponses({
            @APIResponse(responseCode = "201", description = "Parcela created",
                    content = @Content(schema = @Schema(implementation = ParcelaResponseDto.class))),
            @APIResponse(responseCode = "400", description = "Invalid or missing request body"),
            @APIResponse(responseCode = "401", description = "Authentication required"),
            @APIResponse(responseCode = "403", description = "Only farmers may register parcelas"),
            @APIResponse(responseCode = "404", description = "Farmer not found for the authenticated user"),
            @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response registerParcela(@Valid RegisterParcelaDto registerParcelaDto) {
        CurrentUser currentUser = authenticatedUserContext.getCurrentUser();
        if (currentUser == null) {
            return errorResponse(Response.Status.UNAUTHORIZED, "Se requiere autenticación");
        }
        if (!RoleConstants.FARMER.equals(currentUser.getRoleId())) {
            return errorResponse(Response.Status.FORBIDDEN, "Solo los agricultores pueden registrar parcelas");
        }
        if (registerParcelaDto == null) {
            return errorResponse(Response.Status.BAD_REQUEST, "El cuerpo de la solicitud es requerido");
        }

        try {
            Optional<Farmer> farmerOpt = farmerRepository.findByIdUser(currentUser.getUserId());
            if (farmerOpt.isEmpty()) {
                return errorResponse(Response.Status.NOT_FOUND, "Agricultor no encontrado para el usuario autenticado");
            }
            Long farmerId = farmerOpt.get().getFarmerId();

            ParcelaResponseDto response = registerParcelaUseCase.execute(farmerId, registerParcelaDto);
            return Response.status(Response.Status.CREATED).entity(response).build();
        } catch (IllegalArgumentException e) {
            return errorResponse(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (IllegalStateException e) {
            return errorResponse(Response.Status.CONFLICT, e.getMessage());
        } catch (RuntimeException e) {
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }

    @GET
    @Path("/catalogos/estados")
    @Operation(summary = "List parcela states",
            description = "Returns all parcela states (id + nombre) for the register-farm form. Requires authentication.")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Catalog returned",
                    content = @Content(schema = @Schema(implementation = ParcelaCatalogItemDto[].class))),
            @APIResponse(responseCode = "401", description = "Authentication required"),
            @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response getEstadosParcela() {
        if (authenticatedUserContext.getCurrentUser() == null) {
            return errorResponse(Response.Status.UNAUTHORIZED, "Se requiere autenticación");
        }
        try {
            return Response.ok(getParcelaCatalogsUseCase.getEstadosParcela()).build();
        } catch (RuntimeException e) {
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }

    @GET
    @Path("/catalogos/tipos-cultivo")
    @Operation(summary = "List crop types",
            description = "Returns all crop types (id + nombre) for the register-farm form. Requires authentication.")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Catalog returned",
                    content = @Content(schema = @Schema(implementation = ParcelaCatalogItemDto[].class))),
            @APIResponse(responseCode = "401", description = "Authentication required"),
            @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response getTiposCultivo() {
        if (authenticatedUserContext.getCurrentUser() == null) {
            return errorResponse(Response.Status.UNAUTHORIZED, "Se requiere autenticación");
        }
        try {
            return Response.ok(getParcelaCatalogsUseCase.getTiposCultivo()).build();
        } catch (RuntimeException e) {
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }

    @GET
    @Path("/catalogos/sistemas-riego")
    @Operation(summary = "List irrigation systems",
            description = "Returns all irrigation systems (id + nombre) for the register-farm form. Requires authentication.")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Catalog returned",
                    content = @Content(schema = @Schema(implementation = ParcelaCatalogItemDto[].class))),
            @APIResponse(responseCode = "401", description = "Authentication required"),
            @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response getSistemasRiego() {
        if (authenticatedUserContext.getCurrentUser() == null) {
            return errorResponse(Response.Status.UNAUTHORIZED, "Se requiere autenticación");
        }
        try {
            return Response.ok(getParcelaCatalogsUseCase.getSistemasRiego()).build();
        } catch (RuntimeException e) {
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }
}
