package itesm.mx.interfaces.rest;

import com.google.firebase.auth.FirebaseAuthException;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;
import itesm.mx.application.dto.CreateVigilanciaFitosanitariaDto;
import itesm.mx.application.dto.GetVigilanciaFitosanitariaResponseDto;
import itesm.mx.application.dto.UpdateVigilanciaFitosanitariaDto;
import itesm.mx.application.dto.ValidateVigilanciaDto;
import itesm.mx.application.usecase.vigilancia.CreateVigilanciaFitosanitariaUseCase;
import itesm.mx.application.usecase.vigilancia.DeleteVigilanciaFitosanitariaUseCase;
import itesm.mx.application.usecase.vigilancia.GetAllVigilanciasFitosanitariasUseCase;
import itesm.mx.application.usecase.vigilancia.GetVigilanciaFitosanitariaByIdUseCase;
import itesm.mx.application.usecase.vigilancia.UpdateVigilanciaFitosanitariaUseCase;
import itesm.mx.application.usecase.vigilancia.ValidateVigilanciaFitosanitariaUseCase;
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
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@QuarkusTest
@TestProfile(H2TestProfile.class)
class VigilanciaFitosanitariaResourceIntegrationTest {

    @InjectMock
    FirebaseTokenVerifier firebaseTokenVerifier;

    @InjectMock
    FirebaseUserManager firebaseUserManager;

    @InjectMock
    GetAllVigilanciasFitosanitariasUseCase getAllVigilanciasFitosanitariasUseCase;

    @InjectMock
    GetVigilanciaFitosanitariaByIdUseCase getVigilanciaFitosanitariaByIdUseCase;

    @InjectMock
    CreateVigilanciaFitosanitariaUseCase createVigilanciaFitosanitariaUseCase;

    @InjectMock
    UpdateVigilanciaFitosanitariaUseCase updateVigilanciaFitosanitariaUseCase;

    @InjectMock
    DeleteVigilanciaFitosanitariaUseCase deleteVigilanciaFitosanitariaUseCase;

    @InjectMock
    ValidateVigilanciaFitosanitariaUseCase validateVigilanciaFitosanitariaUseCase;

    @Inject
    UserRepositoryImpl userRepository;

    @BeforeEach
    @Transactional
    void setup() {
        userRepository.deleteAll();

        UserEntity admin = new UserEntity();
        admin.firebaseUuid = "vigilancia-admin-uuid";
        admin.name = "Vigilancia Admin";
        admin.email = "admin@vigilancia.test";
        admin.roleId = 1;
        userRepository.persist(admin);

        UserEntity seller = new UserEntity();
        seller.firebaseUuid = "vigilancia-seller-uuid";
        seller.name = "Vigilancia Seller";
        seller.email = "seller@vigilancia.test";
        seller.roleId = 3;
        userRepository.persist(seller);
    }

    @Test
    void getAll_WhenNoAuthHeader_Returns401() {
        given()
        .when()
            .get("/api/vigilancias-fitosanitarias")
        .then()
            .statusCode(401);
    }

    @Test
    void getAll_WhenAuthenticated_Returns200AndBody() throws Exception {
        String token = "vigilancia-admin-token";
        when(firebaseTokenVerifier.verifyTokenAndGetUid(token)).thenReturn("vigilancia-admin-uuid");
        when(getAllVigilanciasFitosanitariasUseCase.execute()).thenReturn(List.of(
                new GetVigilanciaFitosanitariaResponseDto(
                        1L,
                        2L,
                        "monitoreo",
                        3L,
                        "cid",
                        new BigDecimal("20.67000000"),
                        new BigDecimal("-103.35000000"),
                        4L,
                        5L,
                        "plaga",
                        6L,
                        "hospedante",
                        7L,
                        "variedad",
                        8L,
                        "especie",
                        new BigDecimal("12.50"),
                        2L,
                        "Revision",
                        null,
                        null
                )
        ));

        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .get("/api/vigilancias-fitosanitarias")
        .then()
            .statusCode(200)
            .body("[0].vigilanciaFitosanitariaId", equalTo(1))
            .body("[0].systemMonitoringName", equalTo("monitoreo"))
            .body("[0].plagueName", equalTo("plaga"));
    }

    @Test
    void getById_WhenAuthenticated_Returns200AndBody() throws Exception {
        String token = "vigilancia-admin-token";
        when(firebaseTokenVerifier.verifyTokenAndGetUid(token)).thenReturn("vigilancia-admin-uuid");
        when(getVigilanciaFitosanitariaByIdUseCase.execute(10L)).thenReturn(
                new GetVigilanciaFitosanitariaResponseDto(
                        10L,
                        2L,
                        "monitoreo",
                        3L,
                        "cid",
                        new BigDecimal("20.67000000"),
                        new BigDecimal("-103.35000000"),
                        4L,
                        5L,
                        "plaga",
                        6L,
                        "hospedante",
                        7L,
                        "variedad",
                        8L,
                        "especie",
                        new BigDecimal("12.50"),
                        2L,
                        "Revision",
                        null,
                        null
                )
        );

        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .get("/api/vigilancias-fitosanitarias/10")
        .then()
            .statusCode(200)
            .body("vigilanciaFitosanitariaId", equalTo(10));
    }

