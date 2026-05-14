package itesm.mx.interfaces.rest;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;
import itesm.mx.application.dto.RegisterInventoryDto;
import itesm.mx.application.dto.UpdateInventoryDto;
import itesm.mx.application.usecase.marketplace.inventory.DeleteInventoryUseCase;
import itesm.mx.application.usecase.marketplace.inventory.GetCurrentStockUseCase;
import itesm.mx.application.usecase.marketplace.inventory.GetInventoryHistoryUseCase;
import itesm.mx.application.usecase.marketplace.inventory.RegisterInventoryUseCase;
import itesm.mx.application.usecase.marketplace.inventory.UpdateInventoryUseCase;
import itesm.mx.application.usecase.marketplace.product.GetProductByIdUseCase;
import itesm.mx.domain.models.marketplace.Category;
import itesm.mx.domain.models.marketplace.Inventory;
import itesm.mx.domain.models.marketplace.InventoryAction;
import itesm.mx.domain.models.marketplace.InventoryActionConstants;
import itesm.mx.domain.models.marketplace.Product;
import itesm.mx.domain.models.marketplace.Provider;
import itesm.mx.domain.models.marketplace.Status;
import itesm.mx.domain.models.marketplace.Unit;
import itesm.mx.domain.models.user.TechnicalSeller;
import itesm.mx.domain.models.user.User;
import itesm.mx.domain.repository.marketplace.InventoryRepository;
import itesm.mx.infrastructure.firebase.FirebaseTokenVerifier;
import itesm.mx.infrastructure.firebase.FirebaseUserManager;
import itesm.mx.infrastructure.persistence.entity.users.UserEntity;
import itesm.mx.infrastructure.persistence.repository.user.UserRepositoryImpl;
import itesm.mx.support.H2TestProfile;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@QuarkusTest
@TestProfile(H2TestProfile.class)
class InventoryResourceIntegrationTest {

    private static final String SELLER_UID = "inv-seller-uuid";
    private static final String SELLER2_UID = "inv-seller2-uuid";
    private static final String ADMIN_UID = "inv-admin-uuid";

    @InjectMock FirebaseTokenVerifier firebaseTokenVerifier;
    @InjectMock FirebaseUserManager firebaseUserManager;

    @InjectMock RegisterInventoryUseCase registerInventoryUseCase;
    @InjectMock UpdateInventoryUseCase updateInventoryUseCase;
    @InjectMock DeleteInventoryUseCase deleteInventoryUseCase;
    @InjectMock GetInventoryHistoryUseCase getInventoryHistoryUseCase;
    @InjectMock GetCurrentStockUseCase getCurrentStockUseCase;
    @InjectMock GetProductByIdUseCase getProductByIdUseCase;
    @InjectMock InventoryRepository inventoryRepository;

    @Inject UserRepositoryImpl userRepository;

    @BeforeEach
    @Transactional
    void setup() {
        userRepository.deleteAll();

        UserEntity seller = new UserEntity();
        seller.firebaseUuid = SELLER_UID;
        seller.name = "Inv Seller";
        seller.email = "inv-seller@test.com";
        seller.roleId = 3;
        userRepository.persist(seller);

        UserEntity seller2 = new UserEntity();
        seller2.firebaseUuid = SELLER2_UID;
        seller2.name = "Inv Seller 2";
        seller2.email = "inv-seller2@test.com";
        seller2.roleId = 3;
        userRepository.persist(seller2);

        UserEntity admin = new UserEntity();
        admin.firebaseUuid = ADMIN_UID;
        admin.name = "Inv Admin";
        admin.email = "inv-admin@test.com";
        admin.roleId = 1;
        userRepository.persist(admin);
    }

    private Long sellerUserId() {
        return userRepository.findByFirebaseUuid(SELLER_UID).orElseThrow().getUserId();
    }

