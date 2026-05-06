package itesm.mx.interfaces.rest;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;
import itesm.mx.application.dto.RegisterProviderDto;
import itesm.mx.application.dto.UpdateProviderDto;
import itesm.mx.application.usecase.marketplace.provider.DeleteProviderUseCase;
import itesm.mx.application.usecase.marketplace.provider.GetAllProvidersUseCase;
import itesm.mx.application.usecase.marketplace.provider.GetProviderByIdUseCase;
import itesm.mx.application.usecase.marketplace.provider.GetProvidersByUserUseCase;
import itesm.mx.application.usecase.marketplace.provider.RegisterProviderUseCase;
import itesm.mx.application.usecase.marketplace.provider.UpdateProviderUseCase;
import itesm.mx.domain.models.marketplace.Provider;
import itesm.mx.domain.models.user.User;
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
import java.util.Optional;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@QuarkusTest
@TestProfile(H2TestProfile.class)
class ProviderResourceIntegrationTest {

    private static final String SELLER_UID = "provider-seller-uuid";
    private static final String SELLER_EMAIL = "provider-seller@test.com";
    private static final String SELLER2_UID = "provider-seller2-uuid";
    private static final String SELLER2_EMAIL = "provider-seller2@test.com";
    private static final String ADMIN_UID = "provider-admin-uuid";
    private static final String ADMIN_EMAIL = "provider-admin@test.com";
    private static final String FARMER_UID = "provider-farmer-uuid";
    private static final String FARMER_EMAIL = "provider-farmer@test.com";

    @InjectMock FirebaseTokenVerifier firebaseTokenVerifier;
    @InjectMock FirebaseUserManager firebaseUserManager;

    @InjectMock RegisterProviderUseCase registerProviderUseCase;
    @InjectMock UpdateProviderUseCase updateProviderUseCase;
    @InjectMock DeleteProviderUseCase deleteProviderUseCase;
    @InjectMock GetAllProvidersUseCase getAllProvidersUseCase;
    @InjectMock GetProviderByIdUseCase getProviderByIdUseCase;
    @InjectMock GetProvidersByUserUseCase getProvidersByUserUseCase;

    @Inject UserRepositoryImpl userRepository;

    @BeforeEach
    @Transactional
    void setup() {
        userRepository.deleteAll();

        UserEntity seller = new UserEntity();
        seller.firebaseUuid = SELLER_UID;
        seller.name = "Provider Seller";
        seller.email = SELLER_EMAIL;
        seller.roleId = 3;
        userRepository.persist(seller);

        UserEntity seller2 = new UserEntity();
        seller2.firebaseUuid = SELLER2_UID;
        seller2.name = "Provider Seller 2";
        seller2.email = SELLER2_EMAIL;
        seller2.roleId = 3;
        userRepository.persist(seller2);

        UserEntity admin = new UserEntity();
        admin.firebaseUuid = ADMIN_UID;
        admin.name = "Provider Admin";
        admin.email = ADMIN_EMAIL;
        admin.roleId = 1;
        userRepository.persist(admin);

        UserEntity farmer = new UserEntity();
        farmer.firebaseUuid = FARMER_UID;
        farmer.name = "Provider Farmer";
        farmer.email = FARMER_EMAIL;
        farmer.roleId = 2;
        userRepository.persist(farmer);
    }

    private User domainUser(Long id) {
        User u = new User();
        u.setUserId(id);
        return u;
    }

    private Long sellerUserId() {
        return userRepository.findByFirebaseUuid(SELLER_UID).orElseThrow().getUserId();
    }

    private Long seller2UserId() {
        return userRepository.findByFirebaseUuid(SELLER2_UID).orElseThrow().getUserId();
    }

