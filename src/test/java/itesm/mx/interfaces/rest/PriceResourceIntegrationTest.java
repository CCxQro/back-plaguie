package itesm.mx.interfaces.rest;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import itesm.mx.application.usecase.marketplace.price.GetAllPricesUseCase;
import itesm.mx.application.usecase.marketplace.product.GetProductByIdUseCase;
import itesm.mx.domain.models.marketplace.Category;
import itesm.mx.domain.models.marketplace.Price;
import itesm.mx.domain.models.marketplace.Product;
import itesm.mx.domain.models.marketplace.Provider;
import itesm.mx.domain.models.marketplace.Status;
import itesm.mx.domain.models.marketplace.Unit;
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
import static org.mockito.Mockito.when;

@QuarkusTest
@TestProfile(H2TestProfile.class)
class PriceResourceIntegrationTest {

    private static final String SELLER_UID = "price-seller-uuid";
    private static final String SELLER2_UID = "price-seller2-uuid";
    private static final String ADMIN_UID = "price-admin-uuid";

    @InjectMock FirebaseTokenVerifier firebaseTokenVerifier;
    @InjectMock FirebaseUserManager firebaseUserManager;

    @InjectMock GetAllPricesUseCase getAllPricesUseCase;
    @InjectMock GetProductByIdUseCase getProductByIdUseCase;

    @Inject UserRepositoryImpl userRepository;

    @BeforeEach
    @Transactional
    void setup() {
        userRepository.deleteAll();

        UserEntity seller = new UserEntity();
        seller.firebaseUuid = SELLER_UID;
        seller.name = "Price Seller";
        seller.email = "price-seller@test.com";
        seller.roleId = 3;
        userRepository.persist(seller);

        UserEntity seller2 = new UserEntity();
        seller2.firebaseUuid = SELLER2_UID;
        seller2.name = "Price Seller 2";
        seller2.email = "price-seller2@test.com";
        seller2.roleId = 3;
        userRepository.persist(seller2);

        UserEntity admin = new UserEntity();
        admin.firebaseUuid = ADMIN_UID;
        admin.name = "Price Admin";
        admin.email = "price-admin@test.com";
        admin.roleId = 1;
        userRepository.persist(admin);
    }

    private Long sellerUserId() {
        return userRepository.findByFirebaseUuid(SELLER_UID).orElseThrow().getUserId();
    }

    private Product buildProduct(Long skuSellerId, Long sellerUserId) {
        User user = new User();
        user.setUserId(sellerUserId);

        TechnicalSeller seller = new TechnicalSeller();
        seller.setTechnicalSellerId(1L);
        seller.setUser(user);

        Category category = new Category();
        category.setCategoryId(1L);

        Provider provider = new Provider();
        provider.setProviderId(1L);

        Unit unit = new Unit();
        unit.setUnitId(1L);

        Status status = new Status();
        status.setStatusId(1L);

        return new Product(skuSellerId, seller, "Producto", "SKU-001",
                category, provider, 100.0, unit, "desc", status, null);
    }

    private Price buildPrice(Long priceId, Long skuSellerId, BigDecimal value, LocalDateTime when) {
        Product product = new Product();
        product.setSkuSellerId(skuSellerId);
        return new Price(priceId, product, value, when);
    }

    @Test
    void getPriceHistory_asAdmin_returns200WithHistory() throws Exception {
        String token = "admin-token";
        when(firebaseTokenVerifier.verifyTokenAndGetUid(token)).thenReturn(ADMIN_UID);

        Long sellerId = sellerUserId();
        when(getProductByIdUseCase.execute(1001L)).thenReturn(buildProduct(1001L, sellerId));

        List<Price> history = List.of(
                buildPrice(2L, 1001L, new BigDecimal("200.00000"), LocalDateTime.of(2025, 5, 2, 14, 0)),
                buildPrice(1L, 1001L, new BigDecimal("175.50000"), LocalDateTime.of(2025, 4, 1, 12, 0))
        );
        when(getAllPricesUseCase.execute(1001L)).thenReturn(history);

        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .get("/api/prices/1001")
        .then()
            .statusCode(200)
            .body("[0].priceId", equalTo(2))
            .body("[0].skuSellerId", equalTo(1001))
            .body("[0].price", notNullValue())
            .body("[0].priceDate", notNullValue())
            .body("[1].priceId", equalTo(1))
            .body("[1].skuSellerId", equalTo(1001));
    }

    @Test
    void getPriceHistory_asOwnerSeller_returns200() throws Exception {
        String token = "seller-token";
        when(firebaseTokenVerifier.verifyTokenAndGetUid(token)).thenReturn(SELLER_UID);

        Long sellerId = sellerUserId();
        when(getProductByIdUseCase.execute(1001L)).thenReturn(buildProduct(1001L, sellerId));

        List<Price> history = List.of(
                buildPrice(1L, 1001L, new BigDecimal("175.50000"), LocalDateTime.of(2025, 4, 1, 12, 0))
        );
        when(getAllPricesUseCase.execute(1001L)).thenReturn(history);

        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .get("/api/prices/1001")
        .then()
            .statusCode(200)
            .body("[0].priceId", equalTo(1))
            .body("[0].skuSellerId", equalTo(1001));
    }

    @Test
    void getPriceHistory_asNonOwnerSeller_returns403() throws Exception {
        String token = "seller2-token";
        when(firebaseTokenVerifier.verifyTokenAndGetUid(token)).thenReturn(SELLER2_UID);

        Long ownerUserId = sellerUserId();
        when(getProductByIdUseCase.execute(1001L)).thenReturn(buildProduct(1001L, ownerUserId));

        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .get("/api/prices/1001")
        .then()
            .statusCode(403)
            .body("error", equalTo("No tienes permiso para ver el historial de precios de este producto"));
    }

    @Test
    void getPriceHistory_productNotFound_returns404() throws Exception {
        String token = "admin-token";
        when(firebaseTokenVerifier.verifyTokenAndGetUid(token)).thenReturn(ADMIN_UID);
        when(getProductByIdUseCase.execute(9999L)).thenThrow(new IllegalArgumentException("Product not found"));

        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .get("/api/prices/9999")
        .then()
            .statusCode(404)
            .body("error", equalTo("Producto no encontrado"));
    }

    @Test
    void getPriceHistory_withoutAuth_returns401() {
        given()
        .when()
            .get("/api/prices/1001")
        .then()
            .statusCode(401);
    }
}
