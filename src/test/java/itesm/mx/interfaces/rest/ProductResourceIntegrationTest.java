package itesm.mx.interfaces.rest;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;
import itesm.mx.application.dto.RegisterProductDto;
import itesm.mx.application.dto.UpdateProductDto;
import itesm.mx.application.usecase.marketplace.product.*;
import itesm.mx.domain.models.marketplace.*;
import itesm.mx.domain.models.user.TechnicalSeller;
import itesm.mx.domain.models.user.User;
import itesm.mx.infrastructure.firebase.FirebaseTokenVerifier;
import itesm.mx.infrastructure.firebase.FirebaseUserManager;
import itesm.mx.infrastructure.persistence.entity.users.UserEntity;
import itesm.mx.infrastructure.persistence.repository.user.UserRepositoryImpl;
import itesm.mx.support.H2TestProfile;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
@TestProfile(H2TestProfile.class)
class ProductResourceIntegrationTest {

    private static final String SELLER_UID = "product-seller-uuid";
    private static final String SELLER2_UID = "product-seller2-uuid";
    private static final String ADMIN_UID = "product-admin-uuid";
    private static final String FARMER_UID = "product-farmer-uuid";

    @InjectMock FirebaseTokenVerifier firebaseTokenVerifier;
    @InjectMock FirebaseUserManager firebaseUserManager;

    @InjectMock RegisterProductUseCase registerProductUseCase;
    @InjectMock UpdateProductUseCase updateProductUseCase;
    @InjectMock DeleteProductUseCase deleteProductUseCase;
    @InjectMock GetAllProductsUseCase getAllProductsUseCase;
    @InjectMock GetProductByIdUseCase getProductByIdUseCase;
    @InjectMock GetProductsBySellerUseCase getProductsBySellerUseCase;
    @InjectMock GetProductsByProviderUseCase getProductsByProviderUseCase;
    @InjectMock GetProductsByStatusUseCase getProductsByStatusUseCase;

    @Inject UserRepositoryImpl userRepository;

    @BeforeEach
    @Transactional
    void setup() {
        userRepository.deleteAll();

        UserEntity seller = new UserEntity();
        seller.firebaseUuid = SELLER_UID;
        seller.name = "Product Seller";
        seller.email = "product-seller@test.com";
        seller.roleId = 3;
        userRepository.persist(seller);

        UserEntity seller2 = new UserEntity();
        seller2.firebaseUuid = SELLER2_UID;
        seller2.name = "Product Seller 2";
        seller2.email = "product-seller2@test.com";
        seller2.roleId = 3;
        userRepository.persist(seller2);

        UserEntity admin = new UserEntity();
        admin.firebaseUuid = ADMIN_UID;
        admin.name = "Product Admin";
        admin.email = "product-admin@test.com";
        admin.roleId = 1;
        userRepository.persist(admin);

        UserEntity farmer = new UserEntity();
        farmer.firebaseUuid = FARMER_UID;
        farmer.name = "Product Farmer";
        farmer.email = "product-farmer@test.com";
        farmer.roleId = 2;
        userRepository.persist(farmer);
    }

    private Long sellerUserId() {
        return userRepository.findByFirebaseUuid(SELLER_UID).orElseThrow().getUserId();
    }

    private Long seller2UserId() {
        return userRepository.findByFirebaseUuid(SELLER2_UID).orElseThrow().getUserId();
    }

    private Product buildProduct(Long skuSellerId, Long sellerUserId) {
        User user = new User();
        user.setUserId(sellerUserId);

        TechnicalSeller seller = new TechnicalSeller();
        seller.setTechnicalSellerId(1L);
        seller.setUser(user);

        Category category = new Category();
        category.setCategoryId(1L);
        category.setName("Fertilizantes");

        Provider provider = new Provider();
        provider.setProviderId(1L);
        provider.setName("AgroSuministros");

        Unit unit = new Unit();
        unit.setUnitId(1L);
        unit.setName("Kilogramo");

        Status status = new Status();
        status.setStatusId(1L);
        status.setName("Accepted");

        return new Product(skuSellerId, seller, "Fertilizante NPK", "FERT-001",
                category, provider, 250.0, unit, "Descripción de prueba.", status, null);
    }

    private Product buildProductWithPrice(Long skuSellerId, Long sellerUserId, BigDecimal price, LocalDateTime priceDate) {
        Product product = buildProduct(skuSellerId, sellerUserId);
        product.setLatestPrice(price);
        product.setLatestPriceDate(priceDate);
        return product;
    }