    @Test
    void mainFlow_RegisterListUpdateDeleteAsSeller() throws Exception {
        String token = "valid-provider-token";
        when(firebaseTokenVerifier.verifyTokenAndGetUid(token)).thenReturn(SELLER_UID);

        Long sellerId = sellerUserId();

        // 1) POST register
        RegisterProviderDto registerDto = new RegisterProviderDto();
        registerDto.name = "AgroSupplier";

        Provider created = new Provider(11L, domainUser(sellerId), "AgroSupplier");
        when(registerProviderUseCase.execute(any(Provider.class))).thenReturn(created);

        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body(registerDto)
        .when()
            .post("/api/providers")
        .then()
            .statusCode(201)
            .body("providerId", equalTo(11))
            .body("name", equalTo("AgroSupplier"))
            .body("userId", equalTo(sellerId.intValue()));

        // 2) GET list (no param) — both ADMIN and SELLER see all
        when(getAllProvidersUseCase.execute()).thenReturn(List.of(created));

        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .get("/api/providers")
        .then()
            .statusCode(200)
            .body("[0].providerId", equalTo(11))
            .body("[0].name", equalTo("AgroSupplier"));

        // 3) PUT update — SELLER updating own provider
        UpdateProviderDto updateDto = new UpdateProviderDto();
        updateDto.name = "AgroSupplier Renamed";

        Provider existing = new Provider(11L, domainUser(sellerId), "AgroSupplier");
        when(getProviderByIdUseCase.execute(11L)).thenReturn(Optional.of(existing));

        Provider updated = new Provider(11L, domainUser(sellerId), "AgroSupplier Renamed");
        when(updateProviderUseCase.execute(eq(11L), any(Provider.class))).thenReturn(updated);

        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body(updateDto)
        .when()
            .put("/api/providers/11")
        .then()
            .statusCode(200)
            .body("providerId", equalTo(11))
            .body("name", equalTo("AgroSupplier Renamed"));

        // 4) DELETE — SELLER is forbidden, only ADMIN can delete
        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .delete("/api/providers/11")
        .then()
            .statusCode(403);
    }

    @Test
    void getProviders_asFarmer_returns403() throws Exception {
        String token = "farmer-token";
        when(firebaseTokenVerifier.verifyTokenAndGetUid(token)).thenReturn(FARMER_UID);

        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .get("/api/providers")
        .then()
            .statusCode(403);
    }

    @Test
    void getProvidersByUserId_asAdmin_returns200() throws Exception {
        String token = "admin-token";
        when(firebaseTokenVerifier.verifyTokenAndGetUid(token)).thenReturn(ADMIN_UID);

        Long sellerId = sellerUserId();
        List<Provider> providers = List.of(new Provider(1L, domainUser(sellerId), "AgroSupplier"));
        when(getProvidersByUserUseCase.execute(sellerId)).thenReturn(providers);

        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .get("/api/providers?userId=" + sellerId)
        .then()
            .statusCode(200)
            .body("[0].providerId", equalTo(1))
            .body("[0].name", equalTo("AgroSupplier"))
            .body("[0].userId", equalTo(sellerId.intValue()));
    }

    @Test
    void getProvidersByUserId_asSellerWithOtherUserId_returns403() throws Exception {
        String token = "seller2-token";
        when(firebaseTokenVerifier.verifyTokenAndGetUid(token)).thenReturn(SELLER2_UID);

        Long sellerId = sellerUserId();

        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .get("/api/providers?userId=" + sellerId)
        .then()
            .statusCode(403);
    }

    @Test
    void updateProvider_asSellerNotOwner_returns403() throws Exception {
        String token = "seller2-token";
        when(firebaseTokenVerifier.verifyTokenAndGetUid(token)).thenReturn(SELLER2_UID);

        Long sellerId = sellerUserId();
        Provider existing = new Provider(11L, domainUser(sellerId), "AgroSupplier");
        when(getProviderByIdUseCase.execute(11L)).thenReturn(Optional.of(existing));

        UpdateProviderDto dto = new UpdateProviderDto();
        dto.name = "AgroSupplier Renamed";

        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body(dto)
        .when()
            .put("/api/providers/11")
        .then()
            .statusCode(403);
    }

    @Test
    void deleteProvider_asAdmin_returns204() throws Exception {
        String token = "admin-token";
        when(firebaseTokenVerifier.verifyTokenAndGetUid(token)).thenReturn(ADMIN_UID);

        when(deleteProviderUseCase.execute(11L)).thenReturn(true);

        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .delete("/api/providers/11")
        .then()
            .statusCode(204);
    }
}