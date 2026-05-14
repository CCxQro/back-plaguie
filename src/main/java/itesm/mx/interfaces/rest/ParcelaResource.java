package itesm.mx.interfaces.rest;

import itesm.mx.application.dto.ParcelaResponseDto;
import itesm.mx.application.security.AuthenticatedUserContext;
import itesm.mx.application.usecase.parcela.GetParcelasByFarmerUseCase;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;

import static itesm.mx.interfaces.rest.utils.ErrorResponseUtils.errorResponse;

@Path("/api/parcelas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Parcelas", description = "Gestión de parcelas agrícolas")
public class ParcelaResource {

    @Inject
    GetParcelasByFarmerUseCase getParcelasByFarmerUseCase;

    @Inject
    AuthenticatedUserContext authenticatedUserContext;

    @GET
    @Path("/farmer/{farmerId}")
    @Operation(summary = "Get parcelas by farmer", description = "Returns all parcelas for a given farmer. Requires authentication.")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Parcelas returned",
                    content = @Content(schema = @Schema(implementation = ParcelaResponseDto[].class))),
            @APIResponse(responseCode = "401", description = "Authentication required"),
            @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response getParcelasByFarmer(
            @Parameter(description = "Farmer id") @PathParam("farmerId") Long farmerId) {
        if (authenticatedUserContext.getCurrentUser() == null) {
            return errorResponse(Response.Status.UNAUTHORIZED, "Se requiere autenticación");
        }
        try {
            List<ParcelaResponseDto> parcelas = getParcelasByFarmerUseCase.execute(farmerId);
            return Response.ok(parcelas).build();
        } catch (IllegalArgumentException e) {
            return errorResponse(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (RuntimeException e) {
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }
}
