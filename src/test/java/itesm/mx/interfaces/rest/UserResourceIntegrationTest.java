package itesm.mx.interfaces.rest;

import com.google.firebase.auth.FirebaseAuthException;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;
import itesm.mx.application.dto.GetUserResponseDto;
import itesm.mx.application.dto.UpdateUserDto;
import itesm.mx.application.usecase.users.DeactivateUserUseCase;
import itesm.mx.application.usecase.users.GetAllUsersUseCase;
import itesm.mx.application.usecase.users.GetUserByIdUseCase;
import itesm.mx.application.usecase.users.UpdateUserUseCase;
import itesm.mx.infrastructure.firebase.FirebaseTokenVerifier;
import itesm.mx.infrastructure.firebase.FirebaseUserManager;
import itesm.mx.infrastructure.persistence.entity.users.UserEntity;
import itesm.mx.infrastructure.persistence.repository.user.UserRepositoryImpl;
import itesm.mx.support.H2TestProfile;
import itesm.mx.application.dto.UserPageResponseDto;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@QuarkusTest
@TestProfile(H2TestProfile.class)
class UserResourceIntegrationTest {

    @InjectMock
    FirebaseTokenVerifier firebaseTokenVerifier;

    @InjectMock
    FirebaseUserManager firebaseUserManager;

    @InjectMock
    GetAllUsersUseCase getAllUsersUseCase;

    @InjectMock
    GetUserByIdUseCase getUserByIdUseCase;

    @InjectMock
    UpdateUserUseCase updateUserUseCase;

    @InjectMock
    DeactivateUserUseCase deactivateUserUseCase;

    @Inject
    UserRepositoryImpl userRepository;

    private static final String ADMIN_TOKEN = "admin-token";
    private static final String FARMER_TOKEN = "farmer-token";

    private Long adminId;
    private Long farmerId;

    @BeforeEach
    @Transactional
    void setup() {
        userRepository.deleteAll();

        UserEntity admin = new UserEntity();
        admin.firebaseUuid = "uuid-admin-user";
        admin.name = "Admin User";
        admin.email = "admin@itesm.mx";
        admin.roleId = 1;
        admin.isActive = true;
        userRepository.persist(admin);
        adminId = admin.userId;

        UserEntity farmer = new UserEntity();
        farmer.firebaseUuid = "uuid-farmer-user";
        farmer.name = "Farmer User";
        farmer.email = "farmer@itesm.mx";
        farmer.roleId = 2;
        farmer.isActive = true;
        userRepository.persist(farmer);
        farmerId = farmer.userId;
    }

    // --- GET /api/users ---

    @Test
    void getUsers_WhenNoAuthHeader_Returns401() {
        given()
        .when()
            .get("/api/users")
        .then()
            .statusCode(401);
    }

    @Test
    void getUsers_WhenAuthenticatedAsNonAdmin_Returns403() throws Exception {
        when(firebaseTokenVerifier.verifyTokenAndGetUid(FARMER_TOKEN)).thenReturn("uuid-farmer-user");

        given()
            .header("Authorization", "Bearer " + FARMER_TOKEN)
        .when()
            .get("/api/users")
        .then()
            .statusCode(403)
            .body("error", equalTo("Solo un administrador puede listar todos los usuarios"));
    }

    @Test
    void getUsers_WhenAuthenticatedAsAdmin_Returns200WithPagedEnvelope() throws Exception {
        when(firebaseTokenVerifier.verifyTokenAndGetUid(ADMIN_TOKEN)).thenReturn("uuid-admin-user");
        when(getAllUsersUseCase.execute(anyInt(), anyInt(), any(), any(), any()))
                .thenReturn(new UserPageResponseDto(List.of(
                        new GetUserResponseDto(adminId, "uuid-admin-user", "Admin User", "admin@itesm.mx", 1, true),
                        new GetUserResponseDto(farmerId, "uuid-farmer-user", "Farmer User", "farmer@itesm.mx", 2, true)
                ), 2L, 0, 10));

        given()
            .header("Authorization", "Bearer " + ADMIN_TOKEN)
        .when()
            .get("/api/users")
        .then()
            .statusCode(200)
            .body("content.size()", equalTo(2))
            .body("totalElements", equalTo(2))
            .body("totalPages", equalTo(1))
            .body("page", equalTo(0))
            .body("size", equalTo(10));
    }

    @Test
    void getUsers_WithQueryParams_ForwardedToUseCase() throws Exception {
        when(firebaseTokenVerifier.verifyTokenAndGetUid(ADMIN_TOKEN)).thenReturn("uuid-admin-user");
        when(getAllUsersUseCase.execute(eq(1), eq(5), eq("farm"), eq(2), eq(true)))
                .thenReturn(new UserPageResponseDto(List.of(
                        new GetUserResponseDto(farmerId, "uuid-farmer-user", "Farmer User", "farmer@itesm.mx", 2, true)
                ), 1L, 1, 5));

        given()
            .header("Authorization", "Bearer " + ADMIN_TOKEN)
            .queryParam("page", 1)
            .queryParam("size", 5)
            .queryParam("name", "farm")
            .queryParam("roleId", 2)
            .queryParam("isActive", true)
        .when()
            .get("/api/users")
        .then()
            .statusCode(200)
            .body("content.size()", equalTo(1))
            .body("totalElements", equalTo(1));
    }

