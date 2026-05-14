package itesm.mx.interfaces.rest;

import com.google.firebase.auth.FirebaseAuthException;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;
import itesm.mx.application.dto.AplicacionInsumoResponseDto;
import itesm.mx.application.dto.RegisterAplicacionInsumoDto;
import itesm.mx.application.usecase.insumo.GetAplicacionesByFarmerUseCase;
import itesm.mx.application.usecase.insumo.RegisterAplicacionInsumoUseCase;
import itesm.mx.infrastructure.firebase.FirebaseTokenVerifier;
import itesm.mx.infrastructure.firebase.FirebaseUserManager;
import itesm.mx.infrastructure.persistence.entity.users.FarmerEntity;
import itesm.mx.infrastructure.persistence.entity.users.UserEntity;
import itesm.mx.infrastructure.persistence.repository.user.FarmerRepositoryImpl;
import itesm.mx.infrastructure.persistence.repository.user.UserRepositoryImpl;
import itesm.mx.support.H2TestProfile;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@QuarkusTest
@TestProfile(H2TestProfile.class)
class AplicacionInsumoResourceIntegrationTest {

    @InjectMock
    FirebaseTokenVerifier firebaseTokenVerifier;

    @InjectMock
    FirebaseUserManager firebaseUserManager;

    @InjectMock
    RegisterAplicacionInsumoUseCase registerAplicacionInsumoUseCase;

    @InjectMock
    GetAplicacionesByFarmerUseCase getAplicacionesByFarmerUseCase;

    @Inject
    UserRepositoryImpl userRepository;

    @Inject
    FarmerRepositoryImpl farmerRepository;

    @BeforeEach
    @Transactional
    void setup() {
        farmerRepository.deleteAll();
        userRepository.deleteAll();

        UserEntity farmerUser = new UserEntity();
        farmerUser.firebaseUuid = "insumo-farmer-uuid";
        farmerUser.name = "Insumo Farmer";
        farmerUser.email = "farmer@insumo.test";
        farmerUser.roleId = 2;
        userRepository.persist(farmerUser);

        FarmerEntity farmerEntity = new FarmerEntity();
        farmerEntity.userId = farmerUser.userId;
        farmerEntity.locationId = 1L;
        farmerEntity.isActive = true;
        farmerRepository.persist(farmerEntity);

        UserEntity adminUser = new UserEntity();
        adminUser.firebaseUuid = "insumo-admin-uuid";
        adminUser.name = "Insumo Admin";
        adminUser.email = "admin@insumo.test";
        adminUser.roleId = 1;
        userRepository.persist(adminUser);
    }

    @Test
    void getAll_WhenNoAuthHeader_Returns401() {
        given()
        .when()
            .get("/api/insumos")
        .then()
            .statusCode(401);
    }

    @Test
    void getAll_WhenAuthenticatedFarmer_Returns200() throws Exception {
        String token = "insumo-farmer-token";
        when(firebaseTokenVerifier.verifyTokenAndGetUid(token)).thenReturn("insumo-farmer-uuid");

        AplicacionInsumoResponseDto item = new AplicacionInsumoResponseDto(
                1L, LocalDate.of(2025, 5, 15), 1001L, "Fertilizante", "kg", 1.0, 1L, "Parcela Norte", 1L);
        when(getAplicacionesByFarmerUseCase.execute(anyLong())).thenReturn(List.of(item));

        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .get("/api/insumos")
        .then()
            .statusCode(200)
            .body("[0].aplicacionId", equalTo(1))
            .body("[0].productoNombre", equalTo("Fertilizante"));
    }

    @Test
    void post_WhenNonFarmer_Returns403() throws Exception {
        String token = "insumo-admin-token";
        when(firebaseTokenVerifier.verifyTokenAndGetUid(token)).thenReturn("insumo-admin-uuid");

        RegisterAplicacionInsumoDto dto = new RegisterAplicacionInsumoDto();
        dto.fecha = LocalDate.of(2025, 5, 15);
        dto.skuIdVendedor = 1001L;
        dto.cantidad = 1.0;
        dto.parcelaId = 1L;

        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body(dto)
        .when()
            .post("/api/insumos")
        .then()
            .statusCode(403);
    }

    @Test
    void post_WhenValidFarmerRequest_Returns201() throws Exception {
        String token = "insumo-farmer-token";
        when(firebaseTokenVerifier.verifyTokenAndGetUid(token)).thenReturn("insumo-farmer-uuid");

        RegisterAplicacionInsumoDto dto = new RegisterAplicacionInsumoDto();
        dto.fecha = LocalDate.of(2025, 5, 15);
        dto.skuIdVendedor = 1001L;
        dto.cantidad = 1.0;
        dto.parcelaId = 1L;

        AplicacionInsumoResponseDto response = new AplicacionInsumoResponseDto(
                10L, LocalDate.of(2025, 5, 15), 1001L, "Fertilizante", "kg", 1.0, 1L, "Parcela Norte", 1L);
        when(registerAplicacionInsumoUseCase.execute(any(), anyLong())).thenReturn(response);

        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body(dto)
        .when()
            .post("/api/insumos")
        .then()
            .statusCode(201)
            .body("aplicacionId", equalTo(10))
            .body("productoNombre", equalTo("Fertilizante"));
    }

    @Test
    void post_WhenBodyIsMissing_Returns400() throws Exception {
        String token = "insumo-farmer-token";
        when(firebaseTokenVerifier.verifyTokenAndGetUid(token)).thenReturn("insumo-farmer-uuid");

        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
        .when()
            .post("/api/insumos")
        .then()
            .statusCode(400);
    }
}
