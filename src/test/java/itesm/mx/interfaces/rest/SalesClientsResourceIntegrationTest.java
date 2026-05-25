package itesm.mx.interfaces.rest;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;
import itesm.mx.application.dto.ClientAlertaSummaryDto;
import itesm.mx.application.dto.ClientDetailDto;
import itesm.mx.application.dto.ClientMapDto;
import itesm.mx.application.dto.ClientOrderSummaryDto;
import itesm.mx.application.dto.ClientParcelaSummaryDto;
import itesm.mx.application.usecase.sales.GetClientDetailBySellerUseCase;
import itesm.mx.application.usecase.sales.GetClientsMapBySellerUseCase;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@QuarkusTest
@TestProfile(H2TestProfile.class)
class SalesClientsResourceIntegrationTest {

    @InjectMock FirebaseTokenVerifier firebaseTokenVerifier;
    @InjectMock FirebaseUserManager firebaseUserManager;
    @InjectMock GetClientsMapBySellerUseCase getClientsMapBySellerUseCase;
    @InjectMock GetClientDetailBySellerUseCase getClientDetailBySellerUseCase;

    @Inject UserRepositoryImpl userRepository;

    private static final String SELLER_TOKEN = "sales-clients-seller-token";
    private static final String ADMIN_TOKEN = "sales-clients-admin-token";
    private static final String FARMER_TOKEN = "sales-clients-farmer-token";

    private Long sellerUserId;

    @BeforeEach
    @Transactional
    void setup() {
        userRepository.deleteAll();

        UserEntity admin = new UserEntity();
        admin.firebaseUuid = "sales-clients-admin-uuid";
        admin.name = "Admin";
        admin.email = "admin@sales-clients.test";
        admin.roleId = 1;
        admin.isActive = true;
        userRepository.persist(admin);

        UserEntity seller = new UserEntity();
        seller.firebaseUuid = "sales-clients-seller-uuid";
        seller.name = "Seller";
        seller.email = "seller@sales-clients.test";
        seller.roleId = 3;
        seller.isActive = true;
        userRepository.persist(seller);
        sellerUserId = seller.userId;

        UserEntity farmer = new UserEntity();
        farmer.firebaseUuid = "sales-clients-farmer-uuid";
        farmer.name = "Farmer";
        farmer.email = "farmer@sales-clients.test";
        farmer.roleId = 2;
        farmer.isActive = true;
        userRepository.persist(farmer);
    }

    private ClientMapDto sampleClientMapDto() {
        return new ClientMapDto(
                10L, 100L, "Juan", "juan@test.mx",
                20.59, -100.39, 200L,
                "Querétaro", "El Marqués", "La Cañada",
                List.of("Maíz"), List.of("Siembra"),
                1, 2.5,
                true, 1, "alta",
                3, LocalDateTime.parse("2026-01-10T08:30:00")
        );
    }

    private ClientDetailDto sampleClientDetailDto() {
        ClientParcelaSummaryDto parcela = new ClientParcelaSummaryDto(
                1L, "Parcela 1", 2.5, "Maíz", "Siembra", "Goteo", 7.0, null, null, true);
        ClientAlertaSummaryDto alerta = new ClientAlertaSummaryDto(
                1L, "Alerta plaga", "Pulgón", "alta",
                new BigDecimal("1.50"), LocalDateTime.parse("2026-01-10T08:30:00"),
                2L, "Revision", true);
        ClientOrderSummaryDto orderSummary = new ClientOrderSummaryDto(
                3, new BigDecimal("1500.00"),
                LocalDateTime.parse("2026-01-10T08:30:00"), "Pendiente");

        return new ClientDetailDto(
                10L, 100L, "Juan", "juan@test.mx", true,
                200L, 20.59, -100.39,
                "Querétaro", "El Marqués", "La Cañada", "Predio Uno",
                List.of(parcela), List.of(alerta), orderSummary
        );
    }

    // --- GET /api/sales/clients ---

    @Test
    void getClients_WhenNoAuth_Returns401() {
        given()
        .when()
            .get("/api/sales/clients")
        .then()
            .statusCode(401);
    }

