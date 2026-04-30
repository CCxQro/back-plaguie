package itesm.mx.infrastructure.security;

import com.google.firebase.auth.FirebaseAuthException;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
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
import static org.mockito.Mockito.when;

@QuarkusTest
@TestProfile(H2TestProfile.class)
class FirebaseAuthFilterIntegrationTest {

    @InjectMock
    FirebaseTokenVerifier firebaseTokenVerifier;

    @InjectMock
    FirebaseUserManager firebaseUserManager;

    @Inject
    UserRepositoryImpl userRepository;

    private static final String PROTECTED_ENDPOINT = "/api/ruta-protegida";

    @BeforeEach
    @Transactional
    void setup() {
        userRepository.deleteAll();

        UserEntity testUser = new UserEntity();
        testUser.firebaseUuid = "uuid-valido";
        testUser.name = "Usuario Autorizado";
        testUser.email = "autorizado@correo.com";
        testUser.roleId = 1;
        userRepository.persist(testUser);
    }

    @Test
    void filter_WhenNoAuthHeader_Returns401() {
        given()
        .when()
            .get(PROTECTED_ENDPOINT)
        .then()
            .statusCode(401);
    }

    @Test
    void filter_WhenInvalidAuthFormat_Returns401() {
        given()
            .header("Authorization", "TokenInvalidoSinBearer 12345")
        .when()
            .get(PROTECTED_ENDPOINT)
        .then()
            .statusCode(401);
    }

    @Test
    void filter_WhenFirebaseRejectsToken_Returns401() throws Exception {
        String invalidToken = "token_expirado";

        FirebaseAuthException mockAuthException = org.mockito.Mockito.mock(FirebaseAuthException.class);
        when(mockAuthException.getMessage()).thenReturn("Token inválido");

        when(firebaseTokenVerifier.verifyTokenAndGetUid(invalidToken))
                .thenThrow(mockAuthException);

        given()
            .header("Authorization", "Bearer " + invalidToken)
        .when()
            .get(PROTECTED_ENDPOINT)
        .then()
            .statusCode(401);
    }

    @Test
    void filter_WhenUserNotInDatabase_Returns401() throws Exception {
        String token = "token_de_usuario_fantasma";
        String unregisteredUuid = "uuid-no-registrado";

        when(firebaseTokenVerifier.verifyTokenAndGetUid(token)).thenReturn(unregisteredUuid);

        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .get(PROTECTED_ENDPOINT)
        .then()
            .statusCode(401);
    }

    @Test
    void filter_WhenLoginEndpointIsCalled_BypassesFilter() {

        given()
            .contentType("application/json")
            .body("{}")
        .when()
            .post("/api/auth/login")
        .then()
            .statusCode(400);
    }

    @Test
    void filter_WhenValidTokenAndUserExists_PassesFilter() throws Exception {
        String validToken = "token_perfecto";
        String registeredUuid = "uuid-valido"; 

        when(firebaseTokenVerifier.verifyTokenAndGetUid(validToken)).thenReturn(registeredUuid);

        given()
            .header("Authorization", "Bearer " + validToken)
        .when()
            .get(PROTECTED_ENDPOINT)
        .then()
            .statusCode(200)
            .body("firebaseUuid", equalTo("uuid-valido"))
            .body("email", equalTo("autorizado@correo.com"));
    }
}
