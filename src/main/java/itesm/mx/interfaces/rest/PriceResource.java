package itesm.mx.interfaces.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import itesm.mx.application.dto.PriceResponseDto;
import itesm.mx.application.dto.RegisterPriceDto;
import itesm.mx.application.mapper.marketplace.PriceDtoMapper;
import itesm.mx.application.security.AuthenticatedUserContext;
import itesm.mx.application.usecase.marketplace.price.GetAllPricesUseCase;
import itesm.mx.application.usecase.marketplace.price.RegisterPriceUseCase;
import itesm.mx.application.usecase.marketplace.product.GetProductByIdUseCase;
import itesm.mx.domain.models.marketplace.Price;
import itesm.mx.domain.models.marketplace.Product;
import itesm.mx.domain.models.user.RoleConstants;

import java.util.List;

import static itesm.mx.interfaces.rest.utils.ErrorResponseUtils.errorResponse;

@Path("/api/prices")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PriceResource {

    @Inject GetAllPricesUseCase getAllPricesUseCase;
    @Inject RegisterPriceUseCase registerPriceUseCase;
    @Inject GetProductByIdUseCase getProductByIdUseCase;
    @Inject AuthenticatedUserContext authenticatedUserContext;

    @GET
    @Path("/{skuSellerId}")
    public Response getPriceHistory(@PathParam("skuSellerId") Long skuSellerId) {
        if (authenticatedUserContext.getCurrentUser() == null) {
            return errorResponse(Response.Status.UNAUTHORIZED, "Se requiere autenticacion");
        }

        Product product;
        try {
            product = getProductByIdUseCase.execute(skuSellerId);
        } catch (IllegalArgumentException e) {
            return errorResponse(Response.Status.NOT_FOUND, "Producto no encontrado");
        }

        Response authError = checkProductOwnership(product, "ver el historial de precios de este producto");
        if (authError != null) return authError;

        try {
            List<PriceResponseDto> history = getAllPricesUseCase.execute(skuSellerId)
                    .stream()
                    .map(PriceDtoMapper::toResponseDto)
                    .toList();
            return Response.ok(history).build();
        } catch (IllegalArgumentException e) {
            return errorResponse(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (RuntimeException e) {
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }

    @POST
    @Path("/product/{skuSellerId}")
    public Response registerPrice(@PathParam("skuSellerId") Long skuSellerId, RegisterPriceDto dto) {
        if (dto == null) {
            return errorResponse(Response.Status.BAD_REQUEST, "El cuerpo de la solicitud es requerido");
        }
        if (authenticatedUserContext.getCurrentUser() == null) {
            return errorResponse(Response.Status.UNAUTHORIZED, "Se requiere autenticacion");
        }

        Product product;
        try {
            product = getProductByIdUseCase.execute(skuSellerId);
        } catch (IllegalArgumentException e) {
            return errorResponse(Response.Status.NOT_FOUND, "Producto no encontrado");
        }

        Response authError = checkProductOwnership(product, "registrar precios para este producto");
        if (authError != null) return authError;

        try {
            Product productRef = new Product();
            productRef.setSkuSellerId(skuSellerId);
            Price price = new Price();
            price.setProduct(productRef);
            price.setPrice(dto.price);
            Price saved = registerPriceUseCase.execute(price);
            return Response.status(Response.Status.CREATED)
                    .entity(PriceDtoMapper.toResponseDto(saved))
                    .build();
        } catch (IllegalArgumentException e) {
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("not found")) {
                return errorResponse(Response.Status.NOT_FOUND, e.getMessage());
            }
            return errorResponse(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (RuntimeException e) {
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }

    private Response checkProductOwnership(Product product, String forbiddenAction) {
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