    @Test
    void getUsers_WhenInvalidPageParam_Returns400() throws Exception {
        when(firebaseTokenVerifier.verifyTokenAndGetUid(ADMIN_TOKEN)).thenReturn("uuid-admin-user");
        when(getAllUsersUseCase.execute(eq(-1), anyInt(), any(), any(), any()))
                .thenThrow(new IllegalArgumentException("El número de página no puede ser negativo"));

        given()
            .header("Authorization", "Bearer " + ADMIN_TOKEN)
            .queryParam("page", -1)
        .when()
            .get("/api/users")
        .then()
            .statusCode(400)
            .body("error", equalTo("El número de página no puede ser negativo"));
    }

    // --- GET /api/users/{id} ---

    @Test
    void getUserById_WhenNoAuthHeader_Returns401() {
        given()
        .when()
            .get("/api/users/" + adminId)
        .then()
            .statusCode(401);
    }

    @Test
    void getUserById_WhenAdminRequestsAnyUser_Returns200() throws Exception {
        when(firebaseTokenVerifier.verifyTokenAndGetUid(ADMIN_TOKEN)).thenReturn("uuid-admin-user");
        when(getUserByIdUseCase.execute(farmerId)).thenReturn(
                new GetUserResponseDto(farmerId, "uuid-farmer-user", "Farmer User", "farmer@itesm.mx", 2, true)
        );

        given()
            .header("Authorization", "Bearer " + ADMIN_TOKEN)
        .when()
            .get("/api/users/" + farmerId)
        .then()
            .statusCode(200)
            .body("email", equalTo("farmer@itesm.mx"));
    }

    @Test
    void getUserById_WhenUserRequestsOwnId_Returns200() throws Exception {
        when(firebaseTokenVerifier.verifyTokenAndGetUid(FARMER_TOKEN)).thenReturn("uuid-farmer-user");
        when(getUserByIdUseCase.execute(farmerId)).thenReturn(
                new GetUserResponseDto(farmerId, "uuid-farmer-user", "Farmer User", "farmer@itesm.mx", 2, true)
        );

        given()
            .header("Authorization", "Bearer " + FARMER_TOKEN)
        .when()
            .get("/api/users/" + farmerId)
        .then()
            .statusCode(200)
            .body("firebaseUuid", equalTo("uuid-farmer-user"));
    }

    @Test
    void getUserById_WhenNonAdminRequestsOtherId_Returns403() throws Exception {
        when(firebaseTokenVerifier.verifyTokenAndGetUid(FARMER_TOKEN)).thenReturn("uuid-farmer-user");

        given()
            .header("Authorization", "Bearer " + FARMER_TOKEN)
        .when()
            .get("/api/users/" + adminId)
        .then()
            .statusCode(403)
            .body("error", equalTo("No tienes permiso para consultar este usuario"));
    }

    @Test
    void getUserById_WhenUserNotFound_Returns404() throws Exception {
        when(firebaseTokenVerifier.verifyTokenAndGetUid(ADMIN_TOKEN)).thenReturn("uuid-admin-user");
        when(getUserByIdUseCase.execute(99999L))
                .thenThrow(new IllegalStateException("Usuario no encontrado con id: 99999"));

        given()
            .header("Authorization", "Bearer " + ADMIN_TOKEN)
        .when()
            .get("/api/users/99999")
        .then()
            .statusCode(404);
    }

    // --- PUT /api/users/{id} ---

    @Test
    void updateUser_WhenNoAuthHeader_Returns401() {
        given()
            .contentType(ContentType.JSON)
            .body(new UpdateUserDto())
        .when()
            .put("/api/users/" + adminId)
        .then()
            .statusCode(401);
    }

    @Test
    void updateUser_WhenNonAdmin_Returns403() throws Exception {
        when(firebaseTokenVerifier.verifyTokenAndGetUid(FARMER_TOKEN)).thenReturn("uuid-farmer-user");

        UpdateUserDto dto = new UpdateUserDto();
        dto.name = "New Name";

        given()
            .header("Authorization", "Bearer " + FARMER_TOKEN)
            .contentType(ContentType.JSON)
            .body(dto)
        .when()
            .put("/api/users/" + farmerId)
        .then()
            .statusCode(403)
            .body("error", equalTo("Solo un administrador puede actualizar usuarios"));
    }

