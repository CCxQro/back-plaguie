package itesm.mx.interfaces.rest;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import itesm.mx.application.dto.FarmerLocationDto;
import itesm.mx.application.dto.OrderResponseDto;
import itesm.mx.application.dto.RegisterOrderDto;
import itesm.mx.application.security.AuthenticatedUserContext;
import itesm.mx.application.usecase.order.CreateOrderUseCase;
import itesm.mx.application.usecase.order.GetFarmerLocationsBySellerUseCase;
import itesm.mx.application.usecase.order.GetOrderByIdUseCase;
import itesm.mx.application.usecase.order.GetOrdersBySellerUseCase;
import itesm.mx.application.usecase.order.UpdateOrderStatusUseCase;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import static itesm.mx.interfaces.rest.utils.ErrorResponseUtils.errorResponse;

@Path("/api/orders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Orders", description = "Order management endpoints")
public class OrderResource {

    private static final Integer ADMIN_ROLE_ID = 1;
    private static final Integer SELLER_ROLE_ID = 3;

    @Inject CreateOrderUseCase createOrderUseCase;
    @Inject GetOrderByIdUseCase getOrderByIdUseCase;
    @Inject GetOrdersBySellerUseCase getOrdersBySellerUseCase;
    @Inject GetFarmerLocationsBySellerUseCase getFarmerLocationsBySellerUseCase;
    @Inject UpdateOrderStatusUseCase updateOrderStatusUseCase;
    @Inject AuthenticatedUserContext authenticatedUserContext;

