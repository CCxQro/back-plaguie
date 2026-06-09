package itesm.mx.interfaces.rest.sales;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;
import itesm.mx.application.dto.sales.SalesSummaryDto;
import itesm.mx.application.dto.sales.InventoryAlertDto;
import itesm.mx.application.usecase.sales.GetSalesSummaryUseCase;
import itesm.mx.application.usecase.sales.GetInventoryAlertsUseCase;
import itesm.mx.infrastructure.firebase.FirebaseTokenVerifier;
import itesm.mx.infrastructure.persistence.entity.users.UserEntity;
import itesm.mx.infrastructure.persistence.repository.user.UserRepositoryImpl;
import itesm.mx.support.H2TestProfile;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@QuarkusTest
@TestProfile(H2TestProfile.class)
class SalesDashboardResourceTest {

    @InjectMock FirebaseTokenVerifier firebaseTokenVerifier;
    @InjectMock GetSalesSummaryUseCase getSalesSummaryUseCase;
    @InjectMock GetInventoryAlertsUseCase getInventoryAlertsUseCase;

    @Inject UserRepositoryImpl userRepository;

    private static final String SELLER_TOKEN = "seller-token";

    @BeforeEach
    @Transactional
    void setup() {
        userRepository.deleteAll();

        UserEntity seller = new UserEntity();
        seller.firebaseUuid = "seller-uuid";
        seller.name = "Seller";
        seller.email = "seller@test.mx";
        seller.roleId = 3; // SELLER
        seller.isActive = true;
        userRepository.persist(seller);
    }

    @Test
    void getSalesSummary_Authorized_Returns200() throws Exception {
        when(firebaseTokenVerifier.verifyTokenAndGetUid(SELLER_TOKEN))
                .thenReturn("seller-uuid");
        when(getSalesSummaryUseCase.execute(any(), any(), any()))
                .thenReturn(new SalesSummaryDto(new BigDecimal("1500.00"), 10, 50, 100));

        given()
            .header("Authorization", "Bearer " + SELLER_TOKEN)
        .when()
            .get("/api/sales/summary")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON);
    }

    @Test
    void getInventoryAlerts_Authorized_Returns200() throws Exception {
        when(firebaseTokenVerifier.verifyTokenAndGetUid(SELLER_TOKEN))
                .thenReturn("seller-uuid");
        when(getInventoryAlertsUseCase.execute(any(), anyInt()))
                .thenReturn(List.of(new InventoryAlertDto(1L, "Product A", "SKU1", 3)));

        given()
            .header("Authorization", "Bearer " + SELLER_TOKEN)
        .when()
            .get("/api/sales/inventory-alerts?threshold=5")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON);
    }
}