    @Test
    void updateUser_WhenMissingBody_Returns400() throws Exception {
        when(firebaseTokenVerifier.verifyTokenAndGetUid(ADMIN_TOKEN)).thenReturn("uuid-admin-user");

        given()
            .header("Authorization", "Bearer " + ADMIN_TOKEN)
            .contentType(ContentType.JSON)
        .when()
            .put("/api/users/" + adminId)
        .then()
            .statusCode(400)
            .body("error", equalTo("El cuerpo de la solicitud es requerido"));
    }

    @Test
    void updateUser_WhenAdminAndValidBody_Returns200WithUpdatedData() throws Exception {
        when(firebaseTokenVerifier.verifyTokenAndGetUid(ADMIN_TOKEN)).thenReturn("uuid-admin-user");

        UpdateUserDto dto = new UpdateUserDto();
        dto.name = "Renamed Admin";

        when(updateUserUseCase.execute(eq(adminId), any(UpdateUserDto.class))).thenReturn(
                new GetUserResponseDto(adminId, "uuid-admin-user", "Renamed Admin", "admin@itesm.mx", 1, true)
        );

        given()
            .header("Authorization", "Bearer " + ADMIN_TOKEN)
            .contentType(ContentType.JSON)
            .body(dto)
        .when()
            .put("/api/users/" + adminId)
        .then()
            .statusCode(200)
            .body("name", equalTo("Renamed Admin"));
    }

    @Test
    void updateUser_WhenUserNotFound_Returns404() throws Exception {
        when(firebaseTokenVerifier.verifyTokenAndGetUid(ADMIN_TOKEN)).thenReturn("uuid-admin-user");

        UpdateUserDto dto = new UpdateUserDto();
        dto.name = "Ghost";

        when(updateUserUseCase.execute(eq(99999L), any(UpdateUserDto.class)))
                .thenThrow(new IllegalStateException("Usuario no encontrado con id: 99999"));

        given()
            .header("Authorization", "Bearer " + ADMIN_TOKEN)
            .contentType(ContentType.JSON)
            .body(dto)
        .when()
            .put("/api/users/99999")
        .then()
            .statusCode(404);
    }

    // --- DELETE /api/users/{id} ---

    @Test
    void deactivateUser_WhenNoAuthHeader_Returns401() {
        given()
        .when()
            .delete("/api/users/" + farmerId)
        .then()
            .statusCode(401);
    }

    @Test
    void deactivateUser_WhenNonAdmin_Returns403() throws Exception {
        when(firebaseTokenVerifier.verifyTokenAndGetUid(FARMER_TOKEN)).thenReturn("uuid-farmer-user");

        given()
            .header("Authorization", "Bearer " + FARMER_TOKEN)
        .when()
            .delete("/api/users/" + farmerId)
        .then()
            .statusCode(403)
            .body("error", equalTo("Solo un administrador puede desactivar usuarios"));
    }

    @Test
    void deactivateUser_WhenAdminAndUserExists_Returns204() throws Exception {
        when(firebaseTokenVerifier.verifyTokenAndGetUid(ADMIN_TOKEN)).thenReturn("uuid-admin-user");

        given()
            .header("Authorization", "Bearer " + ADMIN_TOKEN)
        .when()
            .delete("/api/users/" + farmerId)
        .then()
            .statusCode(204);
    }

    @Test
    void deactivateUser_WhenUserNotFound_Returns404() throws Exception {
        when(firebaseTokenVerifier.verifyTokenAndGetUid(ADMIN_TOKEN)).thenReturn("uuid-admin-user");
        doThrow(new IllegalStateException("Usuario no encontrado con id: 99999"))
                .when(deactivateUserUseCase).execute(99999L);

        given()
            .header("Authorization", "Bearer " + ADMIN_TOKEN)
        .when()
            .delete("/api/users/99999")
        .then()
            .statusCode(404);
    }

    @Test
    void deactivateUser_WhenInvalidId_Returns400() throws Exception {
        when(firebaseTokenVerifier.verifyTokenAndGetUid(ADMIN_TOKEN)).thenReturn("uuid-admin-user");
        doThrow(new IllegalArgumentException("El ID de usuario no es válido"))
                .when(deactivateUserUseCase).execute(0L);

        given()
            .header("Authorization", "Bearer " + ADMIN_TOKEN)
        .when()
            .delete("/api/users/0")
        .then()
            .statusCode(400)
            .body("error", equalTo("El ID de usuario no es válido"));
    }

    @Test
    void deactivateUser_WhenInvalidToken_Returns401() throws Exception {
        FirebaseAuthException mockEx = org.mockito.Mockito.mock(FirebaseAuthException.class);
        when(mockEx.getMessage()).thenReturn("Token inválido");
        when(firebaseTokenVerifier.verifyTokenAndGetUid("bad-token")).thenThrow(mockEx);

        given()
            .header("Authorization", "Bearer bad-token")
        .when()
            .delete("/api/users/" + farmerId)
        .then()
            .statusCode(401);
    }
}
