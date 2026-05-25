package itesm.mx.interfaces.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import itesm.mx.application.dto.InventoryResponseDto;
import itesm.mx.application.dto.InventorySummaryResponseDto;
import itesm.mx.application.dto.RegisterInventoryDto;
import itesm.mx.application.dto.UpdateInventoryDto;
import itesm.mx.application.mapper.marketplace.InventoryDtoMapper;
import itesm.mx.application.security.AuthenticatedUserContext;
import itesm.mx.application.usecase.marketplace.inventory.DeleteInventoryUseCase;
import itesm.mx.application.usecase.marketplace.inventory.GetCurrentStockUseCase;
import itesm.mx.application.usecase.marketplace.inventory.GetInventoryHistoryUseCase;
import itesm.mx.application.usecase.marketplace.inventory.RegisterInventoryUseCase;
import itesm.mx.application.usecase.marketplace.inventory.UpdateInventoryUseCase;
import itesm.mx.application.usecase.marketplace.product.GetProductByIdUseCase;
import itesm.mx.domain.models.marketplace.Inventory;
import itesm.mx.domain.models.marketplace.InventoryAction;
import itesm.mx.domain.models.marketplace.InventoryActionConstants;
import itesm.mx.domain.models.marketplace.Product;
import itesm.mx.domain.models.user.RoleConstants;
import itesm.mx.domain.repository.marketplace.InventoryRepository;

import java.util.List;

import static itesm.mx.interfaces.rest.utils.ErrorResponseUtils.errorResponse;

