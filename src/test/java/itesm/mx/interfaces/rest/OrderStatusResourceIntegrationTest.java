package itesm.mx.interfaces.rest;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import itesm.mx.application.dto.OrderStatusResponseDto;
import itesm.mx.application.usecase.order.GetAllOrderStatusesUseCase;
import itesm.mx.infrastructure.firebase.FirebaseTokenVerifier;
import itesm.mx.infrastructure.firebase.FirebaseUserManager;
import itesm.mx.infrastructure.persistence.entity.users.UserEntity;
import itesm.mx.infrastructure.persistence.repository.user.UserRepositoryImpl;
import itesm.mx.support.H2TestProfile;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.Mockito.when;

@QuarkusTest
@TestProfile(H2TestProfile.class)
class OrderStatusResourceIntegrationTest {

    @InjectMock FirebaseTokenVerifier firebaseTokenVerifier;
    @InjectMock FirebaseUserManager firebaseUserManager;
    @InjectMock GetAllOrderStatusesUseCase getAllOrderStatusesUseCase;

    @Inject UserRepositoryImpl userRepository;

    private static final String USER_TOKEN = "user-token";

    @BeforeEach
    @Transactional
    void setup() {
        userRepository.deleteAll();

        UserEntity user = new UserEntity();
        user.firebaseUuid = "uuid-os-user";
        user.name = "Regular User";
        user.email = "osuser@itesm.mx";
        user.roleId = 2;
        user.isActive = true;
        userRepository.persist(user);
    }

    @Test
    void getAllOrderStatuses_WhenNoAuthHeader_Returns401() {
        given()
        .when()
            .get("/api/order-statuses")
        .then()
            .statusCode(401);
    }

    @Test
    void getAllOrderStatuses_WhenAuthenticated_Returns200WithList() throws Exception {
        when(firebaseTokenVerifier.verifyTokenAndGetUid(USER_TOKEN)).thenReturn("uuid-os-user");
        when(getAllOrderStatusesUseCase.execute()).thenReturn(List.of(
                new OrderStatusResponseDto(1L, "Pendiente"),
                new OrderStatusResponseDto(2L, "Confirmado")
        ));

        given()
            .header("Authorization", "Bearer " + USER_TOKEN)
        .when()
            .get("/api/order-statuses")
        .then()
            .statusCode(200)
            .body("size()", equalTo(2))
            .body("[0].orderStatusId", equalTo(1))
            .body("[0].estado", equalTo("Pendiente"));
    }
}