    @POST
    @Operation(summary = "Create order", description = "Creates a new order with line items. Seller-only endpoint.")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = RegisterOrderDto.class)))
    @APIResponses({
            @APIResponse(responseCode = "201", description = "Order created",
                    content = @Content(schema = @Schema(implementation = OrderResponseDto.class))),
            @APIResponse(responseCode = "400", description = "Invalid or missing request body"),
            @APIResponse(responseCode = "401", description = "Authentication required"),
            @APIResponse(responseCode = "403", description = "Seller role required"),
            @APIResponse(responseCode = "404", description = "Farmer, seller or product not found"),
            @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response createOrder(@Valid RegisterOrderDto dto) {
        if (authenticatedUserContext.getCurrentUser() == null) {
            return errorResponse(Response.Status.UNAUTHORIZED, "Se requiere autenticación");
        }
        if (!SELLER_ROLE_ID.equals(authenticatedUserContext.getCurrentUser().getRoleId())) {
            return errorResponse(Response.Status.FORBIDDEN, "Solo un técnico vendedor puede crear pedidos");
        }
        if (dto == null) {
            return errorResponse(Response.Status.BAD_REQUEST, "El cuerpo de la solicitud es requerido");
        }
        try {
            OrderResponseDto result = createOrderUseCase.execute(dto);
            return Response.status(Response.Status.CREATED).entity(result).build();
        } catch (IllegalArgumentException e) {
            return errorResponse(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (IllegalStateException e) {
            return errorResponse(Response.Status.NOT_FOUND, e.getMessage());
        } catch (RuntimeException e) {
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }

    @GET
    @Path("/{orderId}")
    @Operation(summary = "Get order by id", description = "Returns an order with its details. Requires authentication.")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Order returned",
                    content = @Content(schema = @Schema(implementation = OrderResponseDto.class))),
            @APIResponse(responseCode = "400", description = "Invalid order id"),
            @APIResponse(responseCode = "401", description = "Authentication required"),
            @APIResponse(responseCode = "404", description = "Order not found"),
            @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response getOrderById(
            @Parameter(description = "Order id") @PathParam("orderId") Long orderId) {
        if (authenticatedUserContext.getCurrentUser() == null) {
            return errorResponse(Response.Status.UNAUTHORIZED, "Se requiere autenticación");
        }
        try {
            return Response.ok(getOrderByIdUseCase.execute(orderId)).build();
        } catch (IllegalArgumentException e) {
            return errorResponse(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (IllegalStateException e) {
            return errorResponse(Response.Status.NOT_FOUND, e.getMessage());
        } catch (RuntimeException e) {
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }

    @GET
    @Operation(summary = "List orders for current seller", description = "Returns all orders belonging to the logged-in seller. Seller-only endpoint.")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Orders returned",
                    content = @Content(schema = @Schema(implementation = OrderResponseDto[].class))),
            @APIResponse(responseCode = "401", description = "Authentication required"),
            @APIResponse(responseCode = "403", description = "Seller role required"),
            @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response getOrders() {
        if (authenticatedUserContext.getCurrentUser() == null) {
            return errorResponse(Response.Status.UNAUTHORIZED, "Se requiere autenticación");
        }
        if (!SELLER_ROLE_ID.equals(authenticatedUserContext.getCurrentUser().getRoleId())) {
            return errorResponse(Response.Status.FORBIDDEN, "Solo un técnico vendedor puede listar sus pedidos");
        }
        try {
            return Response.ok(getOrdersBySellerUseCase.execute(
                    authenticatedUserContext.getCurrentUser().getUserId())).build();
        } catch (IllegalArgumentException e) {
            return errorResponse(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (RuntimeException e) {
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }

    @GET
    @Path("/farmer-locations")
    @Operation(summary = "Get farmer locations for current seller",
            description = "Returns GPS coordinates of all farmers who have placed orders with the logged-in seller. Seller-only endpoint.")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Locations returned",
                    content = @Content(schema = @Schema(implementation = FarmerLocationDto[].class))),
            @APIResponse(responseCode = "401", description = "Authentication required"),
            @APIResponse(responseCode = "403", description = "Seller role required"),
            @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response getFarmerLocations() {
        if (authenticatedUserContext.getCurrentUser() == null) {
            return errorResponse(Response.Status.UNAUTHORIZED, "Se requiere autenticación");
        }
        if (!SELLER_ROLE_ID.equals(authenticatedUserContext.getCurrentUser().getRoleId())) {
            return errorResponse(Response.Status.FORBIDDEN,
                    "Solo un técnico vendedor puede consultar las ubicaciones de sus clientes");
        }
        try {
            return Response.ok(getFarmerLocationsBySellerUseCase.execute(
                    authenticatedUserContext.getCurrentUser().getUserId())).build();
        } catch (IllegalArgumentException e) {
            return errorResponse(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (RuntimeException e) {
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }

    @PATCH
    @Path("/{orderId}/status")
    @Operation(summary = "Update order status", description = "Updates the status of an order. Seller or Admin endpoint.")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = UpdateOrderStatusDto.class)))
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Order status updated",
                    content = @Content(schema = @Schema(implementation = OrderResponseDto.class))),
            @APIResponse(responseCode = "400", description = "Invalid order id or status id"),
            @APIResponse(responseCode = "401", description = "Authentication required"),
            @APIResponse(responseCode = "403", description = "Seller or Admin role required"),
            @APIResponse(responseCode = "404", description = "Order or status not found"),
            @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response updateOrderStatus(
            @Parameter(description = "Order id") @PathParam("orderId") Long orderId,
            @Valid UpdateOrderStatusDto body) {
        if (authenticatedUserContext.getCurrentUser() == null) {
            return errorResponse(Response.Status.UNAUTHORIZED, "Se requiere autenticación");
        }
        Integer role = authenticatedUserContext.getCurrentUser().getRoleId();
        if (!SELLER_ROLE_ID.equals(role) && !ADMIN_ROLE_ID.equals(role)) {
            return errorResponse(Response.Status.FORBIDDEN,
                    "Solo un técnico vendedor o administrador puede actualizar el estado del pedido");
        }
        if (body == null || body.orderStatusId == null) {
            return errorResponse(Response.Status.BAD_REQUEST, "El id del estado es requerido");
        }
        try {
            return Response.ok(updateOrderStatusUseCase.execute(orderId, body.orderStatusId)).build();
        } catch (IllegalArgumentException e) {
            return errorResponse(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (IllegalStateException e) {
            return errorResponse(Response.Status.NOT_FOUND, e.getMessage());
        } catch (RuntimeException e) {
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }

    public static class UpdateOrderStatusDto {
        @jakarta.validation.constraints.NotNull(message = "El id del estado es requerido")
        @jakarta.validation.constraints.Positive(message = "El id del estado debe ser positivo")
        public Long orderStatusId;
    }
}