    // -------------------------------------------------------------------------
    // POST /api/products
    // -------------------------------------------------------------------------

    @Test
    void registerProduct_asSeller_noImage_returns201() throws Exception {
        String token = "seller-token";
        when(firebaseTokenVerifier.verifyTokenAndGetUid(token)).thenReturn(SELLER_UID);

        Long userId = sellerUserId();
        Product created = buildProduct(999L, userId);
        when(registerProductUseCase.execute(any(Product.class))).thenReturn(created);

        RegisterProductDto dto = new RegisterProductDto();
        dto.sellerId = 1L;
        dto.name = "Fertilizante NPK";
        dto.sku = "FERT-001";
        dto.categoryId = 1L;
        dto.providerId = 1L;
        dto.unitValue = 250.0;
        dto.unitId = 1L;
        dto.description = "Descripción de prueba.";
        dto.statusId = 1L;

        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body(dto)
        .when()
            .post("/api/products")
        .then()
            .statusCode(201)
            .body("skuSellerId", equalTo(999))
            .body("name", equalTo("Fertilizante NPK"))
            .body("sku", equalTo("FERT-001"))
            .body("firebaseImageId", nullValue());
    }

    @Test
    void registerProduct_asSeller_withImage_returns201() throws Exception {
        String token = "seller-token";
        when(firebaseTokenVerifier.verifyTokenAndGetUid(token)).thenReturn(SELLER_UID);

        Long userId = sellerUserId();
        Product created = buildProduct(888L, userId);
        created.setFirebaseImageId("products/seller/fungicida.jpg");
        when(registerProductUseCase.execute(any(Product.class))).thenReturn(created);

        RegisterProductDto dto = new RegisterProductDto();
        dto.sellerId = 1L;
        dto.name = "Fertilizante NPK";
        dto.sku = "FERT-001";
        dto.categoryId = 1L;
        dto.providerId = 1L;
        dto.unitValue = 250.0;
        dto.unitId = 1L;
        dto.description = "Descripción de prueba.";
        dto.statusId = 1L;
        dto.firebaseImageId = "products/seller/fungicida.jpg";

        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body(dto)
        .when()
            .post("/api/products")
        .then()
            .statusCode(201)
            .body("skuSellerId", equalTo(888))
            .body("firebaseImageId", equalTo("products/seller/fungicida.jpg"));
    }

    @Test
    void registerProduct_asFarmer_returns403() throws Exception {
        String token = "farmer-token";
        when(firebaseTokenVerifier.verifyTokenAndGetUid(token)).thenReturn(FARMER_UID);

        RegisterProductDto dto = new RegisterProductDto();
        dto.sellerId = 1L;
        dto.name = "Fertilizante";
        dto.sku = "FERT-001";
        dto.categoryId = 1L;
        dto.providerId = 1L;
        dto.unitValue = 100.0;
        dto.unitId = 1L;
        dto.description = "desc";
        dto.statusId = 1L;

        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body(dto)
        .when()
            .post("/api/products")
        .then()
            .statusCode(403);
    }

    @Test
    void registerProduct_withoutAuth_returns401() {
        RegisterProductDto dto = new RegisterProductDto();
        dto.sellerId = 1L;
        dto.name = "Fertilizante";
        dto.sku = "FERT-001";
        dto.categoryId = 1L;
        dto.providerId = 1L;
        dto.unitValue = 100.0;
        dto.unitId = 1L;
        dto.description = "desc";
        dto.statusId = 1L;

        given()
            .contentType(ContentType.JSON)
            .body(dto)
        .when()
            .post("/api/products")
        .then()
            .statusCode(401);
    }

    // -------------------------------------------------------------------------
    // GET /api/products
    // -------------------------------------------------------------------------

    @Test
    void getAllProducts_asSeller_returns200() throws Exception {
        String token = "seller-token";
        when(firebaseTokenVerifier.verifyTokenAndGetUid(token)).thenReturn(SELLER_UID);

        Long userId = sellerUserId();
        List<Product> products = List.of(buildProduct(1001L, userId), buildProduct(1002L, userId));
        when(getAllProductsUseCase.execute()).thenReturn(products);

        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .get("/api/products")
        .then()
            .statusCode(200)
            .body("[0].skuSellerId", equalTo(1001))
            .body("[1].skuSellerId", equalTo(1002));
    }

