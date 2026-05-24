package itesm.mx.interfaces.rest;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import itesm.mx.application.usecase.users.subUsers.GetAdministratorByUserIdUseCase;
import itesm.mx.application.usecase.users.subUsers.GetAllAdministratorsUseCase;
import itesm.mx.application.usecase.users.subUsers.GetAllFarmersUseCase;
import itesm.mx.application.usecase.users.subUsers.GetAllTechnicalSellersUseCase;
import itesm.mx.application.usecase.users.subUsers.GetFarmerByUserIdUseCase;
import itesm.mx.application.usecase.users.subUsers.GetTechnicalSellerByUserIdUseCase;
import itesm.mx.domain.models.user.Administrator;
import itesm.mx.domain.models.user.Farmer;
import itesm.mx.domain.models.user.TechnicalSeller;
import itesm.mx.domain.models.user.User;
import itesm.mx.infrastructure.firebase.FirebaseTokenVerifier;
import itesm.mx.infrastructure.firebase.FirebaseUserManager;
import itesm.mx.infrastructure.persistence.entity.users.UserEntity;
import itesm.mx.infrastructure.persistence.repository.user.UserRepositoryImpl;
import itesm.mx.support.H2TestProfile;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.Mockito.when;

@QuarkusTest
@TestProfile(H2TestProfile.class)
class SubUsersResourceIntegrationTest {

    @InjectMock
    FirebaseTokenVerifier firebaseTokenVerifier;

    @InjectMock
    FirebaseUserManager firebaseUserManager;

    @InjectMock
    GetAllFarmersUseCase getAllFarmersUseCase;

    @InjectMock
    GetAllTechnicalSellersUseCase getAllTechnicalSellersUseCase;

    @InjectMock
    GetAllAdministratorsUseCase getAllAdministratorsUseCase;

    @InjectMock
    GetFarmerByUserIdUseCase getFarmerByUserIdUseCase;

    @InjectMock
    GetTechnicalSellerByUserIdUseCase getTechnicalSellerByUserIdUseCase;

    @InjectMock
    GetAdministratorByUserIdUseCase getAdministratorByUserIdUseCase;

    @Inject
    UserRepositoryImpl userRepository;

    private static final String ADMIN_TOKEN = "admin-token";
    private static final String FARMER_TOKEN = "farmer-token";
    private static final String SELLER_TOKEN = "seller-token";

    private Long adminId;
    private Long farmerId;
    private Long sellerId;

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

