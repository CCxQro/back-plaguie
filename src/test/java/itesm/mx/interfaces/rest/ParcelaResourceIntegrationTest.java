package itesm.mx.interfaces.rest;

import com.google.firebase.auth.FirebaseAuthException;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import itesm.mx.application.dto.ParcelaDetailDto;
import itesm.mx.application.dto.ParcelaResponseDto;
import itesm.mx.application.usecase.parcela.GetParcelaDetailUseCase;
import itesm.mx.application.usecase.parcela.GetParcelasByFarmerUseCase;
import itesm.mx.application.usecase.parcela.RegisterParcelaUseCase;
import itesm.mx.application.usecase.parcela.GetParcelaCatalogsUseCase;
import itesm.mx.infrastructure.firebase.FirebaseTokenVerifier;
import itesm.mx.infrastructure.firebase.FirebaseUserManager;
import itesm.mx.infrastructure.persistence.entity.users.UserEntity;
import itesm.mx.infrastructure.persistence.repository.user.UserRepositoryImpl;
import itesm.mx.support.H2TestProfile;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

@QuarkusTest
@TestProfile(H2TestProfile.class)
class ParcelaResourceIntegrationTest {

    @InjectMock
    FirebaseTokenVerifier firebaseTokenVerifier;

    @InjectMock
    FirebaseUserManager firebaseUserManager;

    @InjectMock
    GetParcelaDetailUseCase getParcelaDetailUseCase;

    @InjectMock
    GetParcelasByFarmerUseCase getParcelasByFarmerUseCase;

    @InjectMock
    RegisterParcelaUseCase registerParcelaUseCase;

    @InjectMock
    GetParcelaCatalogsUseCase getParcelaCatalogsUseCase;

    @Inject
    UserRepositoryImpl userRepository;

    private static final String FARMER_UUID = "parcela-farmer-uuid";
    private static final String ADMIN_UUID = "parcela-admin-uuid";

    @BeforeEach
    @Transactional
    void setup() throws Exception {
        userRepository.deleteAll();

        UserEntity admin = new UserEntity();
        admin.firebaseUuid = ADMIN_UUID;
        admin.name = "Admin Parcela";
        admin.email = "admin@parcela.test";
        admin.roleId = 1;
        userRepository.persist(admin);

        UserEntity farmer = new UserEntity();
        farmer.firebaseUuid = FARMER_UUID;
        farmer.name = "Farmer Parcela";
        farmer.email = "farmer@parcela.test";
        farmer.roleId = 2;
        userRepository.persist(farmer);

        when(firebaseTokenVerifier.verifyTokenAndGetUid(ADMIN_UUID)).thenReturn(ADMIN_UUID);
        when(firebaseTokenVerifier.verifyTokenAndGetUid(FARMER_UUID)).thenReturn(FARMER_UUID);
    }

    @Test
    void getParcela_NoAuth_Returns401() {
        given()
            .when().get("/api/parcelas/1")
            .then().statusCode(401);
    }

    @Test
    void getParcela_ValidAdmin_Returns200WithDetail() {
        ParcelaDetailDto dto = new ParcelaDetailDto();
        ParcelaResponseDto resp = new ParcelaResponseDto(1L, "Lote Norte", 5.0, "Maíz", "Activo", true);
        dto.parcela = resp;
        dto.healthPercentage = 90.0;
        dto.suggestions = List.of("Revisión preventiva de Gusano cogollero");

        when(getParcelaDetailUseCase.execute(anyLong(), isNull())).thenReturn(dto);

        given()
            .header("Authorization", "Bearer " + ADMIN_UUID)
            .when().get("/api/parcelas/1")
            .then()
            .statusCode(200)
            .body("healthPercentage", equalTo(90.0f))
            .body("suggestions", hasSize(1))
            .body("parcela.nombre", equalTo("Lote Norte"));
    }

    @Test
    void getParcela_ValidFarmer_Returns200() {
        ParcelaDetailDto dto = new ParcelaDetailDto();
        ParcelaResponseDto resp = new ParcelaResponseDto(2L, "Lote Sur", 3.0, "Frijol", "Activo", true);
        dto.parcela = resp;
        dto.healthPercentage = 100.0;
        dto.suggestions = Collections.emptyList();

        when(getParcelaDetailUseCase.execute(anyLong(), anyLong())).thenReturn(dto);

        given()
            .header("Authorization", "Bearer " + FARMER_UUID)
            .when().get("/api/parcelas/2")
            .then()
            .statusCode(200)
            .body("healthPercentage", equalTo(100.0f));
    }

    @Test
    void getParcela_NotFound_Returns400() {
        when(getParcelaDetailUseCase.execute(anyLong(), isNull()))
            .thenThrow(new IllegalArgumentException("Parcela no encontrada"));

        given()
            .header("Authorization", "Bearer " + ADMIN_UUID)
            .when().get("/api/parcelas/999")
            .then()
            .statusCode(400);
    }
}
