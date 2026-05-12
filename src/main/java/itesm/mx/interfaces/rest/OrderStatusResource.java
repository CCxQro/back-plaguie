package itesm.mx.interfaces.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import itesm.mx.application.dto.OrderStatusResponseDto;
import itesm.mx.application.security.AuthenticatedUserContext;
import itesm.mx.application.usecase.order.GetAllOrderStatusesUseCase;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import static itesm.mx.interfaces.rest.utils.ErrorResponseUtils.errorResponse;

@Path("/api/order-statuses")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Order Statuses", description = "Order status catalog endpoints")
public class OrderStatusResource {

    @Inject
    GetAllOrderStatusesUseCase getAllOrderStatusesUseCase;

    @Inject
    AuthenticatedUserContext authenticatedUserContext;

    @GET
    @Operation(summary = "List order statuses", description = "Returns all available order statuses. Requires authentication.")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "List returned",
                    content = @Content(schema = @Schema(implementation = OrderStatusResponseDto[].class))),
            @APIResponse(responseCode = "401", description = "Authentication required"),
            @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response getAllOrderStatuses() {
        if (authenticatedUserContext.getCurrentUser() == null) {
            return errorResponse(Response.Status.UNAUTHORIZED, "Se requiere autenticación");
        }
        try {
            return Response.ok(getAllOrderStatusesUseCase.execute()).build();
        } catch (RuntimeException e) {
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }
}
