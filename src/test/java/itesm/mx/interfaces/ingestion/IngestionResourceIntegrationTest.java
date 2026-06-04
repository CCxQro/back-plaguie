package itesm.mx.interfaces.ingestion;

import com.google.firebase.auth.FirebaseAuthException;
import io.quarkus.test.InjectMock;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import itesm.mx.application.usecase.ingestion.CheckIngestionUseCase;
import itesm.mx.application.usecase.ingestion.GetIngestionRunsUseCase;
import itesm.mx.domain.models.ingestion.IngestionRun;
import itesm.mx.infrastructure.firebase.FirebaseTokenVerifier;
import itesm.mx.infrastructure.firebase.FirebaseUserManager;
import itesm.mx.infrastructure.persistence.entity.users.UserEntity;
import itesm.mx.infrastructure.persistence.repository.user.UserRepositoryImpl;
import itesm.mx.support.H2TestProfile;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.mockito.Mockito.when;

/**
 * Integration tests for IngestionResource auth/role gates (SCRUM-315, SCRUM-317, SCRUM-319).
 * Uses H2TestProfile + in-memory Kafka channels (no real broker needed).
 */
@QuarkusTest
@TestProfile(H2TestProfile.class)
@QuarkusTestResource(KafkaTestResource.class)
class IngestionResourceIntegrationTest {

    @InjectMock FirebaseTokenVerifier firebaseTokenVerifier;
    @InjectMock FirebaseUserManager firebaseUserManager;
    @InjectMock CheckIngestionUseCase checkIngestionUseCase;
    @InjectMock GetIngestionRunsUseCase getIngestionRunsUseCase;

    @Inject UserRepositoryImpl userRepository;

    private static final String ADMIN_TOKEN  = "admin-token";
    private static final String FARMER_TOKEN = "farmer-token";

    @BeforeEach
    @Transactional
    void setup() throws FirebaseAuthException {
        userRepository.deleteAll();

        UserEntity admin = new UserEntity();
        admin.firebaseUuid = "admin-uid";
        admin.name = "Admin Test";
        admin.email = "admin@test.com";
        admin.roleId = 1; // ADMIN
        admin.isActive = true;
        userRepository.persist(admin);

        UserEntity farmer = new UserEntity();
        farmer.firebaseUuid = "farmer-uid";
        farmer.name = "Farmer Test";
        farmer.email = "farmer@test.com";
        farmer.roleId = 2; // FARMER
        farmer.isActive = true;
        userRepository.persist(farmer);

        when(firebaseTokenVerifier.verifyTokenAndGetUid(ADMIN_TOKEN)).thenReturn("admin-uid");
        when(firebaseTokenVerifier.verifyTokenAndGetUid(FARMER_TOKEN)).thenReturn("farmer-uid");
    }

    // ---- POST /api/ingestion/check ----

    @Test
    void checkEndpoint_requiresAuth() {
        given()
            .post("/api/ingestion/check")
        .then()
            .statusCode(401);
    }

    @Test
    void checkEndpoint_requiresAdminRole() {
        given()
            .header("Authorization", "Bearer " + FARMER_TOKEN)
            .post("/api/ingestion/check")
        .then()
            .statusCode(403);
    }

    @Test
    void checkEndpoint_adminCanTrigger() {
        IngestionRun run = new IngestionRun();
        run.setId(1L);
        run.setStartedAt(LocalDateTime.now());
        run.setStatus("RUNNING");
        run.setFilesFound(2);
        run.setFilesProcessed(2);
        when(checkIngestionUseCase.execute()).thenReturn(run);

        given()
            .header("Authorization", "Bearer " + ADMIN_TOKEN)
            .post("/api/ingestion/check")
        .then()
            .statusCode(200)
            .body("id", notNullValue())
            .body("status", notNullValue());
    }

    // ---- GET /api/ingestion/runs ----

    @Test
    void runsEndpoint_requiresAuth() {
        given()
            .get("/api/ingestion/runs")
        .then()
            .statusCode(401);
    }

    @Test
    void runsEndpoint_requiresAdminRole() {
        given()
            .header("Authorization", "Bearer " + FARMER_TOKEN)
            .get("/api/ingestion/runs")
        .then()
            .statusCode(403);
    }

    @Test
    void runsEndpoint_adminGetsHistory() {
        IngestionRun run = new IngestionRun();
        run.setId(1L);
        run.setStartedAt(LocalDateTime.now());
        run.setStatus("COMPLETED");
        run.setFilesFound(1);
        run.setFilesProcessed(1);
        when(getIngestionRunsUseCase.execute()).thenReturn(List.of(run));

        given()
            .header("Authorization", "Bearer " + ADMIN_TOKEN)
            .get("/api/ingestion/runs")
        .then()
            .statusCode(200);
    }
}
