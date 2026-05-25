package itesm.mx.interfaces.rest;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import itesm.mx.support.H2TestProfile;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;

@QuarkusTest
@TestProfile(H2TestProfile.class)
class StatusResourceIntegrationTest {

    @Test
    void getStatus_Returns200AndUpState() {
        given()
        .when()
            .get("/api/status")
        .then()
            .statusCode(200)
            .body("status", equalTo("UP"))
            .body("database", equalTo("UP"))
            .body("service", equalTo("back-plaguie"));
    }
}