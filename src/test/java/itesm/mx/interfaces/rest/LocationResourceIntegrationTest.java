package itesm.mx.interfaces.rest;

import com.google.firebase.auth.FirebaseAuthException;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;
import itesm.mx.application.dto.GetLocationResponseDto;
import itesm.mx.application.dto.RegisterLocationDto;
import itesm.mx.application.usecase.location.location.GetAllLocationsUseCase;
import itesm.mx.application.usecase.location.location.RegisterLocationUseCase;
import itesm.mx.infrastructure.firebase.FirebaseTokenVerifier;
import itesm.mx.infrastructure.firebase.FirebaseUserManager;
import itesm.mx.infrastructure.persistence.entity.users.UserEntity;
import itesm.mx.infrastructure.persistence.repository.UserRepositoryImpl;
import itesm.mx.support.H2TestProfile;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@QuarkusTest
@TestProfile(H2TestProfile.class)
class LocationResourceIntegrationTest {

    @InjectMock
    FirebaseTokenVerifier firebaseTokenVerifier;

    @InjectMock
    FirebaseUserManager firebaseUserManager;

    @InjectMock
    GetAllLocationsUseCase getAllLocationsUseCase;

    @InjectMock
    RegisterLocationUseCase registerLocationUseCase;

    @Inject
    UserRepositoryImpl userRepository;

    @BeforeEach
    @Transactional
    void setup() {
        userRepository.deleteAll();

        UserEntity testUser = new UserEntity();
        testUser.firebaseUuid = "location-test-uuid";
        testUser.name = "Location Tester";
        testUser.email = "location@test.com";
        testUser.roleId = 1;
        userRepository.persist(testUser);
    }

    @Test
    void getAllLocations_WhenNoAuthHeader_Returns401() {
        given()
        .when()
            .get("/api/locations")
        .then()
            .statusCode(401);
    }

    @Test
    void getAllLocations_WhenAuthenticated_Returns200AndBody() throws Exception {
        String token = "valid-location-token";
        when(firebaseTokenVerifier.verifyTokenAndGetUid(token)).thenReturn("location-test-uuid");
        when(getAllLocationsUseCase.execute()).thenReturn(List.of(
                new GetLocationResponseDto(
                        1L,
                        20.67,
                        -103.35,
                        1L,
                        "jalisco",
                        2L,
                        "guadalajara",
                        3L,
                        "centro",
                        4L,
                        "predio norte"
                )
        ));

        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .get("/api/locations")
        .then()
            .statusCode(200)
            .body("[0].locationId", equalTo(1))
            .body("[0].stateName", equalTo("jalisco"))
            .body("[0].municipalityName", equalTo("guadalajara"));
    }

    @Test
    void registerLocation_WhenBodyIsMissing_Returns400() {
        String token = "valid-location-token";
        try {
            when(firebaseTokenVerifier.verifyTokenAndGetUid(token)).thenReturn("location-test-uuid");
        } catch (FirebaseAuthException e) {
            throw new RuntimeException(e);
        }

        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
        .when()
            .post("/api/locations")
        .then()
            .statusCode(400)
            .body("error", equalTo("El cuerpo de la solicitud es requerido"));
    }

    @Test
    void registerLocation_WhenAuthenticatedAndValid_Returns201AndBody() throws Exception {
        String token = "valid-location-token";
        when(firebaseTokenVerifier.verifyTokenAndGetUid(token)).thenReturn("location-test-uuid");

        RegisterLocationDto request = new RegisterLocationDto();
        request.latitude = 20.67;
        request.longitude = -103.35;
        request.stateName = "Jalisco";
        request.municipalityName = "Guadalajara";
        request.localityName = "Centro";
        request.propertyName = "Predio Norte";

        when(registerLocationUseCase.execute(org.mockito.ArgumentMatchers.any())).thenReturn(
                new GetLocationResponseDto(
                        8L,
                        20.67,
                        -103.35,
                        1L,
                        "jalisco",
                        2L,
                        "guadalajara",
                        3L,
                        "centro",
                        4L,
                        "predio norte"
                )
        );

        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/locations")
        .then()
            .statusCode(201)
            .body("locationId", equalTo(8))
            .body("latitude", equalTo(20.67f))
            .body("longitude", equalTo(-103.35f))
            .body("propertyName", equalTo("predio norte"));
    }

    @Test
    void registerLocation_WhenTokenIsInvalid_Returns401() throws Exception {
        String invalidToken = "invalid-location-token";
        FirebaseAuthException mockAuthException = mock(FirebaseAuthException.class);
        when(mockAuthException.getMessage()).thenReturn("Token invalido");
        when(firebaseTokenVerifier.verifyTokenAndGetUid(invalidToken)).thenThrow(mockAuthException);

        RegisterLocationDto request = new RegisterLocationDto();
        request.latitude = 20.67;
        request.longitude = -103.35;
        request.stateName = "Jalisco";
        request.municipalityName = "Guadalajara";
        request.localityName = "Centro";
        request.propertyName = "Predio Norte";

        given()
            .header("Authorization", "Bearer " + invalidToken)
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/locations")
        .then()
            .statusCode(401);
    }
}