        UserEntity seller = new UserEntity();
        seller.firebaseUuid = "uuid-seller-user";
        seller.name = "Seller User";
        seller.email = "seller@itesm.mx";
        seller.roleId = 3;
        seller.isActive = true;
        userRepository.persist(seller);
        sellerId = seller.userId;
    }

    // --- GET /api/users/farmers/{userId} ---

    @Test
    void getFarmerByUserId_WhenNoAuthHeader_Returns401() {
        given()
        .when()
            .get("/api/users/farmers/" + farmerId)
        .then()
            .statusCode(401);
    }

    @Test
    void getFarmerByUserId_WhenAuthenticatedAsSeller_Returns403() throws Exception {
        when(firebaseTokenVerifier.verifyTokenAndGetUid(SELLER_TOKEN)).thenReturn("uuid-seller-user");

        given()
            .header("Authorization", "Bearer " + SELLER_TOKEN)
        .when()
            .get("/api/users/farmers/" + farmerId)
        .then()
            .statusCode(403)
            .body("error", equalTo("No tienes permiso para consultar agricultores"));
    }

    @Test
    void getFarmerByUserId_WhenAuthenticatedAsAdminAndFound_Returns200() throws Exception {
        when(firebaseTokenVerifier.verifyTokenAndGetUid(ADMIN_TOKEN)).thenReturn("uuid-admin-user");
        User user = new User(farmerId, "uuid-farmer-user", "Farmer User", "farmer@itesm.mx", 2, true);
        when(getFarmerByUserIdUseCase.execute(farmerId))
                .thenReturn(Optional.of(new Farmer(1L, user, true)));

        given()
            .header("Authorization", "Bearer " + ADMIN_TOKEN)
        .when()
            .get("/api/users/farmers/" + farmerId)
        .then()
            .statusCode(200)
            .body("farmerId", equalTo(1))
            .body("user.userId", equalTo(farmerId.intValue()))
            .body("user.email", equalTo("farmer@itesm.mx"));
    }

    @Test
    void getFarmerByUserId_WhenAuthenticatedAsFarmerAndFound_Returns200() throws Exception {
        when(firebaseTokenVerifier.verifyTokenAndGetUid(FARMER_TOKEN)).thenReturn("uuid-farmer-user");
        User user = new User(farmerId, "uuid-farmer-user", "Farmer User", "farmer@itesm.mx", 2, true);
        when(getFarmerByUserIdUseCase.execute(farmerId))
                .thenReturn(Optional.of(new Farmer(1L, user, true)));

        given()
            .header("Authorization", "Bearer " + FARMER_TOKEN)
        .when()
            .get("/api/users/farmers/" + farmerId)
        .then()
            .statusCode(200)
            .body("user.userId", equalTo(farmerId.intValue()));
    }

    @Test
    void getFarmerByUserId_WhenNotFound_Returns404() throws Exception {
        when(firebaseTokenVerifier.verifyTokenAndGetUid(ADMIN_TOKEN)).thenReturn("uuid-admin-user");
        when(getFarmerByUserIdUseCase.execute(99999L)).thenReturn(Optional.empty());

        given()
            .header("Authorization", "Bearer " + ADMIN_TOKEN)
        .when()
            .get("/api/users/farmers/99999")
        .then()
            .statusCode(404)
            .body("error", equalTo("Agricultor no encontrado"));
    }

    // --- GET /api/users/technical-sellers/{userId} ---

    @Test
    void getTechnicalSellerByUserId_WhenNoAuthHeader_Returns401() {
        given()
        .when()
            .get("/api/users/technical-sellers/" + sellerId)
        .then()
            .statusCode(401);
    }

    @Test
    void getTechnicalSellerByUserId_WhenAuthenticatedAsFarmer_Returns403() throws Exception {
        when(firebaseTokenVerifier.verifyTokenAndGetUid(FARMER_TOKEN)).thenReturn("uuid-farmer-user");

        given()
            .header("Authorization", "Bearer " + FARMER_TOKEN)
        .when()
            .get("/api/users/technical-sellers/" + sellerId)
        .then()
            .statusCode(403)
            .body("error", equalTo("No tienes permiso para consultar técnicos vendedores"));
    }

    @Test
    void getTechnicalSellerByUserId_WhenAuthenticatedAsAdminAndFound_Returns200() throws Exception {
        when(firebaseTokenVerifier.verifyTokenAndGetUid(ADMIN_TOKEN)).thenReturn("uuid-admin-user");
        User user = new User(sellerId, "uuid-seller-user", "Seller User", "seller@itesm.mx", 3, true);
        when(getTechnicalSellerByUserIdUseCase.execute(sellerId))
                .thenReturn(Optional.of(new TechnicalSeller(1L, user, true)));

        given()
            .header("Authorization", "Bearer " + ADMIN_TOKEN)
        .when()
            .get("/api/users/technical-sellers/" + sellerId)
        .then()
            .statusCode(200)
            .body("technicalSellerId", equalTo(1))
            .body("user.userId", equalTo(sellerId.intValue()))
            .body("user.email", equalTo("seller@itesm.mx"));
    }

    @Test
    void getTechnicalSellerByUserId_WhenAuthenticatedAsSellerAndFound_Returns200() throws Exception {
        when(firebaseTokenVerifier.verifyTokenAndGetUid(SELLER_TOKEN)).thenReturn("uuid-seller-user");
        User user = new User(sellerId, "uuid-seller-user", "Seller User", "seller@itesm.mx", 3, true);
        when(getTechnicalSellerByUserIdUseCase.execute(sellerId))
                .thenReturn(Optional.of(new TechnicalSeller(1L, user, true)));

        given()
            .header("Authorization", "Bearer " + SELLER_TOKEN)
        .when()
            .get("/api/users/technical-sellers/" + sellerId)
        .then()
            .statusCode(200)
            .body("user.userId", equalTo(sellerId.intValue()));
    }

    @Test
    void getTechnicalSellerByUserId_WhenNotFound_Returns404() throws Exception {
        when(firebaseTokenVerifier.verifyTokenAndGetUid(ADMIN_TOKEN)).thenReturn("uuid-admin-user");
        when(getTechnicalSellerByUserIdUseCase.execute(99999L)).thenReturn(Optional.empty());

        given()
            .header("Authorization", "Bearer " + ADMIN_TOKEN)
        .when()
            .get("/api/users/technical-sellers/99999")
        .then()
            .statusCode(404)
            .body("error", equalTo("Técnico vendedor no encontrado"));
    }

    // --- GET /api/users/administrators/{userId} ---

    @Test
    void getAdministratorByUserId_WhenNoAuthHeader_Returns401() {
        given()
        .when()
            .get("/api/users/administrators/" + adminId)
        .then()
            .statusCode(401);
    }

    @Test
    void getAdministratorByUserId_WhenAuthenticatedAsFarmer_Returns403() throws Exception {
        when(firebaseTokenVerifier.verifyTokenAndGetUid(FARMER_TOKEN)).thenReturn("uuid-farmer-user");

        given()
            .header("Authorization", "Bearer " + FARMER_TOKEN)
        .when()
            .get("/api/users/administrators/" + adminId)
        .then()
            .statusCode(403)
            .body("error", equalTo("Solo un administrador puede consultar administradores"));
    }

    @Test
    void getAdministratorByUserId_WhenAuthenticatedAsSeller_Returns403() throws Exception {
        when(firebaseTokenVerifier.verifyTokenAndGetUid(SELLER_TOKEN)).thenReturn("uuid-seller-user");

        given()
            .header("Authorization", "Bearer " + SELLER_TOKEN)
        .when()
            .get("/api/users/administrators/" + adminId)
        .then()
            .statusCode(403)
            .body("error", equalTo("Solo un administrador puede consultar administradores"));
    }

    @Test
    void getAdministratorByUserId_WhenAuthenticatedAsAdminAndFound_Returns200() throws Exception {
        when(firebaseTokenVerifier.verifyTokenAndGetUid(ADMIN_TOKEN)).thenReturn("uuid-admin-user");
        User user = new User(adminId, "uuid-admin-user", "Admin User", "admin@itesm.mx", 1, true);
        when(getAdministratorByUserIdUseCase.execute(adminId))
                .thenReturn(Optional.of(new Administrator(1L, user, true)));

        given()
            .header("Authorization", "Bearer " + ADMIN_TOKEN)
        .when()
            .get("/api/users/administrators/" + adminId)
        .then()
            .statusCode(200)
            .body("administratorId", equalTo(1))
            .body("user.userId", equalTo(adminId.intValue()))
            .body("user.email", equalTo("admin@itesm.mx"));
    }

    @Test
    void getAdministratorByUserId_WhenNotFound_Returns404() throws Exception {
        when(firebaseTokenVerifier.verifyTokenAndGetUid(ADMIN_TOKEN)).thenReturn("uuid-admin-user");
        when(getAdministratorByUserIdUseCase.execute(99999L)).thenReturn(Optional.empty());

        given()
            .header("Authorization", "Bearer " + ADMIN_TOKEN)
        .when()
            .get("/api/users/administrators/99999")
        .then()
            .statusCode(404)
            .body("error", equalTo("Administrador no encontrado"));
    }
}
