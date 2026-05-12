package itesm.mx.interfaces.rest;

import com.google.firebase.auth.FirebaseAuthException;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;
import itesm.mx.application.dto.FarmerLocationDto;
import itesm.mx.application.dto.OrderDetailItemDto;
import itesm.mx.application.dto.OrderDetailResponseDto;
import itesm.mx.application.dto.OrderResponseDto;
import itesm.mx.application.dto.RegisterOrderDto;
import itesm.mx.application.usecase.order.CreateOrderUseCase;
import itesm.mx.application.usecase.order.GetFarmerLocationsBySellerUseCase;
import itesm.mx.application.usecase.order.GetOrderByIdUseCase;
import itesm.mx.application.usecase.order.GetOrdersBySellerUseCase;
import itesm.mx.application.usecase.order.UpdateOrderStatusUseCase;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@QuarkusTest
@TestProfile(H2TestProfile.class)
class OrderResourceIntegrationTest {

    @InjectMock FirebaseTokenVerifier firebaseTokenVerifier;
    @InjectMock FirebaseUserManager firebaseUserManager;
    @InjectMock CreateOrderUseCase createOrderUseCase;
    @InjectMock GetOrderByIdUseCase getOrderByIdUseCase;
    @InjectMock GetOrdersBySellerUseCase getOrdersBySellerUseCase;
    @InjectMock GetFarmerLocationsBySellerUseCase getFarmerLocationsBySellerUseCase;
    @InjectMock UpdateOrderStatusUseCase updateOrderStatusUseCase;

    @Inject UserRepositoryImpl userRepository;

    private static final String SELLER_TOKEN = "seller-token";
    private static final String ADMIN_TOKEN  = "admin-token";

    private Long sellerId;

    @BeforeEach
    @Transactional
    void setup() {
        userRepository.deleteAll();

        UserEntity admin = new UserEntity();
        admin.firebaseUuid = "uuid-or-admin";
        admin.name = "Admin";
        admin.email = "oradmin@itesm.mx";
        admin.roleId = 1;
        admin.isActive = true;
        userRepository.persist(admin);

        UserEntity seller = new UserEntity();
        seller.firebaseUuid = "uuid-or-seller";
        seller.name = "Seller";
        seller.email = "orseller@itesm.mx";
        seller.roleId = 3;
        seller.isActive = true;
        userRepository.persist(seller);
        sellerId = seller.userId;
    }

    private OrderResponseDto sampleOrderResponse() {
        return new OrderResponseDto(1L, 1L, "Farmer A", sellerId, "Seller A",
                LocalDateTime.now(), 1L, "Pendiente", BigDecimal.valueOf(500),
                List.of(new OrderDetailResponseDto(1L, 1001L, "Producto X", 2, 250.0f)));
    }

    // --- POST /api/orders ---

    @Test
    void createOrder_WhenNoAuthHeader_Returns401() {
        given()
            .contentType(ContentType.JSON)
            .body("{}")
        .when()
            .post("/api/orders")
        .then()
            .statusCode(401);
    }

    @Test
    void createOrder_WhenNonSeller_Returns403() throws Exception {
        when(firebaseTokenVerifier.verifyTokenAndGetUid(ADMIN_TOKEN)).thenReturn("uuid-or-admin");

        RegisterOrderDto dto = new RegisterOrderDto();
        dto.farmerId = 1L;
        dto.sellerId = 1L;
        dto.orderStatusId = 1L;
        dto.totalAmount = BigDecimal.valueOf(500);
        OrderDetailItemDto item = new OrderDetailItemDto();
        item.productId = 1L;
        item.quantity = 1;
        item.unitPrice = 100.0f;
        dto.details = List.of(item);

        given()
            .header("Authorization", "Bearer " + ADMIN_TOKEN)
            .contentType(ContentType.JSON)
            .body(dto)
        .when()
            .post("/api/orders")
        .then()
            .statusCode(403)
            .body("error", equalTo("Solo un técnico vendedor puede crear pedidos"));
    }

    @Test
    void createOrder_WhenSellerAndValidBody_Returns201() throws Exception {
        when(firebaseTokenVerifier.verifyTokenAndGetUid(SELLER_TOKEN)).thenReturn("uuid-or-seller");
        when(createOrderUseCase.execute(any())).thenReturn(sampleOrderResponse());

        RegisterOrderDto dto = new RegisterOrderDto();
        dto.farmerId = 1L;
        dto.sellerId = 1L;
        dto.orderStatusId = 1L;
        dto.totalAmount = BigDecimal.valueOf(500);
        OrderDetailItemDto item = new OrderDetailItemDto();
        item.productId = 1001L;
        item.quantity = 2;
        item.unitPrice = 250.0f;
        dto.details = List.of(item);

        given()
            .header("Authorization", "Bearer " + SELLER_TOKEN)
            .contentType(ContentType.JSON)
            .body(dto)
        .when()
            .post("/api/orders")
        .then()
            .statusCode(201)
            .body("orderId", equalTo(1))
            .body("totalAmount", notNullValue());
    }

