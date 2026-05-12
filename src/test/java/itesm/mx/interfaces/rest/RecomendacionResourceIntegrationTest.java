package itesm.mx.interfaces.rest;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;
import itesm.mx.application.dto.CreateRecomendacionDto;
import itesm.mx.application.dto.GetRecomendacionResponseDto;
import itesm.mx.application.usecase.recomendacion.CreateRecomendacionUseCase;
import itesm.mx.application.usecase.recomendacion.GetRecomendacionByIdUseCase;
import itesm.mx.application.usecase.recomendacion.GetAllRecomendacionesUseCase;
import itesm.mx.application.usecase.recomendacion.ValidateRecomendacionUseCase;
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

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@QuarkusTest
@TestProfile(H2TestProfile.class)
class RecomendacionResourceIntegrationTest {

    @InjectMock
    FirebaseTokenVerifier firebaseTokenVerifier;

    @InjectMock
    FirebaseUserManager firebaseUserManager;

    @InjectMock
    GetAllRecomendacionesUseCase getAllRecomendacionesUseCase;

    @InjectMock
    GetRecomendacionByIdUseCase getRecomendacionByIdUseCase;

    @InjectMock
    CreateRecomendacionUseCase createRecomendacionUseCase;

    @InjectMock
    ValidateRecomendacionUseCase validateRecomendacionUseCase;

    @Inject
    UserRepositoryImpl userRepository;

    @BeforeEach
    @Transactional
    void setup() {
        userRepository.deleteAll();

        UserEntity admin = new UserEntity();
        admin.firebaseUuid = "recom-admin-uuid";
        admin.name = "Recom Admin";
        admin.email = "admin@recom.test";
        admin.roleId = 1;
        userRepository.persist(admin);

        UserEntity farmer = new UserEntity();
        farmer.firebaseUuid = "recom-farmer-uuid";
        farmer.name = "Recom Farmer";
        farmer.email = "farmer@recom.test";
        farmer.roleId = 2;
        userRepository.persist(farmer);
    }

    @Test
    void getAll_WhenNoAuth_Returns401() {
        given()
        .when()
            .get("/api/recomendaciones")
        .then()
            .statusCode(401);
    }

    @Test
    void getAll_WhenAuthenticated_Returns200() throws Exception {
        String token = "recom-admin-token";
        when(firebaseTokenVerifier.verifyTokenAndGetUid(token)).thenReturn("recom-admin-uuid");
        when(getAllRecomendacionesUseCase.execute()).thenReturn(List.of(
                buildResponseDto(1L, "Recom 1", "Pulgón")
        ));

        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .get("/api/recomendaciones")
        .then()
            .statusCode(200)
            .body("[0].recomendacionId", equalTo(1))
            .body("[0].titulo", equalTo("Recom 1"))
            .body("[0].tipoPlaga", equalTo("Pulgón"));
    }

    @Test
    void getById_WhenAuthenticated_Returns200() throws Exception {
        String token = "recom-admin-token";
        when(firebaseTokenVerifier.verifyTokenAndGetUid(token)).thenReturn("recom-admin-uuid");
        when(getRecomendacionByIdUseCase.execute(1L)).thenReturn(
                buildResponseDto(1L, "Recom 1", "Pulgón")
        );

        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .get("/api/recomendaciones/1")
        .then()
            .statusCode(200)
            .body("recomendacionId", equalTo(1))
            .body("titulo", equalTo("Recom 1"));
    }

    @Test
    void create_WhenAdmin_Returns201() throws Exception {
        String token = "recom-admin-token";
        when(firebaseTokenVerifier.verifyTokenAndGetUid(token)).thenReturn("recom-admin-uuid");
        when(createRecomendacionUseCase.execute(any(), anyLong())).thenReturn(
                buildResponseDto(10L, "Nueva Recom", "Langosta")
        );

        CreateRecomendacionDto request = new CreateRecomendacionDto();
        request.titulo = "Nueva Recom";
        request.tipoPlaga = "Langosta";
        request.productosRecomendados = "Producto A";

        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/recomendaciones")
        .then()
            .statusCode(201)
            .body("recomendacionId", equalTo(10))
            .body("titulo", equalTo("Nueva Recom"));
    }

    @Test
    void create_WhenNonAdmin_Returns403() throws Exception {
        String token = "recom-farmer-token";
        when(firebaseTokenVerifier.verifyTokenAndGetUid(token)).thenReturn("recom-farmer-uuid");

        CreateRecomendacionDto request = new CreateRecomendacionDto();
        request.titulo = "Nueva Recom";
        request.tipoPlaga = "Test";
        request.productosRecomendados = "Prod";

        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/recomendaciones")
        .then()
            .statusCode(403)
            .body("error", equalTo("Solo un administrador puede crear recomendaciones"));
    }

    @Test
    void validate_WhenAdmin_Returns200() throws Exception {
        String token = "recom-admin-token";
        when(firebaseTokenVerifier.verifyTokenAndGetUid(token)).thenReturn("recom-admin-uuid");
        when(validateRecomendacionUseCase.execute(anyLong(), anyLong(), anyLong())).thenReturn(
                new GetRecomendacionResponseDto(1L, "Recom 1", "Desc", "Langosta",
                        "Prod A", 11L, "2026-01-10T08:30:00",
                        1L, "Accepted", 1L, "2026-01-15T10:00:00")
        );

        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body("{\"statusId\": 1}")
        .when()
            .patch("/api/recomendaciones/1/validate")
        .then()
            .statusCode(200)
            .body("statusId", equalTo(1))
            .body("statusName", equalTo("Accepted"));
    }

    @Test
    void validate_WhenNonAdmin_Returns403() throws Exception {
        String token = "recom-farmer-token";
        when(firebaseTokenVerifier.verifyTokenAndGetUid(token)).thenReturn("recom-farmer-uuid");

        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body("{\"statusId\": 1}")
        .when()
            .patch("/api/recomendaciones/1/validate")
        .then()
            .statusCode(403)
            .body("error", equalTo("Solo un administrador puede validar recomendaciones"));
    }

    @Test
    void validate_WhenNotFound_Returns404() throws Exception {
        String token = "recom-admin-token";
        when(firebaseTokenVerifier.verifyTokenAndGetUid(token)).thenReturn("recom-admin-uuid");
        when(validateRecomendacionUseCase.execute(anyLong(), anyLong(), anyLong()))
                .thenThrow(new IllegalStateException("Recomendación no encontrada con id: 999"));

        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body("{\"statusId\": 1}")
        .when()
            .patch("/api/recomendaciones/999/validate")
        .then()
            .statusCode(404)
            .body("error", equalTo("Recomendación no encontrada con id: 999"));
    }

    // --- Helper ---

    private GetRecomendacionResponseDto buildResponseDto(Long id, String titulo, String tipoPlaga) {
        return new GetRecomendacionResponseDto(
                id, titulo, "Descripción de prueba", tipoPlaga,
                "Producto A", 11L, "2026-01-10T08:30:00",
                2L, "Revision", null, null
        );
    }
}