    private Product buildProduct(Long skuSellerId, Long ownerUserId) {
        User user = new User();
        user.setUserId(ownerUserId);

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

    private Inventory buildInventoryRow(Long inventoryId, Long skuSellerId, Long actionId, Integer cantidad) {
        Product product = new Product();
        product.setSkuSellerId(skuSellerId);
        InventoryAction action = new InventoryAction();
        action.setInventoryActionId(actionId);
        action.setAccion(InventoryActionConstants.ADD.equals(actionId) ? "add" : "subtract");
        return new Inventory(inventoryId, product, cantidad, action);
    }

    // -------------------- POST /add --------------------

    @Test
    void addStock_asAdmin_returns201() throws Exception {
        String token = "admin-token";
        when(firebaseTokenVerifier.verifyTokenAndGetUid(token)).thenReturn(ADMIN_UID);

        when(getProductByIdUseCase.execute(1001L)).thenReturn(buildProduct(1001L, sellerUserId()));
        Inventory saved = buildInventoryRow(42L, 1001L, InventoryActionConstants.ADD, 50);
        when(registerInventoryUseCase.execute(any(Inventory.class))).thenReturn(saved);

        RegisterInventoryDto dto = new RegisterInventoryDto();
        dto.cantidad = 50;

        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body(dto)
        .when()
            .post("/api/inventory/product/1001/add")
        .then()
            .statusCode(201)
            .body("inventoryId", equalTo(42))
            .body("skuSellerId", equalTo(1001))
            .body("actionId", equalTo(1))
            .body("actionName", equalTo("add"))
            .body("cantidad", equalTo(50));
    }

    @Test
    void addStock_asOwnerSeller_returns201() throws Exception {
        String token = "seller-token";
        when(firebaseTokenVerifier.verifyTokenAndGetUid(token)).thenReturn(SELLER_UID);

        when(getProductByIdUseCase.execute(1001L)).thenReturn(buildProduct(1001L, sellerUserId()));
        Inventory saved = buildInventoryRow(43L, 1001L, InventoryActionConstants.ADD, 20);
        when(registerInventoryUseCase.execute(any(Inventory.class))).thenReturn(saved);

        RegisterInventoryDto dto = new RegisterInventoryDto();
        dto.cantidad = 20;

        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body(dto)
        .when()
            .post("/api/inventory/product/1001/add")
        .then()
            .statusCode(201)
            .body("cantidad", equalTo(20));
    }

    @Test
    void addStock_asNonOwnerSeller_returns403() throws Exception {
        String token = "seller2-token";
        when(firebaseTokenVerifier.verifyTokenAndGetUid(token)).thenReturn(SELLER2_UID);
        when(getProductByIdUseCase.execute(1001L)).thenReturn(buildProduct(1001L, sellerUserId()));

        RegisterInventoryDto dto = new RegisterInventoryDto();
        dto.cantidad = 10;

        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body(dto)
        .when()
            .post("/api/inventory/product/1001/add")
        .then()
            .statusCode(403);
    }

    @Test
    void addStock_withoutAuth_returns401() throws Exception {
        RegisterInventoryDto dto = new RegisterInventoryDto();
        dto.cantidad = 10;

        given()
            .contentType(ContentType.JSON)
            .body(dto)
        .when()
            .post("/api/inventory/product/1001/add")
        .then()
            .statusCode(401);
    }

    @Test
    void addStock_productNotFound_returns404() throws Exception {
        String token = "admin-token";
        when(firebaseTokenVerifier.verifyTokenAndGetUid(token)).thenReturn(ADMIN_UID);
        when(getProductByIdUseCase.execute(9999L)).thenThrow(new IllegalArgumentException("Product not found"));

        RegisterInventoryDto dto = new RegisterInventoryDto();
        dto.cantidad = 10;

        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body(dto)
        .when()
            .post("/api/inventory/product/9999/add")
        .then()
            .statusCode(404);
    }

    // -------------------- POST /remove --------------------

    @Test
    void removeStock_negativeStock_returns400() throws Exception {
        String token = "admin-token";
        when(firebaseTokenVerifier.verifyTokenAndGetUid(token)).thenReturn(ADMIN_UID);

        when(getProductByIdUseCase.execute(1001L)).thenReturn(buildProduct(1001L, sellerUserId()));
        when(registerInventoryUseCase.execute(any(Inventory.class)))
                .thenThrow(new IllegalArgumentException("Operation would result in negative stock"));

        RegisterInventoryDto dto = new RegisterInventoryDto();
        dto.cantidad = 9999;

        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body(dto)
        .when()
            .post("/api/inventory/product/1001/remove")
        .then()
            .statusCode(400);
    }

    // -------------------- GET /history --------------------

    @Test
    void getHistory_asAdmin_returns200WithList() throws Exception {
        String token = "admin-token";
        when(firebaseTokenVerifier.verifyTokenAndGetUid(token)).thenReturn(ADMIN_UID);
        when(getProductByIdUseCase.execute(1001L)).thenReturn(buildProduct(1001L, sellerUserId()));

        List<Inventory> history = List.of(
                buildInventoryRow(4L, 1001L, InventoryActionConstants.ADD, 20),
                buildInventoryRow(3L, 1001L, InventoryActionConstants.SUBTRACT, 30),
                buildInventoryRow(2L, 1001L, InventoryActionConstants.ADD, 50),
                buildInventoryRow(1L, 1001L, InventoryActionConstants.ADD, 100)
        );
        when(getInventoryHistoryUseCase.execute(1001L)).thenReturn(history);

        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .get("/api/inventory/product/1001/history")
        .then()
            .statusCode(200)
            .body("[0].inventoryId", equalTo(4))
            .body("[0].actionName", equalTo("add"))
            .body("[1].actionName", equalTo("subtract"))
            .body("[3].inventoryId", equalTo(1));
    }

    @Test
    void getHistory_asNonOwnerSeller_returns403() throws Exception {
        String token = "seller2-token";
        when(firebaseTokenVerifier.verifyTokenAndGetUid(token)).thenReturn(SELLER2_UID);
        when(getProductByIdUseCase.execute(1001L)).thenReturn(buildProduct(1001L, sellerUserId()));

        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .get("/api/inventory/product/1001/history")
        .then()
            .statusCode(403);
    }

    // -------------------- GET /summary --------------------

    @Test
    void getSummary_asAdmin_returns200WithStock() throws Exception {
        String token = "admin-token";
        when(firebaseTokenVerifier.verifyTokenAndGetUid(token)).thenReturn(ADMIN_UID);
        when(getProductByIdUseCase.execute(1001L)).thenReturn(buildProduct(1001L, sellerUserId()));
        when(getCurrentStockUseCase.execute(1001L)).thenReturn(140);

        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .get("/api/inventory/product/1001/summary")
        .then()
            .statusCode(200)
            .body("skuSellerId", equalTo(1001))
            .body("stock", equalTo(140));
    }

    // -------------------- PUT /{inventoryId} --------------------

    @Test
    void updateRow_asOwnerSeller_returns200() throws Exception {
        String token = "seller-token";
        when(firebaseTokenVerifier.verifyTokenAndGetUid(token)).thenReturn(SELLER_UID);

        Inventory existing = buildInventoryRow(7L, 1001L, InventoryActionConstants.ADD, 50);
        when(inventoryRepository.findByInventoryId(7L)).thenReturn(Optional.of(existing));
        when(getProductByIdUseCase.execute(1001L)).thenReturn(buildProduct(1001L, sellerUserId()));

        Inventory updated = buildInventoryRow(7L, 1001L, InventoryActionConstants.ADD, 80);
        when(updateInventoryUseCase.execute(eq(7L), eq(80))).thenReturn(updated);

        UpdateInventoryDto dto = new UpdateInventoryDto();
        dto.cantidad = 80;

        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body(dto)
        .when()
            .put("/api/inventory/7")
        .then()
            .statusCode(200)
            .body("inventoryId", equalTo(7))
            .body("cantidad", equalTo(80));
    }

    @Test
    void updateRow_notFound_returns404() throws Exception {
        String token = "admin-token";
        when(firebaseTokenVerifier.verifyTokenAndGetUid(token)).thenReturn(ADMIN_UID);
        when(inventoryRepository.findByInventoryId(anyLong())).thenReturn(Optional.empty());

        UpdateInventoryDto dto = new UpdateInventoryDto();
        dto.cantidad = 10;

        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body(dto)
        .when()
            .put("/api/inventory/9999")
        .then()
            .statusCode(404);
    }

    @Test
    void updateRow_wouldGoNegative_returns400() throws Exception {
        String token = "admin-token";
        when(firebaseTokenVerifier.verifyTokenAndGetUid(token)).thenReturn(ADMIN_UID);

        Inventory existing = buildInventoryRow(7L, 1001L, InventoryActionConstants.ADD, 50);
        when(inventoryRepository.findByInventoryId(7L)).thenReturn(Optional.of(existing));
        when(getProductByIdUseCase.execute(1001L)).thenReturn(buildProduct(1001L, sellerUserId()));
        when(updateInventoryUseCase.execute(eq(7L), eq(1)))
                .thenThrow(new IllegalArgumentException("Operation would result in negative stock"));

        UpdateInventoryDto dto = new UpdateInventoryDto();
        dto.cantidad = 1;

        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body(dto)
        .when()
            .put("/api/inventory/7")
        .then()
            .statusCode(400)
            .body("error", notNullValue());
    }

    // -------------------- DELETE /{inventoryId} --------------------

    @Test
    void deleteRow_asAdmin_returns204() throws Exception {
        String token = "admin-token";
        when(firebaseTokenVerifier.verifyTokenAndGetUid(token)).thenReturn(ADMIN_UID);

        Inventory existing = buildInventoryRow(5L, 1001L, InventoryActionConstants.SUBTRACT, 30);
        when(inventoryRepository.findByInventoryId(5L)).thenReturn(Optional.of(existing));
        when(getProductByIdUseCase.execute(1001L)).thenReturn(buildProduct(1001L, sellerUserId()));

        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .delete("/api/inventory/5")
        .then()
            .statusCode(204);
    }

    @Test
    void deleteRow_wouldGoNegative_returns400() throws Exception {
        String token = "admin-token";
        when(firebaseTokenVerifier.verifyTokenAndGetUid(token)).thenReturn(ADMIN_UID);

        Inventory existing = buildInventoryRow(5L, 1001L, InventoryActionConstants.ADD, 200);
        when(inventoryRepository.findByInventoryId(5L)).thenReturn(Optional.of(existing));
        when(getProductByIdUseCase.execute(1001L)).thenReturn(buildProduct(1001L, sellerUserId()));
        org.mockito.Mockito.doThrow(new IllegalArgumentException("Removing this row would result in negative stock"))
                .when(deleteInventoryUseCase).execute(5L);

        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .delete("/api/inventory/5")
        .then()
            .statusCode(400);
    }
}