@Path("/api/inventory")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class InventoryResource {

    @Inject RegisterInventoryUseCase registerInventoryUseCase;
    @Inject UpdateInventoryUseCase updateInventoryUseCase;
    @Inject DeleteInventoryUseCase deleteInventoryUseCase;
    @Inject GetInventoryHistoryUseCase getInventoryHistoryUseCase;
    @Inject GetCurrentStockUseCase getCurrentStockUseCase;
    @Inject GetProductByIdUseCase getProductByIdUseCase;
    @Inject InventoryRepository inventoryRepository;
    @Inject AuthenticatedUserContext authenticatedUserContext;

    // -------------------- Product-scoped --------------------

    @POST
    @Path("/product/{skuSellerId}/add")
    public Response addStock(@PathParam("skuSellerId") Long skuSellerId, RegisterInventoryDto dto) {
        return registerMovement(skuSellerId, dto, InventoryActionConstants.ADD, Response.Status.CREATED);
    }

    @POST
    @Path("/product/{skuSellerId}/remove")
    public Response removeStock(@PathParam("skuSellerId") Long skuSellerId, RegisterInventoryDto dto) {
        return registerMovement(skuSellerId, dto, InventoryActionConstants.SUBTRACT, Response.Status.CREATED);
    }

    @GET
    @Path("/product/{skuSellerId}/history")
    public Response getHistory(@PathParam("skuSellerId") Long skuSellerId) {
        if (authenticatedUserContext.getCurrentUser() == null) {
            return errorResponse(Response.Status.UNAUTHORIZED, "Se requiere autenticacion");
        }
        Response authError = checkProductOwnership(skuSellerId, "ver el historial de inventario");
        if (authError != null) return authError;

        try {
            List<InventoryResponseDto> history = getInventoryHistoryUseCase.execute(skuSellerId)
                    .stream()
                    .map(InventoryDtoMapper::toResponseDto)
                    .toList();
            return Response.ok(history).build();
        } catch (IllegalArgumentException e) {
            return errorResponse(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (RuntimeException e) {
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }

    @GET
    @Path("/product/{skuSellerId}/summary")
    public Response getSummary(@PathParam("skuSellerId") Long skuSellerId) {
        if (authenticatedUserContext.getCurrentUser() == null) {
            return errorResponse(Response.Status.UNAUTHORIZED, "Se requiere autenticacion");
        }
        Response authError = checkProductOwnership(skuSellerId, "ver el inventario");
        if (authError != null) return authError;

        try {
            int stock = getCurrentStockUseCase.execute(skuSellerId);
            InventorySummaryResponseDto dto = new InventorySummaryResponseDto();
            dto.skuSellerId = skuSellerId;
            dto.stock = stock;
            return Response.ok(dto).build();
        } catch (IllegalArgumentException e) {
            return errorResponse(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (RuntimeException e) {
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }

    // -------------------- Row-scoped --------------------

    @PUT
    @Path("/{inventoryId}")
    public Response updateRow(@PathParam("inventoryId") Long inventoryId, UpdateInventoryDto dto) {
        if (dto == null) {
            return errorResponse(Response.Status.BAD_REQUEST, "El cuerpo de la solicitud es requerido");
        }
        if (authenticatedUserContext.getCurrentUser() == null) {
            return errorResponse(Response.Status.UNAUTHORIZED, "Se requiere autenticacion");
        }

        Inventory existing = inventoryRepository.findByInventoryId(inventoryId).orElse(null);
        if (existing == null) {
            return errorResponse(Response.Status.NOT_FOUND, "Registro de inventario no encontrado");
        }
        Response authError = checkProductOwnership(existing.getProduct().getSkuSellerId(), "modificar este registro de inventario");
        if (authError != null) return authError;

        try {
            Inventory updated = updateInventoryUseCase.execute(inventoryId, dto.cantidad);
            return Response.ok(InventoryDtoMapper.toResponseDto(updated)).build();
        } catch (IllegalArgumentException e) {
            return errorResponse(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (RuntimeException e) {
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }

    @DELETE
    @Path("/{inventoryId}")
    public Response deleteRow(@PathParam("inventoryId") Long inventoryId) {
        if (authenticatedUserContext.getCurrentUser() == null) {
            return errorResponse(Response.Status.UNAUTHORIZED, "Se requiere autenticacion");
        }

        Inventory existing = inventoryRepository.findByInventoryId(inventoryId).orElse(null);
        if (existing == null) {
            return errorResponse(Response.Status.NOT_FOUND, "Registro de inventario no encontrado");
        }
        Response authError = checkProductOwnership(existing.getProduct().getSkuSellerId(), "eliminar este registro de inventario");
        if (authError != null) return authError;

        try {
            deleteInventoryUseCase.execute(inventoryId);
            return Response.noContent().build();
        } catch (IllegalArgumentException e) {
            return errorResponse(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (RuntimeException e) {
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }

    // -------------------- Helpers --------------------

    private Response registerMovement(Long skuSellerId, RegisterInventoryDto dto, Long actionId, Response.Status successStatus) {
        if (dto == null) {
            return errorResponse(Response.Status.BAD_REQUEST, "El cuerpo de la solicitud es requerido");
        }
        if (authenticatedUserContext.getCurrentUser() == null) {
            return errorResponse(Response.Status.UNAUTHORIZED, "Se requiere autenticacion");
        }
        Response authError = checkProductOwnership(skuSellerId, "modificar el inventario de este producto");
        if (authError != null) return authError;

        try {
            Product productRef = new Product();
            productRef.setSkuSellerId(skuSellerId);
            InventoryAction action = new InventoryAction();
            action.setInventoryActionId(actionId);
            Inventory row = new Inventory(null, productRef, dto.cantidad, action);
            Inventory saved = registerInventoryUseCase.execute(row);
            return Response.status(successStatus).entity(InventoryDtoMapper.toResponseDto(saved)).build();
        } catch (IllegalArgumentException e) {
            return errorResponse(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (RuntimeException e) {
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }

    private Response checkProductOwnership(Long skuSellerId, String forbiddenAction) {
        Product product;
        try {
            product = getProductByIdUseCase.execute(skuSellerId);
        } catch (IllegalArgumentException e) {
            return errorResponse(Response.Status.NOT_FOUND, "Producto no encontrado");
        }
        Integer role = authenticatedUserContext.getCurrentUser().getRoleId();
        if (RoleConstants.ADMIN.equals(role)) {
            return null;
        }
        Long ownerUserId = product.getSeller().getUser().getUserId();
        Long currentUserId = authenticatedUserContext.getCurrentUser().getUserId();
        if (!ownerUserId.equals(currentUserId)) {
            return errorResponse(Response.Status.FORBIDDEN, "No tienes permiso para " + forbiddenAction);
        }
        return null;
    }
}
