package itesm.mx.interfaces.rest.admin;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;
import itesm.mx.application.dto.admin.AdminMetricsDto;
import itesm.mx.application.usecase.admin.GetAdminMetricsUseCase;
import itesm.mx.infrastructure.firebase.FirebaseTokenVerifier;
import itesm.mx.infrastructure.firebase.FirebaseUserManager;
import itesm.mx.infrastructure.persistence.entity.users.UserEntity;
import itesm.mx.infrastructure.persistence.repository.user.UserRepositoryImpl;
import itesm.mx.support.H2TestProfile;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.mockito.Mockito.when;

@QuarkusTest
@TestProfile(H2TestProfile.class)
class AdminMetricsResourceTest {

    @InjectMock FirebaseTokenVerifier firebaseTokenVerifier;
    @InjectMock FirebaseUserManager firebaseUserManager;
    @InjectMock GetAdminMetricsUseCase getAdminMetricsUseCase;

    @Inject UserRepositoryImpl userRepository;

    private static final String ADMIN_TOKEN = "admin-token";
    private static final String SELLER_TOKEN = "seller-token";

    @BeforeEach
    @Transactional
    void setup() {
        userRepository.deleteAll();

        UserEntity admin = new UserEntity();
        admin.firebaseUuid = "admin-uuid";
        admin.name = "Admin";
        admin.email = "admin@test.mx";
        admin.roleId = 1; // Admin
        admin.isActive = true;
        userRepository.persist(admin);

        UserEntity seller = new UserEntity();
        seller.firebaseUuid = "seller-uuid";
        seller.name = "Seller";
        seller.email = "seller@test.mx";
        seller.roleId = 3;
        seller.isActive = true;
        userRepository.persist(seller);
    }

    @Test
    void getMetrics_Unauthorized_Returns401() {
        given()
            .when().get("/api/admin/metrics")
            .then()
            .statusCode(401);
    }

    @Test
    void getMetrics_WhenNonAdmin_Returns403() throws Exception {
        when(firebaseTokenVerifier.verifyTokenAndGetUid(SELLER_TOKEN))
                .thenReturn("seller-uuid");

        given()
            .header("Authorization", "Bearer " + SELLER_TOKEN)
            .when().get("/api/admin/metrics")
            .then()
            .statusCode(403);
    }

    @Test
    void getMetrics_Authorized_Returns200() throws Exception {
        when(firebaseTokenVerifier.verifyTokenAndGetUid(ADMIN_TOKEN))
                .thenReturn("admin-uuid");
        when(getAdminMetricsUseCase.execute()).thenReturn(new AdminMetricsDto(100, 50, 200, 20, 300, 30));

        given()
            .header("Authorization", "Bearer " + ADMIN_TOKEN)
            .when().get("/api/admin/metrics")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON);
    }
}