    @Test
    void getProductsBySeller_asSeller_returns200() throws Exception {
        String token = "seller-token";
        when(firebaseTokenVerifier.verifyTokenAndGetUid(token)).thenReturn(SELLER_UID);

        Long userId = sellerUserId();
        List<Product> products = List.of(buildProduct(1001L, userId));
        when(getProductsBySellerUseCase.execute(1L)).thenReturn(products);

        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .get("/api/products?sellerId=1")
        .then()
            .statusCode(200)
            .body("[0].skuSellerId", equalTo(1001))
            .body("[0].sellerId", equalTo(1));
    }

    @Test
    void getProducts_withoutAuth_returns401() {
        given()
        .when()
            .get("/api/products")
        .then()
            .statusCode(401);
    }

    // -------------------------------------------------------------------------
    // PUT /api/products/{skuSellerId}
    // -------------------------------------------------------------------------

    @Test
    void updateProduct_asOwnerSeller_returns200() throws Exception {
        String token = "seller-token";
        when(firebaseTokenVerifier.verifyTokenAndGetUid(token)).thenReturn(SELLER_UID);

        Long userId = sellerUserId();
        Product existing = buildProduct(1001L, userId);
        when(getProductByIdUseCase.execute(1001L)).thenReturn(existing);

        Product updated = buildProduct(1001L, userId);
        updated.setUnitValue(300.0);
        updated.setFirebaseImageId("products/seller/updated.jpg");
        when(updateProductUseCase.execute(eq(1001L), any(Product.class))).thenReturn(updated);

        UpdateProductDto dto = new UpdateProductDto();
        dto.name = "Fertilizante NPK";
        dto.sku = "FERT-001";
        dto.categoryId = 1L;
        dto.providerId = 1L;
        dto.unitValue = 300.0;
        dto.unitId = 1L;
        dto.description = "Precio actualizado.";
        dto.statusId = 1L;
        dto.firebaseImageId = "products/seller/updated.jpg";

        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body(dto)
        .when()
            .put("/api/products/1001")
        .then()
            .statusCode(200)
            .body("skuSellerId", equalTo(1001))
            .body("unitValue", equalTo(300.0f))
            .body("firebaseImageId", equalTo("products/seller/updated.jpg"));
    }

    @Test
    void updateProduct_asSellerNotOwner_returns403() throws Exception {
        String token = "seller2-token";
        when(firebaseTokenVerifier.verifyTokenAndGetUid(token)).thenReturn(SELLER2_UID);

        Long seller1UserId = sellerUserId();
        Product existing = buildProduct(1001L, seller1UserId);
        when(getProductByIdUseCase.execute(1001L)).thenReturn(existing);

        UpdateProductDto dto = new UpdateProductDto();
        dto.name = "Fertilizante NPK";
        dto.sku = "FERT-001";
        dto.categoryId = 1L;
        dto.providerId = 1L;
        dto.unitValue = 300.0;
        dto.unitId = 1L;
        dto.description = "Intento de actualizar producto ajeno.";
        dto.statusId = 1L;

        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body(dto)
        .when()
            .put("/api/products/1001")
        .then()
            .statusCode(403)
            .body("error", equalTo("No tienes permiso para actualizar este producto"));
    }

    @Test
    void updateProduct_asAdmin_returns200() throws Exception {
        String token = "admin-token";
        when(firebaseTokenVerifier.verifyTokenAndGetUid(token)).thenReturn(ADMIN_UID);

        Long seller1UserId = sellerUserId();
        Product existing = buildProduct(1001L, seller1UserId);
        when(getProductByIdUseCase.execute(1001L)).thenReturn(existing);

        Product updated = buildProduct(1001L, seller1UserId);
        updated.setUnitValue(99.0);
        when(updateProductUseCase.execute(eq(1001L), any(Product.class))).thenReturn(updated);

        UpdateProductDto dto = new UpdateProductDto();
        dto.name = "Fertilizante NPK";
        dto.sku = "FERT-001";
        dto.categoryId = 1L;
        dto.providerId = 1L;
        dto.unitValue = 99.0;
        dto.unitId = 1L;
        dto.description = "Admin update.";
        dto.statusId = 1L;

        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body(dto)
        .when()
            .put("/api/products/1001")
        .then()
            .statusCode(200)
            .body("skuSellerId", equalTo(1001));
    }

    @Test
    void updateProduct_notFound_returns404() throws Exception {
        String token = "seller-token";
        when(firebaseTokenVerifier.verifyTokenAndGetUid(token)).thenReturn(SELLER_UID);
        when(getProductByIdUseCase.execute(9999L)).thenThrow(new IllegalArgumentException("Product not found"));

        UpdateProductDto dto = new UpdateProductDto();
        dto.name = "X";
        dto.sku = "X-001";
        dto.categoryId = 1L;
        dto.providerId = 1L;
        dto.unitValue = 1.0;
        dto.unitId = 1L;
        dto.description = "X";
        dto.statusId = 1L;

        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body(dto)
        .when()
            .put("/api/products/9999")
        .then()
            .statusCode(404)
            .body("error", equalTo("Producto no encontrado"));
    }

