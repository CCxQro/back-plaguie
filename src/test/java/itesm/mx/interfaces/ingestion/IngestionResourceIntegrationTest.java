package itesm.mx.interfaces.ingestion;

import com.google.firebase.auth.FirebaseAuthException;
import io.quarkus.test.InjectMock;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import itesm.mx.application.usecase.ingestion.GetIngestionRunsUseCase;
import itesm.mx.application.usecase.ingestion.UploadIngestionUseCase;
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

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.mockito.Mockito.when;

/**
 * Integration tests for IngestionResource auth/role gates and upload flow (EP-05 upload refactor).
 * Uses H2TestProfile + in-memory Kafka channels (no real broker needed).
 */
@QuarkusTest
@TestProfile(H2TestProfile.class)
@QuarkusTestResource(KafkaTestResource.class)
class IngestionResourceIntegrationTest {

    @InjectMock FirebaseTokenVerifier firebaseTokenVerifier;
    @InjectMock FirebaseUserManager firebaseUserManager;
    @InjectMock UploadIngestionUseCase uploadIngestionUseCase;
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

    // ---- POST /api/ingestion/upload — auth/role gates ----

    @Test
    void uploadEndpoint_requiresAuth() {
        given()
            .contentType("multipart/form-data")
            .multiPart("file", "test.csv", "estado,municipio\n".getBytes(), "text/csv")
            .post("/api/ingestion/upload")
        .then()
            .statusCode(401);
    }

    @Test
    void uploadEndpoint_requiresAdminRole() {
        given()
            .header("Authorization", "Bearer " + FARMER_TOKEN)
            .contentType("multipart/form-data")
            .multiPart("file", "test.csv", "estado,municipio\n".getBytes(), "text/csv")
            .post("/api/ingestion/upload")
        .then()
            .statusCode(403);
    }

    @Test
    void uploadEndpoint_rejectsNonCsvExtension() {
        given()
            .header("Authorization", "Bearer " + ADMIN_TOKEN)
            .contentType("multipart/form-data")
            .multiPart("file", "data.xlsx", "some bytes".getBytes(), "application/octet-stream")
            .post("/api/ingestion/upload")
        .then()
            .statusCode(400);
    }

    @Test
    void uploadEndpoint_rejectsEmptyFile() {
        given()
            .header("Authorization", "Bearer " + ADMIN_TOKEN)
            .contentType("multipart/form-data")
            .multiPart("file", "data.csv", new byte[0], "text/csv")
            .post("/api/ingestion/upload")
        .then()
            .statusCode(400);
    }

    @Test
    void uploadEndpoint_adminCanUploadCsv() throws Exception {
        IngestionRun run = new IngestionRun();
        run.setId(1L);
        run.setStartedAt(LocalDateTime.now());
        run.setStatus("RUNNING");
        run.setFilesFound(1);
        run.setFilesProcessed(1);

        // Mock: any call to uploadIngestionUseCase.execute returns our run
        when(uploadIngestionUseCase.execute(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any(byte[].class),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(run);

        byte[] csvBytes = loadFixture("fixtures/senasica_sample.csv");

        given()
            .header("Authorization", "Bearer " + ADMIN_TOKEN)
            .contentType("multipart/form-data")
            .multiPart("file", "senasica_sample.csv", csvBytes, "text/csv")
            .post("/api/ingestion/upload")
        .then()
            .statusCode(200)
            .body("id", notNullValue())
            .body("status", equalTo("RUNNING"));
    }

    @Test
    void uploadEndpoint_returnsSkippedWhenAlreadyIngested() throws Exception {
        IngestionRun skipped = new IngestionRun();
        skipped.setStatus("SKIPPED");
        skipped.setFilesFound(0);
        skipped.setFilesProcessed(0);
        skipped.setStartedAt(LocalDateTime.now());
        skipped.setFinishedAt(LocalDateTime.now());

        when(uploadIngestionUseCase.execute(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any(byte[].class),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(skipped);

        byte[] csvBytes = loadFixture("fixtures/senasica_sample.csv");

        given()
            .header("Authorization", "Bearer " + ADMIN_TOKEN)
            .contentType("multipart/form-data")
            .multiPart("file", "senasica_sample.csv", csvBytes, "text/csv")
            .post("/api/ingestion/upload")
        .then()
            .statusCode(200)
            .body("status", equalTo("skipped/already ingested"));
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

    // ---- Helpers ----

    private byte[] loadFixture(String path) throws Exception {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("Fixture not found: " + path);
            return is.readAllBytes();
        }
    }
}