    @Test
    void create_WhenBodyIsMissing_Returns400() throws Exception {
        String token = "vigilancia-admin-token";
        when(firebaseTokenVerifier.verifyTokenAndGetUid(token)).thenReturn("vigilancia-admin-uuid");

        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
        .when()
            .post("/api/vigilancias-fitosanitarias")
        .then()
            .statusCode(400)
            .body("error", equalTo("El cuerpo de la solicitud es requerido"));
    }

    @Test
    void create_WhenAuthenticatedAdminAndValid_Returns201() throws Exception {
        String token = "vigilancia-admin-token";
        when(firebaseTokenVerifier.verifyTokenAndGetUid(token)).thenReturn("vigilancia-admin-uuid");

        CreateVigilanciaFitosanitariaDto request = new CreateVigilanciaFitosanitariaDto();
        request.systemMonitoringId = 2L;
        request.identificationKeyId = 3L;
        request.latitude = new BigDecimal("20.67000000");
        request.longitude = new BigDecimal("-103.35000000");
        request.locationId = 4L;
        request.plagueId = 5L;
        request.hostId = 6L;
        request.varietyId = 7L;
        request.speciesId = 8L;
        request.ahosp = new BigDecimal("12.50");

        when(createVigilanciaFitosanitariaUseCase.execute(any())).thenReturn(
                new GetVigilanciaFitosanitariaResponseDto(
                        15L,
                        2L,
                        "monitoreo",
                        3L,
                        "cid",
                        new BigDecimal("20.67000000"),
                        new BigDecimal("-103.35000000"),
                        4L,
                        5L,
                        "plaga",
                        6L,
                        "hospedante",
                        7L,
                        "variedad",
                        8L,
                        "especie",
                        new BigDecimal("12.50"),
                        2L,
                        "Revision",
                        null,
                        null
                )
        );

        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/vigilancias-fitosanitarias")
        .then()
            .statusCode(201)
            .body("vigilanciaFitosanitariaId", equalTo(15));
    }

    @Test
    void create_WhenNonAdmin_Returns403() throws Exception {
        String token = "vigilancia-seller-token";
        when(firebaseTokenVerifier.verifyTokenAndGetUid(token)).thenReturn("vigilancia-seller-uuid");

        CreateVigilanciaFitosanitariaDto request = new CreateVigilanciaFitosanitariaDto();
        request.systemMonitoringId = 2L;
        request.identificationKeyId = 3L;
        request.latitude = new BigDecimal("20.67000000");
        request.longitude = new BigDecimal("-103.35000000");
        request.locationId = 4L;
        request.plagueId = 5L;
        request.hostId = 6L;
        request.varietyId = 7L;
        request.speciesId = 8L;

        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/vigilancias-fitosanitarias")
        .then()
            .statusCode(403)
            .body("error", equalTo("Solo un administrador puede crear vigilancias fitosanitarias"));
    }

    @Test
    void update_WhenAuthenticatedAdminAndValid_Returns200() throws Exception {
        String token = "vigilancia-admin-token";
        when(firebaseTokenVerifier.verifyTokenAndGetUid(token)).thenReturn("vigilancia-admin-uuid");

        UpdateVigilanciaFitosanitariaDto request = new UpdateVigilanciaFitosanitariaDto();
        request.ahosp = new BigDecimal("18.75");

        when(updateVigilanciaFitosanitariaUseCase.execute(anyLong(), any())).thenReturn(
                new GetVigilanciaFitosanitariaResponseDto(
                        15L,
                        2L,
                        "monitoreo",
                        3L,
                        "cid",
                        new BigDecimal("20.67000000"),
                        new BigDecimal("-103.35000000"),
                        4L,
                        5L,
                        "plaga",
                        6L,
                        "hospedante",
                        7L,
                        "variedad",
                        8L,
                        "especie",
                        new BigDecimal("18.75"),
                        2L,
                        "Revision",
                        null,
                        null
                )
        );

        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .put("/api/vigilancias-fitosanitarias/15")
        .then()
            .statusCode(200)
            .body("ahosp", equalTo(18.75f));
    }