    // -------------------------------------------------------------------------
    // DELETE /api/products/{skuSellerId}
    // -------------------------------------------------------------------------

    @Test
    void deleteProduct_asOwnerSeller_returns204() throws Exception {
        String token = "seller-token";
        when(firebaseTokenVerifier.verifyTokenAndGetUid(token)).thenReturn(SELLER_UID);

        Long userId = sellerUserId();
        Product existing = buildProduct(1001L, userId);
        when(getProductByIdUseCase.execute(1001L)).thenReturn(existing);
        when(deleteProductUseCase.execute(1001L)).thenReturn(true);

        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .delete("/api/products/1001")
        .then()
            .statusCode(204);
    }

    @Test
    void deleteProduct_asSellerNotOwner_returns403() throws Exception {
        String token = "seller2-token";
        when(firebaseTokenVerifier.verifyTokenAndGetUid(token)).thenReturn(SELLER2_UID);

        Long seller1UserId = sellerUserId();
        Product existing = buildProduct(1001L, seller1UserId);
        when(getProductByIdUseCase.execute(1001L)).thenReturn(existing);

        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .delete("/api/products/1001")
        .then()
            .statusCode(403)
            .body("error", equalTo("No tienes permiso para eliminar este producto"));
    }

    @Test
    void deleteProduct_asAdmin_returns204() throws Exception {
        String token = "admin-token";
        when(firebaseTokenVerifier.verifyTokenAndGetUid(token)).thenReturn(ADMIN_UID);

        Long seller1UserId = sellerUserId();
        Product existing = buildProduct(1001L, seller1UserId);
        when(getProductByIdUseCase.execute(1001L)).thenReturn(existing);
        when(deleteProductUseCase.execute(1001L)).thenReturn(true);

        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .delete("/api/products/1001")
        .then()
            .statusCode(204);
    }

    @Test
    void deleteProduct_notFound_returns404() throws Exception {
        String token = "seller-token";
        when(firebaseTokenVerifier.verifyTokenAndGetUid(token)).thenReturn(SELLER_UID);
        when(getProductByIdUseCase.execute(9999L)).thenThrow(new IllegalArgumentException("Product not found"));

        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .delete("/api/products/9999")
        .then()
            .statusCode(404)
            .body("error", equalTo("Producto no encontrado"));
    }

    // -------------------------------------------------------------------------
    // Price-aware product flows (latestPrice on responses, register with price,
    // update with/without price)
    // -------------------------------------------------------------------------

    @Test
    void getAllProducts_includesLatestPriceInResponse_returns200() throws Exception {
        String token = "seller-token";
        when(firebaseTokenVerifier.verifyTokenAndGetUid(token)).thenReturn(SELLER_UID);

        Long userId = sellerUserId();
        LocalDateTime when = LocalDateTime.of(2025, 5, 2, 14, 0, 0);
        List<Product> products = List.of(
                buildProductWithPrice(1001L, userId, new BigDecimal("250.00000"), when),
                buildProductWithPrice(1002L, userId, new BigDecimal("180.00000"), when));
        when(getAllProductsUseCase.execute()).thenReturn(products);

        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .get("/api/products")
        .then()
            .statusCode(200)
            .body("[0].skuSellerId", equalTo(1001))
            .body("[0].latestPrice", notNullValue())
            .body("[0].latestPriceDate", notNullValue())
            .body("[1].skuSellerId", equalTo(1002))
            .body("[1].latestPrice", notNullValue())
            .body("[1].latestPriceDate", notNullValue());
    }

    @Test
    void getProductsBySeller_includesLatestPriceInResponse_returns200() throws Exception {
        String token = "seller-token";
        when(firebaseTokenVerifier.verifyTokenAndGetUid(token)).thenReturn(SELLER_UID);

        Long userId = sellerUserId();
        LocalDateTime when = LocalDateTime.of(2025, 5, 2, 14, 0, 0);
        List<Product> products = List.of(
                buildProductWithPrice(1001L, userId, new BigDecimal("250.00000"), when));
        when(getProductsBySellerUseCase.execute(1L)).thenReturn(products);

        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .get("/api/products?sellerId=1")
        .then()
            .statusCode(200)
            .body("[0].skuSellerId", equalTo(1001))
            .body("[0].sellerId", equalTo(1))
            .body("[0].latestPrice", notNullValue())
            .body("[0].latestPriceDate", notNullValue());
    }

