package itesm.mx.interfaces.rest;

import com.google.firebase.auth.FirebaseAuthException;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;
import itesm.mx.application.dto.LoginDto;
import itesm.mx.infrastructure.firebase.FirebaseTokenVerifier;
import itesm.mx.infrastructure.firebase.FirebaseUserManager;
import itesm.mx.infrastructure.persistence.entity.users.UserEntity;
import itesm.mx.infrastructure.persistence.repository.UserRepositoryImpl;
import itesm.mx.support.H2TestProfile;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@QuarkusTest
@TestProfile(H2TestProfile.class)
class AuthResourceIntegrationTest {

    @InjectMock
    FirebaseTokenVerifier firebaseTokenVerifier;

    @InjectMock
    FirebaseUserManager firebaseUserManager;

    @Inject
    UserRepositoryImpl userRepository;

    @BeforeEach
    @Transactional 
    void setup() {
        userRepository.deleteAll();

        UserEntity testUser = new UserEntity();
        testUser.firebaseUuid = "uuid-real-para-pruebas";
        testUser.name = "Usuario Integracion";
        testUser.email = "integracion@itesm.mx";
        testUser.roleId = 1;
        
        userRepository.persist(testUser);
    }

    @Test
    void login_WithValidMockedToken_Returns200AndUserData() throws Exception {
        String fakeToken = "token_simulado_frontend";
        String expectedUuid = "uuid-real-para-pruebas";

        when(firebaseTokenVerifier.verifyTokenAndGetUid(fakeToken)).thenReturn(expectedUuid);

        LoginDto requestDto = new LoginDto();
        requestDto.firebaseToken = fakeToken;

        given()
            .contentType(ContentType.JSON)
            .body(requestDto)
        .when()
            .post("/api/auth/login")
        .then()
            .statusCode(200)
            .body("name", equalTo("Usuario Integracion"))
            .body("email", equalTo("integracion@itesm.mx"));
    }

    @Test
    void login_WithInvalidToken_Returns401Unauthorized() throws Exception {
        String invalidToken = "token_basura";

        // Simulamos una excepción válida del contrato del verificador para ejercer el wrapping real del login
        FirebaseAuthException mockAuthException = mock(FirebaseAuthException.class);
        when(mockAuthException.getMessage()).thenReturn("El token ha expirado o no es válido");
        when(firebaseTokenVerifier.verifyTokenAndGetUid(invalidToken))
            .thenThrow(mockAuthException);

        LoginDto requestDto = new LoginDto();
        requestDto.firebaseToken = invalidToken;

        given()
            .contentType(ContentType.JSON)
            .body(requestDto)
        .when()
            .post("/api/auth/login")
        .then()
            .statusCode(401)
            // Validamos que se devuelva el JSON {"error": "..."} definido en AuthResource.ErrorResponse
            .body("error", containsString("no es válido"));
    }

    @Test
    void login_WhenDtoIsNull_Returns400BadRequest() {
        // Probamos la validación manual del DTO nulo dentro del endpoint
        given()
            .contentType(ContentType.JSON)
        .when()
            .post("/api/auth/login")
        .then()
            .statusCode(400)
            .body("error", equalTo("El cuerpo de la solicitud es requerido"));
    }
}