    @Test
    void createOrder_WhenUseCaseThrowsNotFound_Returns404() throws Exception {
        when(firebaseTokenVerifier.verifyTokenAndGetUid(SELLER_TOKEN)).thenReturn("uuid-or-seller");
        when(createOrderUseCase.execute(any()))
                .thenThrow(new IllegalStateException("Agricultor no encontrado con id: 99"));

        RegisterOrderDto dto = new RegisterOrderDto();
        dto.farmerId = 99L;
        dto.sellerId = 1L;
        dto.orderStatusId = 1L;
        dto.totalAmount = BigDecimal.valueOf(500);
        OrderDetailItemDto item = new OrderDetailItemDto();
        item.productId = 1001L;
        item.quantity = 1;
        item.unitPrice = 500.0f;
        dto.details = List.of(item);

        given()
            .header("Authorization", "Bearer " + SELLER_TOKEN)
            .contentType(ContentType.JSON)
            .body(dto)
        .when()
            .post("/api/orders")
        .then()
            .statusCode(404);
    }

    // --- GET /api/orders/{orderId} ---

    @Test
    void getOrderById_WhenNoAuth_Returns401() {
        given()
        .when()
            .get("/api/orders/1")
        .then()
            .statusCode(401);
    }

    @Test
    void getOrderById_WhenAuthenticated_Returns200() throws Exception {
        when(firebaseTokenVerifier.verifyTokenAndGetUid(SELLER_TOKEN)).thenReturn("uuid-or-seller");
        when(getOrderByIdUseCase.execute(1L)).thenReturn(sampleOrderResponse());

        given()
            .header("Authorization", "Bearer " + SELLER_TOKEN)
        .when()
            .get("/api/orders/1")
        .then()
            .statusCode(200)
            .body("orderId", equalTo(1));
    }

    @Test
    void getOrderById_WhenNotFound_Returns404() throws Exception {
        when(firebaseTokenVerifier.verifyTokenAndGetUid(SELLER_TOKEN)).thenReturn("uuid-or-seller");
        when(getOrderByIdUseCase.execute(99L))
                .thenThrow(new IllegalStateException("Pedido no encontrado con id: 99"));

        given()
            .header("Authorization", "Bearer " + SELLER_TOKEN)
        .when()
            .get("/api/orders/99")
        .then()
            .statusCode(404);
    }

    // --- GET /api/orders ---

    @Test
    void getOrders_WhenNonSeller_Returns403() throws Exception {
        when(firebaseTokenVerifier.verifyTokenAndGetUid(ADMIN_TOKEN)).thenReturn("uuid-or-admin");

        given()
            .header("Authorization", "Bearer " + ADMIN_TOKEN)
        .when()
            .get("/api/orders")
        .then()
            .statusCode(403)
            .body("error", equalTo("Solo un técnico vendedor puede listar sus pedidos"));
    }

    @Test
    void getOrders_WhenSeller_Returns200AndList() throws Exception {
        when(firebaseTokenVerifier.verifyTokenAndGetUid(SELLER_TOKEN)).thenReturn("uuid-or-seller");
        when(getOrdersBySellerUseCase.execute(anyLong())).thenReturn(List.of(sampleOrderResponse()));

        given()
            .header("Authorization", "Bearer " + SELLER_TOKEN)
        .when()
            .get("/api/orders")
        .then()
            .statusCode(200)
            .body("size()", equalTo(1));
    }

    // --- GET /api/orders/farmer-locations ---

    @Test
    void getFarmerLocations_WhenNonSeller_Returns403() throws Exception {
        when(firebaseTokenVerifier.verifyTokenAndGetUid(ADMIN_TOKEN)).thenReturn("uuid-or-admin");

        given()
            .header("Authorization", "Bearer " + ADMIN_TOKEN)
        .when()
            .get("/api/orders/farmer-locations")
        .then()
            .statusCode(403);
    }

    @Test
    void getFarmerLocations_WhenSeller_Returns200AndCoords() throws Exception {
        when(firebaseTokenVerifier.verifyTokenAndGetUid(SELLER_TOKEN)).thenReturn("uuid-or-seller");
        when(getFarmerLocationsBySellerUseCase.execute(anyLong())).thenReturn(List.of(
                new FarmerLocationDto(1L, "Juan", 1L, 20.75, -103.48, 1L)
        ));

        given()
            .header("Authorization", "Bearer " + SELLER_TOKEN)
        .when()
            .get("/api/orders/farmer-locations")
        .then()
            .statusCode(200)
            .body("[0].farmerId", equalTo(1))
            .body("[0].latitude", notNullValue())
            .body("[0].longitude", notNullValue());
    }

    // --- PATCH /api/orders/{orderId}/status ---

    @Test
    void updateOrderStatus_WhenSeller_Returns200() throws Exception {
        when(firebaseTokenVerifier.verifyTokenAndGetUid(SELLER_TOKEN)).thenReturn("uuid-or-seller");
        when(updateOrderStatusUseCase.execute(anyLong(), anyLong())).thenReturn(sampleOrderResponse());

        given()
            .header("Authorization", "Bearer " + SELLER_TOKEN)
            .contentType(ContentType.JSON)
            .body("{\"orderStatusId\": 2}")
        .when()
            .patch("/api/orders/1/status")
        .then()
            .statusCode(200)
            .body("orderId", equalTo(1));
    }

    @Test
    void updateOrderStatus_WhenInvalidToken_Returns401() throws Exception {
        FirebaseAuthException mockEx = org.mockito.Mockito.mock(FirebaseAuthException.class);
        when(mockEx.getMessage()).thenReturn("Token inválido");
        when(firebaseTokenVerifier.verifyTokenAndGetUid("bad-token")).thenThrow(mockEx);

        given()
            .header("Authorization", "Bearer bad-token")
            .contentType(ContentType.JSON)
            .body("{\"orderStatusId\": 2}")
        .when()
            .patch("/api/orders/1/status")
        .then()
            .statusCode(401);
    }
}