    @Test
    void registerProduct_withPrice_returnsLatestPriceInResponse_201() throws Exception {
        String token = "seller-token";
        when(firebaseTokenVerifier.verifyTokenAndGetUid(token)).thenReturn(SELLER_UID);

        Long userId = sellerUserId();
        LocalDateTime now = LocalDateTime.now();
        Product created = buildProductWithPrice(777L, userId, new BigDecimal("175.50000"), now);
        when(registerProductUseCase.execute(any(Product.class))).thenReturn(created);

        RegisterProductDto dto = new RegisterProductDto();
        dto.sellerId = 1L;
        dto.name = "Fertilizante NPK";
        dto.sku = "FERT-001";
        dto.categoryId = 1L;
        dto.providerId = 1L;
        dto.unitValue = 100.0;
        dto.unitId = 1L;
        dto.description = "Producto con precio inicial.";
        dto.statusId = 1L;
        dto.price = new BigDecimal("175.50000");

        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body(dto)
        .when()
            .post("/api/products")
        .then()
            .statusCode(201)
            .body("skuSellerId", equalTo(777))
            .body("latestPrice", notNullValue())
            .body("latestPriceDate", notNullValue());
    }

    @Test
    void updateProduct_withNewPrice_returnsUpdatedLatestPrice_200() throws Exception {
        String token = "admin-token";
        when(firebaseTokenVerifier.verifyTokenAndGetUid(token)).thenReturn(ADMIN_UID);

        Long sellerId = sellerUserId();
        Product existing = buildProductWithPrice(1001L, sellerId,
                new BigDecimal("175.50000"), LocalDateTime.now().minusDays(1));
        when(getProductByIdUseCase.execute(1001L)).thenReturn(existing);

        Product updated = buildProductWithPrice(1001L, sellerId,
                new BigDecimal("200.00000"), LocalDateTime.now());
        when(updateProductUseCase.execute(eq(1001L), any(Product.class))).thenReturn(updated);

        UpdateProductDto dto = new UpdateProductDto();
        dto.name = "Fertilizante NPK";
        dto.sku = "FERT-001";
        dto.categoryId = 1L;
        dto.providerId = 1L;
        dto.unitValue = 100.0;
        dto.unitId = 1L;
        dto.description = "Update con nuevo precio.";
        dto.statusId = 1L;
        dto.price = new BigDecimal("200.00000");

        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body(dto)
        .when()
            .put("/api/products/1001")
        .then()
            .statusCode(200)
            .body("skuSellerId", equalTo(1001))
            .body("latestPrice", notNullValue())
            .body("latestPriceDate", notNullValue());

        verify(updateProductUseCase).execute(eq(1001L), any(Product.class));
    }

    @Test
    void updateProduct_withoutPrice_returns200() throws Exception {
        String token = "admin-token";
        when(firebaseTokenVerifier.verifyTokenAndGetUid(token)).thenReturn(ADMIN_UID);

        Long sellerId = sellerUserId();
        Product existing = buildProductWithPrice(1001L, sellerId,
                new BigDecimal("200.00000"), LocalDateTime.now().minusHours(1));
        when(getProductByIdUseCase.execute(1001L)).thenReturn(existing);

        Product updated = buildProductWithPrice(1001L, sellerId,
                new BigDecimal("200.00000"), existing.getLatestPriceDate());
        when(updateProductUseCase.execute(eq(1001L), any(Product.class))).thenReturn(updated);

        UpdateProductDto dto = new UpdateProductDto();
        dto.name = "Fertilizante NPK";
        dto.sku = "FERT-001";
        dto.categoryId = 1L;
        dto.providerId = 1L;
        dto.unitValue = 100.0;
        dto.unitId = 1L;
        dto.description = "Update sin tocar precio.";
        dto.statusId = 1L;
        // dto.price intentionally omitted (null)

        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body(dto)
        .when()
            .put("/api/products/1001")
        .then()
            .statusCode(200)
            .body("skuSellerId", equalTo(1001))
            .body("latestPrice", notNullValue());

        // Resource must still hand the request off to the use case even when price is null;
        // the no-op rule lives inside UpdateProductUseCase, not the resource.
        verify(updateProductUseCase).execute(eq(1001L), any(Product.class));
        verify(registerProductUseCase, never()).execute(any(Product.class));
    }
}
