package itesm.mx.interfaces.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import itesm.mx.application.dto.ProductResponseDto;
import itesm.mx.application.dto.RegisterProductDto;
import itesm.mx.application.dto.UpdateProductDto;
import itesm.mx.application.security.AuthenticatedUserContext;
import itesm.mx.application.usecase.marketplace.product.*;
import itesm.mx.domain.models.marketplace.Category;
import itesm.mx.domain.models.marketplace.Product;
import itesm.mx.domain.models.marketplace.Provider;
import itesm.mx.domain.models.marketplace.Status;
import itesm.mx.domain.models.marketplace.Unit;
import itesm.mx.domain.models.user.RoleConstants;
import itesm.mx.domain.models.user.TechnicalSeller;

import java.util.List;
import java.util.Map;

import static itesm.mx.interfaces.rest.utils.ErrorResponseUtils.errorResponse;

@Path("/api/products")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProductResource {

    @Inject RegisterProductUseCase registerProductUseCase;
    @Inject UpdateProductUseCase updateProductUseCase;
    @Inject DeleteProductUseCase deleteProductUseCase;
    @Inject GetAllProductsUseCase getAllProductsUseCase;
    @Inject GetProductByIdUseCase getProductByIdUseCase;
    @Inject GetProductsBySellerUseCase getProductsBySellerUseCase;
    @Inject GetProductsByProviderUseCase getProductsByProviderUseCase;
    @Inject GetProductsByStatusUseCase getProductsByStatusUseCase;
    @Inject CountAllProductsUseCase countAllProductsUseCase;
    @Inject CountNormalStockProductsUseCase countNormalStockProductsUseCase;
    @Inject CountLowStockProductsUseCase countLowStockProductsUseCase;
    @Inject CountCriticStockProductsUseCase countCriticStockProductsUseCase;
    @Inject AuthenticatedUserContext authenticatedUserContext;

    @GET
    public Response getProducts(
            @QueryParam("sellerId") Long sellerId,
            @QueryParam("providerId") Long providerId,
            @QueryParam("statusId") Long statusId) {

        if (authenticatedUserContext.getCurrentUser() == null) {
            return errorResponse(Response.Status.UNAUTHORIZED, "Se requiere autenticacion");
        }

        try {
            List<ProductResponseDto> products;
            if (sellerId != null) {
                products = getProductsBySellerUseCase.execute(sellerId)
                        .stream().map(this::toResponseDto).toList();
            } else if (providerId != null) {
                products = getProductsByProviderUseCase.execute(providerId)
                        .stream().map(this::toResponseDto).toList();
            } else if (statusId != null) {
                products = getProductsByStatusUseCase.execute(statusId)
                        .stream().map(this::toResponseDto).toList();
            } else {
                products = getAllProductsUseCase.execute()
                        .stream().map(this::toResponseDto).toList();
            }
            return Response.ok(products).build();
        } catch (IllegalArgumentException e) {
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("not found")) {
                return errorResponse(Response.Status.NOT_FOUND, e.getMessage());
            }
            return errorResponse(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (RuntimeException e) {
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }

    @GET
    @Path("/count")
    public Response countProducts() {
        Response auth = requireAdminOrSeller();
        if (auth != null) return auth;
        try {
            long count = countAllProductsUseCase.execute();
            return Response.ok(Map.of("count", count)).build();
        } catch (RuntimeException e) {
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }

    @GET
    @Path("/normalstock")
    public Response countNormalStock() {
        Response auth = requireAdminOrSeller();
        if (auth != null) return auth;
        try {
            long count = countNormalStockProductsUseCase.execute();
            return Response.ok(Map.of("count", count)).build();
        } catch (RuntimeException e) {
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }

    @GET
    @Path("/lowstock")
    public Response countLowStock() {
        Response auth = requireAdminOrSeller();
        if (auth != null) return auth;
        try {
            long count = countLowStockProductsUseCase.execute();
            return Response.ok(Map.of("count", count)).build();
        } catch (RuntimeException e) {
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }

    @GET
    @Path("/criticstock")
    public Response countCriticStock() {
        Response auth = requireAdminOrSeller();
        if (auth != null) return auth;
        try {
            long count = countCriticStockProductsUseCase.execute();
            return Response.ok(Map.of("count", count)).build();
        } catch (RuntimeException e) {
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }

    private Response requireAdminOrSeller() {
        if (authenticatedUserContext.getCurrentUser() == null) {
            return errorResponse(Response.Status.UNAUTHORIZED, "Se requiere autenticacion");
        }
        Integer role = authenticatedUserContext.getCurrentUser().getRoleId();
        if (!RoleConstants.ADMIN.equals(role) && !RoleConstants.SELLER.equals(role)) {
            return errorResponse(Response.Status.FORBIDDEN, "Solo administradores y tecnicos vendedores pueden consultar estos conteos");
        }
        return null;
    }

    @POST
    public Response registerProduct(RegisterProductDto dto) {
        if (dto == null) {
            return errorResponse(Response.Status.BAD_REQUEST, "El cuerpo de la solicitud es requerido");
        }
        if (authenticatedUserContext.getCurrentUser() == null) {
            return errorResponse(Response.Status.UNAUTHORIZED, "Se requiere autenticacion");
        }
        if (!RoleConstants.SELLER.equals(authenticatedUserContext.getCurrentUser().getRoleId())) {
            return errorResponse(Response.Status.FORBIDDEN, "Solo un tecnico vendedor puede registrar productos");
        }

        try {
            long skuSellerId = Math.abs((long) (dto.sellerId + "_" + dto.sku).hashCode());

            Product product = buildProductFromRegisterDto(dto, skuSellerId);
            Product created = registerProductUseCase.execute(product);
            return Response.status(Response.Status.CREATED).entity(toResponseDto(created)).build();
        } catch (IllegalArgumentException e) {
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("not found")) {
                return errorResponse(Response.Status.NOT_FOUND, e.getMessage());
            }
            return errorResponse(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (RuntimeException e) {
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }

    @PUT
    @Path("/{skuSellerId}")
    public Response updateProduct(@PathParam("skuSellerId") Long skuSellerId, UpdateProductDto dto) {
        if (dto == null) {
            return errorResponse(Response.Status.BAD_REQUEST, "El cuerpo de la solicitud es requerido");
        }
        if (authenticatedUserContext.getCurrentUser() == null) {
            return errorResponse(Response.Status.UNAUTHORIZED, "Se requiere autenticacion");
        }

        Product existing;
        try {
            existing = getProductByIdUseCase.execute(skuSellerId);
        } catch (IllegalArgumentException e) {
            return errorResponse(Response.Status.NOT_FOUND, "Producto no encontrado");
        }

        Integer role = authenticatedUserContext.getCurrentUser().getRoleId();
        if (!RoleConstants.ADMIN.equals(role)) {
            Long ownerUserId = existing.getSeller().getUser().getUserId();
            Long currentUserId = authenticatedUserContext.getCurrentUser().getUserId();
            if (!ownerUserId.equals(currentUserId)) {
                return errorResponse(Response.Status.FORBIDDEN, "No tienes permiso para actualizar este producto");
            }
        }

        try {
            Product product = buildProductFromUpdateDto(dto);
            Product updated = updateProductUseCase.execute(skuSellerId, product);
            return Response.ok(toResponseDto(updated)).build();
        } catch (IllegalArgumentException e) {
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("not found")) {
                return errorResponse(Response.Status.NOT_FOUND, e.getMessage());
            }
            return errorResponse(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (RuntimeException e) {
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }

    @DELETE
    @Path("/{skuSellerId}")
    public Response deleteProduct(@PathParam("skuSellerId") Long skuSellerId) {
        if (authenticatedUserContext.getCurrentUser() == null) {
            return errorResponse(Response.Status.UNAUTHORIZED, "Se requiere autenticacion");
        }

        Product existing;
        try {
            existing = getProductByIdUseCase.execute(skuSellerId);
        } catch (IllegalArgumentException e) {
            return errorResponse(Response.Status.NOT_FOUND, "Producto no encontrado");
        }

        Integer role = authenticatedUserContext.getCurrentUser().getRoleId();
        if (!RoleConstants.ADMIN.equals(role)) {
            Long ownerUserId = existing.getSeller().getUser().getUserId();
            Long currentUserId = authenticatedUserContext.getCurrentUser().getUserId();
            if (!ownerUserId.equals(currentUserId)) {
                return errorResponse(Response.Status.FORBIDDEN, "No tienes permiso para eliminar este producto");
            }
        }

        try {
            deleteProductUseCase.execute(skuSellerId);
            return Response.noContent().build();
        } catch (IllegalArgumentException e) {
            return errorResponse(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (RuntimeException e) {
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }

    private Product buildProductFromRegisterDto(RegisterProductDto dto, long skuSellerId) {
        TechnicalSeller seller = new TechnicalSeller();
        seller.setTechnicalSellerId(dto.sellerId);

        Category category = new Category();
        category.setCategoryId(dto.categoryId);

        Provider provider = new Provider();
        provider.setProviderId(dto.providerId);

        Unit unit = new Unit();
        unit.setUnitId(dto.unitId);

        Status status = new Status();
        status.setStatusId(dto.statusId);

        Product product = new Product(skuSellerId, seller, dto.name, dto.sku,
                category, provider, dto.unitValue, unit, dto.description, status, dto.firebaseImageId);
        product.setLatestPrice(dto.price);
        product.setStock(dto.stock);
        return product;
    }

    private Product buildProductFromUpdateDto(UpdateProductDto dto) {
        Category category = new Category();
        category.setCategoryId(dto.categoryId);

        Provider provider = new Provider();
        provider.setProviderId(dto.providerId);

        Unit unit = new Unit();
        unit.setUnitId(dto.unitId);

        Status status = new Status();
        status.setStatusId(dto.statusId);

        Product product = new Product(null, null, dto.name, dto.sku,
                category, provider, dto.unitValue, unit, dto.description, status, dto.firebaseImageId);
        product.setLatestPrice(dto.price);
        product.setStock(dto.stock);
        return product;
    }

    private ProductResponseDto toResponseDto(Product product) {
        ProductResponseDto dto = new ProductResponseDto();
        dto.skuSellerId = product.getSkuSellerId();

        if (product.getSeller() != null) {
            dto.sellerId = product.getSeller().getTechnicalSellerId();
            if (product.getSeller().getUser() != null) {
                dto.sellerName = product.getSeller().getUser().getName();
            }
        }

        dto.name = product.getName();
        dto.sku = product.getSku();

        if (product.getCategory() != null) {
            dto.categoryId = product.getCategory().getCategoryId();
            dto.categoryName = product.getCategory().getName();
        }

        if (product.getProvider() != null) {
            dto.providerId = product.getProvider().getProviderId();
            dto.providerName = product.getProvider().getName();
        }

        dto.unitValue = product.getUnitValue();

        if (product.getUnit() != null) {
            dto.unitId = product.getUnit().getUnitId();
            dto.unitName = product.getUnit().getName();
        }

        dto.description = product.getDescription();

        if (product.getStatus() != null) {
            dto.statusId = product.getStatus().getStatusId();
            dto.statusName = product.getStatus().getName();
        }

        dto.firebaseImageId = product.getFirebaseImageId();
        dto.latestPrice = product.getLatestPrice();
        dto.latestPriceDate = product.getLatestPriceDate();
        dto.stock = product.getStock();

        return dto;
    }
}