    @Test
    void delete_WhenAuthenticatedAdmin_Returns204() throws Exception {
        String token = "vigilancia-admin-token";
        when(firebaseTokenVerifier.verifyTokenAndGetUid(token)).thenReturn("vigilancia-admin-uuid");
        doNothing().when(deleteVigilanciaFitosanitariaUseCase).execute(15L);

        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .delete("/api/vigilancias-fitosanitarias/15")
        .then()
            .statusCode(204);
    }

    @Test
    void create_WhenTokenIsInvalid_Returns401() throws Exception {
        String invalidToken = "vigilancia-token-invalido";
        FirebaseAuthException mockAuthException = org.mockito.Mockito.mock(FirebaseAuthException.class);
        when(mockAuthException.getMessage()).thenReturn("Token invalido");
        when(firebaseTokenVerifier.verifyTokenAndGetUid(invalidToken)).thenThrow(mockAuthException);

        CreateVigilanciaFitosanitariaDto request = new CreateVigilanciaFitosanitariaDto();
        request.systemMonitoringId = 2L;
        request.identificationKeyId = 3L;
        request.latitude = new BigDecimal("20.67000000");
        request.longitude = new BigDecimal("-103.35000000");
        request.locationId = 4L;
        request.plagueId = 5L;
        request.hostId = 6L;
        request.varietyId = 7L;
        request.speciesId = 8L;

        given()
            .header("Authorization", "Bearer " + invalidToken)
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/vigilancias-fitosanitarias")
        .then()
            .statusCode(401);
    }

    // --- Validate (PATCH) tests ---

    @Test
    void validate_WhenNoAuth_Returns401() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"statusId\": 1}")
        .when()
            .patch("/api/vigilancias-fitosanitarias/1/validate")
        .then()
            .statusCode(401);
    }

    @Test
    void validate_WhenNonAdmin_Returns403() throws Exception {
        String token = "vigilancia-seller-token";
        when(firebaseTokenVerifier.verifyTokenAndGetUid(token)).thenReturn("vigilancia-seller-uuid");

        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body("{\"statusId\": 1}")
        .when()
            .patch("/api/vigilancias-fitosanitarias/1/validate")
        .then()
            .statusCode(403)
            .body("error", equalTo("Solo un administrador puede validar vigilancias fitosanitarias"));
    }

    @Test
    void validate_WhenAdminAndValid_Returns200() throws Exception {
        String token = "vigilancia-admin-token";
        when(firebaseTokenVerifier.verifyTokenAndGetUid(token)).thenReturn("vigilancia-admin-uuid");

        when(validateVigilanciaFitosanitariaUseCase.execute(anyLong(), anyLong(), anyLong())).thenReturn(
                new GetVigilanciaFitosanitariaResponseDto(
                        1L, 2L, "monitoreo", 3L, "cid",
                        new BigDecimal("20.67000000"), new BigDecimal("-103.35000000"),
                        4L, 5L, "plaga", 6L, "hospedante",
                        7L, "variedad", 8L, "especie",
                        new BigDecimal("12.50"),
                        1L, "Accepted", 1L, "2026-01-15T10:30:00"
                )
        );

        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body("{\"statusId\": 1}")
        .when()
            .patch("/api/vigilancias-fitosanitarias/1/validate")
        .then()
            .statusCode(200)
            .body("statusId", equalTo(1))
            .body("statusName", equalTo("Accepted"))
            .body("validatedByUserId", equalTo(1));
    }

    @Test
    void validate_WhenInvalidStatusId_Returns400() throws Exception {
        String token = "vigilancia-admin-token";
        when(firebaseTokenVerifier.verifyTokenAndGetUid(token)).thenReturn("vigilancia-admin-uuid");
        when(validateVigilanciaFitosanitariaUseCase.execute(anyLong(), anyLong(), anyLong()))
                .thenThrow(new IllegalArgumentException("El statusId debe ser 1 (Accepted) o 3 (Rejected)"));

        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body("{\"statusId\": 2}")
        .when()
            .patch("/api/vigilancias-fitosanitarias/1/validate")
        .then()
            .statusCode(400)
            .body("error", equalTo("El statusId debe ser 1 (Accepted) o 3 (Rejected)"));
    }

    @Test
    void validate_WhenNotFound_Returns404() throws Exception {
        String token = "vigilancia-admin-token";
        when(firebaseTokenVerifier.verifyTokenAndGetUid(token)).thenReturn("vigilancia-admin-uuid");
        when(validateVigilanciaFitosanitariaUseCase.execute(anyLong(), anyLong(), anyLong()))
                .thenThrow(new IllegalStateException("Vigilancia fitosanitaria no encontrada con id: 999"));

        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body("{\"statusId\": 1}")
        .when()
            .patch("/api/vigilancias-fitosanitarias/999/validate")
        .then()
            .statusCode(404)
            .body("error", equalTo("Vigilancia fitosanitaria no encontrada con id: 999"));
    }
}