    @Test
    void getClients_WhenNonSeller_Returns403() throws Exception {
        when(firebaseTokenVerifier.verifyTokenAndGetUid(ADMIN_TOKEN))
                .thenReturn("sales-clients-admin-uuid");

        given()
            .header("Authorization", "Bearer " + ADMIN_TOKEN)
        .when()
            .get("/api/sales/clients")
        .then()
            .statusCode(403)
            .body("error", equalTo("Solo un técnico vendedor puede consultar sus clientes"));
    }

    @Test
    void getClients_WhenSeller_Returns200() throws Exception {
        when(firebaseTokenVerifier.verifyTokenAndGetUid(SELLER_TOKEN))
                .thenReturn("sales-clients-seller-uuid");
        when(getClientsMapBySellerUseCase.execute(eq(sellerUserId), any()))
                .thenReturn(List.of(sampleClientMapDto()));

        given()
            .header("Authorization", "Bearer " + SELLER_TOKEN)
        .when()
            .get("/api/sales/clients")
        .then()
            .statusCode(200)
            .body("[0].farmerId", equalTo(10))
            .body("[0].name", equalTo("Juan"))
            .body("[0].state", equalTo("Querétaro"))
            .body("[0].hasActiveAlerts", equalTo(true))
            .body("[0].cultivos[0]", equalTo("Maíz"));
    }

    @Test
    void getClients_WithFilters_DelegatesToUseCase() throws Exception {
        when(firebaseTokenVerifier.verifyTokenAndGetUid(SELLER_TOKEN))
                .thenReturn("sales-clients-seller-uuid");
        when(getClientsMapBySellerUseCase.execute(eq(sellerUserId), any()))
                .thenReturn(List.of(sampleClientMapDto()));

        given()
            .header("Authorization", "Bearer " + SELLER_TOKEN)
            .queryParam("cultivo", "Maíz")
            .queryParam("onlyWithActiveAlerts", true)
        .when()
            .get("/api/sales/clients")
        .then()
            .statusCode(200)
            .body("[0].farmerId", equalTo(10));
    }

    // --- GET /api/sales/clients/{farmerId} ---

    @Test
    void getClientDetail_WhenNoAuth_Returns401() {
        given()
        .when()
            .get("/api/sales/clients/10")
        .then()
            .statusCode(401);
    }

    @Test
    void getClientDetail_WhenFarmerRole_Returns403() throws Exception {
        when(firebaseTokenVerifier.verifyTokenAndGetUid(FARMER_TOKEN))
                .thenReturn("sales-clients-farmer-uuid");

        given()
            .header("Authorization", "Bearer " + FARMER_TOKEN)
        .when()
            .get("/api/sales/clients/10")
        .then()
            .statusCode(403);
    }

    @Test
    void getClientDetail_WhenSeller_Returns200() throws Exception {
        when(firebaseTokenVerifier.verifyTokenAndGetUid(SELLER_TOKEN))
                .thenReturn("sales-clients-seller-uuid");
        when(getClientDetailBySellerUseCase.execute(eq(sellerUserId), eq(10L)))
                .thenReturn(sampleClientDetailDto());

        given()
            .header("Authorization", "Bearer " + SELLER_TOKEN)
            .contentType(ContentType.JSON)
        .when()
            .get("/api/sales/clients/10")
        .then()
            .statusCode(200)
            .body("farmerId", equalTo(10))
            .body("name", equalTo("Juan"))
            .body("parcelas.size()", equalTo(1))
            .body("alertas.size()", equalTo(1))
            .body("orderSummary.totalOrders", equalTo(3));
    }

    @Test
    void getClientDetail_WhenFarmerNotClientOfSeller_Returns404() throws Exception {
        when(firebaseTokenVerifier.verifyTokenAndGetUid(SELLER_TOKEN))
                .thenReturn("sales-clients-seller-uuid");
        when(getClientDetailBySellerUseCase.execute(eq(sellerUserId), anyLong()))
                .thenThrow(new IllegalStateException(
                        "El agricultor con id 99 no es cliente del vendedor actual"));

        given()
            .header("Authorization", "Bearer " + SELLER_TOKEN)
        .when()
            .get("/api/sales/clients/99")
        .then()
            .statusCode(404)
            .body("error", equalTo("El agricultor con id 99 no es cliente del vendedor actual"));
    }
}
