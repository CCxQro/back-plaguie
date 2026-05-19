package itesm.mx.interfaces.rest;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import itesm.mx.application.usecase.reporte.ExportarReportePredictivoExcelUseCase;
import itesm.mx.application.usecase.reporte.ExportarReportePredictivoPdfUseCase;
import itesm.mx.application.usecase.reporte.GenerarReportePredictivoPlagasUseCase;
import itesm.mx.domain.models.reporte.PrediccionPlaga;
import itesm.mx.domain.models.reporte.ReportePredictivoPlagas;
import itesm.mx.domain.models.reporte.Temporada;
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
import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@QuarkusTest
@TestProfile(H2TestProfile.class)
class ReportePredictivoPlagaResourceIntegrationTest {

    @InjectMock
    FirebaseTokenVerifier firebaseTokenVerifier;

    @InjectMock
    FirebaseUserManager firebaseUserManager;

    @InjectMock
    GenerarReportePredictivoPlagasUseCase generarUseCase;

    @InjectMock
    ExportarReportePredictivoPdfUseCase exportarPdfUseCase;

    @InjectMock
    ExportarReportePredictivoExcelUseCase exportarExcelUseCase;

    @Inject
    UserRepositoryImpl userRepository;

    @BeforeEach
    @Transactional
    void setup() {
        userRepository.deleteAll();

        UserEntity admin = new UserEntity();
        admin.firebaseUuid = "reporte-admin-uuid";
        admin.name = "Reporte Admin";
        admin.email = "admin@reporte.test";
        admin.roleId = 1;
        userRepository.persist(admin);

        UserEntity seller = new UserEntity();
        seller.firebaseUuid = "reporte-seller-uuid";
        seller.name = "Reporte Seller";
        seller.email = "seller@reporte.test";
        seller.roleId = 3;
        userRepository.persist(seller);

        UserEntity farmer = new UserEntity();
        farmer.firebaseUuid = "reporte-farmer-uuid";
        farmer.name = "Reporte Farmer";
        farmer.email = "farmer@reporte.test";
        farmer.roleId = 2;
        userRepository.persist(farmer);
    }

    private ReportePredictivoPlagas sampleReporte() {
        return new ReportePredictivoPlagas(
                "Jalisco",
                Temporada.VERANO,
                LocalDateTime.of(2026, 6, 15, 10, 30),
                25L,
                "Resumen ejecutivo de prueba",
                List.of(new PrediccionPlaga("Pulgon", 80, "Junio-Julio", "Alto", "Maiz", "alta presion", "Neonic A"))
        );
    }

    @Test
    void getReporte_WhenNoAuth_Returns401() {
        given()
        .when()
            .get("/api/reports/plagas/predictivo?region=Jalisco&temporada=verano")
        .then()
            .statusCode(401);
    }

    @Test
    void getReporte_WhenFarmer_Returns403() throws Exception {
        String token = "reporte-farmer-token";
        when(firebaseTokenVerifier.verifyTokenAndGetUid(token)).thenReturn("reporte-farmer-uuid");

        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .get("/api/reports/plagas/predictivo?region=Jalisco&temporada=verano")
        .then()
            .statusCode(403);
    }

    @Test
    void getReporte_WhenSeller_Returns200AndJson() throws Exception {
        String token = "reporte-seller-token";
        when(firebaseTokenVerifier.verifyTokenAndGetUid(token)).thenReturn("reporte-seller-uuid");
        when(generarUseCase.execute(anyString(), anyString())).thenReturn(sampleReporte());

        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .get("/api/reports/plagas/predictivo?region=Jalisco&temporada=verano")
        .then()
            .statusCode(200)
            .body("region", equalTo("Jalisco"))
            .body("season", equalTo("Verano"))
            .body("observationsAnalyzed", equalTo(25))
            .body("predictions[0].plagueName", equalTo("Pulgon"))
            .body("predictions[0].probability", equalTo(80));
    }

    @Test
    void getReporte_WhenAdmin_Returns200() throws Exception {
        String token = "reporte-admin-token";
        when(firebaseTokenVerifier.verifyTokenAndGetUid(token)).thenReturn("reporte-admin-uuid");
        when(generarUseCase.execute(anyString(), anyString())).thenReturn(sampleReporte());

        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .get("/api/reports/plagas/predictivo?region=Jalisco&temporada=verano")
        .then()
            .statusCode(200);
    }

    @Test
    void getReporte_WhenInvalidTemporada_Returns400() throws Exception {
        String token = "reporte-seller-token";
        when(firebaseTokenVerifier.verifyTokenAndGetUid(token)).thenReturn("reporte-seller-uuid");
        when(generarUseCase.execute(anyString(), anyString()))
                .thenThrow(new IllegalArgumentException("Temporada invalida: xx. Valores aceptados: primavera, verano, otono, invierno"));

        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .get("/api/reports/plagas/predictivo?region=Jalisco&temporada=xx")
        .then()
            .statusCode(400)
            .body("error", equalTo("Temporada invalida: xx. Valores aceptados: primavera, verano, otono, invierno"));
    }

    @Test
    void exportPdf_WhenSeller_ReturnsPdfBytes() throws Exception {
        String token = "reporte-seller-token";
        when(firebaseTokenVerifier.verifyTokenAndGetUid(token)).thenReturn("reporte-seller-uuid");
        byte[] fakePdf = "%PDF-1.4\n%fake".getBytes();
        when(exportarPdfUseCase.execute(anyString(), anyString())).thenReturn(fakePdf);

        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .get("/api/reports/plagas/predictivo/export/pdf?region=Jalisco&temporada=verano")
        .then()
            .statusCode(200)
            .contentType("application/pdf")
            .header("Content-Disposition", org.hamcrest.Matchers.containsString("attachment"));
    }

    @Test
    void exportExcel_WhenSeller_ReturnsExcelBytes() throws Exception {
        String token = "reporte-seller-token";
        when(firebaseTokenVerifier.verifyTokenAndGetUid(token)).thenReturn("reporte-seller-uuid");
        byte[] fakeXlsx = new byte[]{0x50, 0x4B, 0x03, 0x04, 0x00};
        when(exportarExcelUseCase.execute(anyString(), anyString())).thenReturn(fakeXlsx);

        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .get("/api/reports/plagas/predictivo/export/excel?region=Jalisco&temporada=verano")
        .then()
            .statusCode(200)
            .contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
            .header("Content-Length", org.hamcrest.Matchers.notNullValue());
    }

    @Test
    void exportPdf_WhenNoAuth_Returns401() {
        given()
        .when()
            .get("/api/reports/plagas/predictivo/export/pdf?region=Jalisco&temporada=verano")
        .then()
            .statusCode(401);
    }
}
