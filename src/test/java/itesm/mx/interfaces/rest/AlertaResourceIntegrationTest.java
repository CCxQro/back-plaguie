package itesm.mx.interfaces.rest;

import com.google.firebase.auth.FirebaseAuthException;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;
import itesm.mx.application.dto.CreateAlertaDto;
import itesm.mx.application.dto.GetAlertaResponseDto;
import itesm.mx.application.usecase.alerta.CreateAlertaUseCase;
import itesm.mx.application.usecase.alerta.GetAlertaByIdUseCase;
import itesm.mx.application.usecase.alerta.GetAllAlertasUseCase;
import itesm.mx.application.usecase.alerta.ValidateAlertaUseCase;
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
import static org.mockito.Mockito.when;

@QuarkusTest
@TestProfile(H2TestProfile.class)
class AlertaResourceIntegrationTest {

    @InjectMock
    FirebaseTokenVerifier firebaseTokenVerifier;

    @InjectMock
    FirebaseUserManager firebaseUserManager;

    @InjectMock
    GetAllAlertasUseCase getAllAlertasUseCase;

    @InjectMock
    GetAlertaByIdUseCase getAlertaByIdUseCase;

    @InjectMock
    CreateAlertaUseCase createAlertaUseCase;

    @InjectMock
    ValidateAlertaUseCase validateAlertaUseCase;

    @Inject
    UserRepositoryImpl userRepository;

    @BeforeEach
    @Transactional
    void setup() {
        userRepository.deleteAll();

        UserEntity admin = new UserEntity();
        admin.firebaseUuid = "alerta-admin-uuid";
        admin.name = "Alerta Admin";
        admin.email = "admin@alerta.test";
        admin.roleId = 1;
        userRepository.persist(admin);

        UserEntity farmer = new UserEntity();
        farmer.firebaseUuid = "alerta-farmer-uuid";
        farmer.name = "Alerta Farmer";
        farmer.email = "farmer@alerta.test";
        farmer.roleId = 2;
        userRepository.persist(farmer);
    }

    @Test
    void getAll_WhenNoAuth_Returns401() {
        given()
        .when()
            .get("/api/alertas")
        .then()
            .statusCode(401);
    }

    @Test
    void getAll_WhenAuthenticated_Returns200() throws Exception {
        String token = "alerta-admin-token";
        when(firebaseTokenVerifier.verifyTokenAndGetUid(token)).thenReturn("alerta-admin-uuid");
        when(getAllAlertasUseCase.execute()).thenReturn(List.of(
                buildResponseDto(1L, "Alerta 1", "critico")
        ));

        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .get("/api/alertas")
        .then()
            .statusCode(200)
            .body("[0].alertaId", equalTo(1))
            .body("[0].titulo", equalTo("Alerta 1"))
            .body("[0].severidad", equalTo("critico"));
    }

    @Test
    void getById_WhenAuthenticated_Returns200() throws Exception {
        String token = "alerta-admin-token";
        when(firebaseTokenVerifier.verifyTokenAndGetUid(token)).thenReturn("alerta-admin-uuid");
        when(getAlertaByIdUseCase.execute(1L)).thenReturn(
                buildResponseDto(1L, "Alerta 1", "critico")
        );

        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .get("/api/alertas/1")
        .then()
            .statusCode(200)
            .body("alertaId", equalTo(1))
            .body("titulo", equalTo("Alerta 1"));
    }

    @Test
    void create_WhenAdmin_Returns201() throws Exception {
        String token = "alerta-admin-token";
        when(firebaseTokenVerifier.verifyTokenAndGetUid(token)).thenReturn("alerta-admin-uuid");
        when(createAlertaUseCase.execute(any(), anyLong())).thenReturn(
                buildResponseDto(10L, "Nueva Alerta", "advertencia")
        );

        CreateAlertaDto request = new CreateAlertaDto();
        request.titulo = "Nueva Alerta";
        request.ubicacionId = 1L;
        request.tipoPlaga = "Pulgón";
        request.severidad = "advertencia";
        request.hectareas = new BigDecimal("15.00");

        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/alertas")
        .then()
            .statusCode(201)
            .body("alertaId", equalTo(10))
            .body("titulo", equalTo("Nueva Alerta"));
    }

    @Test
    void create_WhenNonAdmin_Returns403() throws Exception {
        String token = "alerta-farmer-token";
        when(firebaseTokenVerifier.verifyTokenAndGetUid(token)).thenReturn("alerta-farmer-uuid");

        CreateAlertaDto request = new CreateAlertaDto();
        request.titulo = "Alerta";
        request.ubicacionId = 1L;
        request.tipoPlaga = "Test";
        request.severidad = "critico";

        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/alertas")
        .then()
            .statusCode(403)
            .body("error", equalTo("Solo un administrador puede crear alertas"));
    }

    @Test
    void validate_WhenAdmin_Returns200() throws Exception {
        String token = "alerta-admin-token";
        when(firebaseTokenVerifier.verifyTokenAndGetUid(token)).thenReturn("alerta-admin-uuid");
        when(validateAlertaUseCase.execute(anyLong(), anyLong(), anyLong())).thenReturn(
                new GetAlertaResponseDto(1L, "Alerta 1", "Desc", 1L, "Langosta",
                        new BigDecimal("50.00"), "critico", 6L, "2026-01-10T08:30:00",
                        1L, "Accepted", 1L, "2026-01-15T10:00:00")
        );

        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body("{\"statusId\": 1}")
        .when()
            .patch("/api/alertas/1/validate")
        .then()
            .statusCode(200)
            .body("statusId", equalTo(1))
            .body("statusName", equalTo("Accepted"));
    }

    @Test
    void validate_WhenNonAdmin_Returns403() throws Exception {
        String token = "alerta-farmer-token";
        when(firebaseTokenVerifier.verifyTokenAndGetUid(token)).thenReturn("alerta-farmer-uuid");

        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body("{\"statusId\": 1}")
        .when()
            .patch("/api/alertas/1/validate")
        .then()
            .statusCode(403)
            .body("error", equalTo("Solo un administrador puede validar alertas"));
    }

    @Test
    void validate_WhenNotFound_Returns404() throws Exception {
        String token = "alerta-admin-token";
        when(firebaseTokenVerifier.verifyTokenAndGetUid(token)).thenReturn("alerta-admin-uuid");
        when(validateAlertaUseCase.execute(anyLong(), anyLong(), anyLong()))
                .thenThrow(new IllegalStateException("Alerta no encontrada con id: 999"));

        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body("{\"statusId\": 1}")
        .when()
            .patch("/api/alertas/999/validate")
        .then()
            .statusCode(404)
            .body("error", equalTo("Alerta no encontrada con id: 999"));
    }

    // --- Helper ---

    private GetAlertaResponseDto buildResponseDto(Long id, String titulo, String severidad) {
        return new GetAlertaResponseDto(
                id, titulo, "Descripción de prueba", 1L, "Langosta",
                new BigDecimal("50.00"), severidad, 6L, "2026-01-10T08:30:00",
                2L, "Revision", null, null
        );
    }
}
