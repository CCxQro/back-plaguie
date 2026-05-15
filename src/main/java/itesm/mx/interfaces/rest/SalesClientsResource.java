package itesm.mx.interfaces.rest;

import itesm.mx.application.dto.ClientDetailDto;
import itesm.mx.application.dto.ClientMapDto;
import itesm.mx.application.security.AuthenticatedUserContext;
import itesm.mx.application.usecase.sales.GetClientDetailBySellerUseCase;
import itesm.mx.application.usecase.sales.GetClientsMapBySellerUseCase;
import itesm.mx.domain.models.user.RoleConstants;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import static itesm.mx.interfaces.rest.utils.ErrorResponseUtils.errorResponse;

@Path("/api/sales/clients")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Sales Clients", description = "Endpoints for technical sellers to consume their clients (farmers) data for the map view")
public class SalesClientsResource {

    @Inject GetClientsMapBySellerUseCase getClientsMapBySellerUseCase;
    @Inject GetClientDetailBySellerUseCase getClientDetailBySellerUseCase;
    @Inject AuthenticatedUserContext authenticatedUserContext;

    @GET
    @Operation(
            summary = "List clients for the current seller (map view)",
            description = "Returns the list of farmers (clients) of the authenticated technical seller, "
                    + "enriched with location, parcela and alert data for the map view. "
                    + "Supports optional query filters."
    )
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Clients returned",
                    content = @Content(schema = @Schema(implementation = ClientMapDto[].class))),
            @APIResponse(responseCode = "400", description = "Invalid request"),
            @APIResponse(responseCode = "401", description = "Authentication required"),
            @APIResponse(responseCode = "403", description = "Seller role required"),
            @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response getClients(
            @Parameter(description = "Filter by crop name") @QueryParam("cultivo") String cultivo,
            @Parameter(description = "Filter by parcela state name") @QueryParam("estadoParcela") String estadoParcela,
            @Parameter(description = "Filter by location state name") @QueryParam("state") String state,
            @Parameter(description = "Filter by location municipality name") @QueryParam("municipality") String municipality,
            @Parameter(description = "Only return clients with active alerts") @QueryParam("onlyWithActiveAlerts") Boolean onlyWithActiveAlerts
    ) {
        if (authenticatedUserContext.getCurrentUser() == null) {
            return errorResponse(Response.Status.UNAUTHORIZED, "Se requiere autenticación");
        }
        if (!RoleConstants.SELLER.equals(authenticatedUserContext.getCurrentUser().getRoleId())) {
            return errorResponse(Response.Status.FORBIDDEN,
                    "Solo un técnico vendedor puede consultar sus clientes");
        }
        try {
            GetClientsMapBySellerUseCase.Filters filters = new GetClientsMapBySellerUseCase.Filters();
            filters.cultivo = cultivo;
            filters.estadoParcela = estadoParcela;
            filters.state = state;
            filters.municipality = municipality;
            filters.onlyWithActiveAlerts = onlyWithActiveAlerts;
            return Response.ok(getClientsMapBySellerUseCase.execute(
                    authenticatedUserContext.getCurrentUser().getUserId(), filters)).build();
        } catch (IllegalArgumentException e) {
            return errorResponse(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (RuntimeException e) {
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }

    @GET
    @Path("/{farmerId}")
    @Operation(
            summary = "Get detailed information for a client of the current seller",
            description = "Returns the full client (farmer) detail including parcelas, alerts and order summary. "
                    + "The farmer must be a client of the authenticated seller, otherwise the endpoint returns 404."
    )
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Client detail returned",
                    content = @Content(schema = @Schema(implementation = ClientDetailDto.class))),
            @APIResponse(responseCode = "400", description = "Invalid farmer id"),
            @APIResponse(responseCode = "401", description = "Authentication required"),
            @APIResponse(responseCode = "403", description = "Seller role required"),
            @APIResponse(responseCode = "404", description = "Farmer is not a client of the current seller"),
            @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response getClientDetail(
            @Parameter(description = "Farmer id") @PathParam("farmerId") Long farmerId) {
        if (authenticatedUserContext.getCurrentUser() == null) {
            return errorResponse(Response.Status.UNAUTHORIZED, "Se requiere autenticación");
        }
        if (!RoleConstants.SELLER.equals(authenticatedUserContext.getCurrentUser().getRoleId())) {
            return errorResponse(Response.Status.FORBIDDEN,
                    "Solo un técnico vendedor puede consultar los detalles de sus clientes");
        }
        try {
            return Response.ok(getClientDetailBySellerUseCase.execute(
                    authenticatedUserContext.getCurrentUser().getUserId(), farmerId)).build();
        } catch (IllegalArgumentException e) {
            return errorResponse(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (IllegalStateException e) {
            return errorResponse(Response.Status.NOT_FOUND, e.getMessage());
        } catch (RuntimeException e) {
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }
}
