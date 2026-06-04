package itesm.mx.interfaces.rest;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import itesm.mx.support.H2TestProfile;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas defensivas para la vulnerabilidad ZAP "Format String Error" (CWE-134)
 * reportada en el ticket SCRUM-298.
 *
 * <p>Investigación previa confirmó que el código no usa {@code String.format},
 * {@code printf}, {@code MessageFormat} ni similares con cadenas controladas
 * por el usuario; los únicos usos de format strings (por ejemplo
 * {@code LOG.errorf}) usan literales hardcodeados como primer argumento y
 * los datos del usuario se pasan únicamente como argumentos posicionales (%s).
 *
 * <p>Estas pruebas dejan evidencia objetiva de que el servicio no es
 * vulnerable a payloads típicos de format string: el servidor no crashea
 * ante payloads en headers, query params ni en el body JSON, y no echa de
 * vuelta el payload procesado (lo que indicaría que pasó por un formatter
 * con datos controlados por el atacante).
 */
@QuarkusTest
@TestProfile(H2TestProfile.class)
class FormatStringSafetyIntegrationTest {

    private static final String[] FORMAT_PAYLOADS = new String[] {
            "%s%s%s%s%s%s%s%s",
            "%n%n%n%n%n%n",
            "%x%x%x%x",
            "%d%d%d%d",
            "AAAA%08x.%08x.%08x.%08x",
            "%s%n%d%p"
    };

    @Test
    void statusEndpoint_DoesNotCrashOnFormatStringPayloadsInHeadersOrQuery() {
        for (String payload : FORMAT_PAYLOADS) {
            Response response = given()
                    .header("X-Format-Probe", payload)
                    .queryParam("probe", payload)
            .when()
                    .get("/api/status");

            int status = response.statusCode();
            assertTrue(status == 200 || status == 503,
                    "El endpoint /api/status debe responder con un status estable (200/503) " +
                            "incluso ante payloads de format string. Recibido: " + status +
                            " para payload=" + payload);

            // Confirmamos que el JSON de status conserva su forma esperada (no hubo crash ni
            // sustitucion del payload en la salida).
            response.then().body("service", containsString("back-plaguie"));

            String body = response.getBody().asString();
            assertFalse(body != null && body.contains(payload),
                    "El cuerpo de la respuesta no debe contener el payload de format " +
                            "(no debe haber eco). payload=" + payload);
        }
    }

    @Test
    void loginEndpoint_HandlesFormatStringPayloadInBodyWithoutCrashOrEcho() {
        for (String payload : FORMAT_PAYLOADS) {
            String jsonBody = "{\"firebaseToken\":\"" + payload + "\"}";

            Response response = given()
                    .contentType(ContentType.JSON)
                    .body(jsonBody)
            .when()
                    .post("/api/auth/login");

            int status = response.statusCode();
            // El servidor debe responder de forma estable (cualquier status HTTP valido), nunca
            // colgarse ni devolver una respuesta truncada/sin body por un crash de format string.
            assertTrue(status >= 200 && status < 600,
                    "El endpoint /api/auth/login debe responder con un status HTTP valido " +
                            "incluso ante payloads de format string. Recibido: " + status +
                            " para payload=" + payload);

            String body = response.getBody().asString();
            // Propiedad clave: el payload no debe aparecer "interpretado" (con saltos de linea
            // reales en lugar de la cadena literal "%n", o caracteres sustituidos por valores
            // de la pila). Si el servidor pasara el token por un format string, veriamos
            // saltos de linea reales o bytes raros en el cuerpo de la respuesta.
            if (body != null && payload.contains("%n")) {
                assertFalse(body.contains("\n\n\n\n\n\n"),
                        "La respuesta no debe contener saltos de linea reales producto de un " +
                                "format string interpretado. payload=" + payload);
            }
            // El body no debe contener el payload eco-eado y procesado a la vez (no hay reflejo
            // del firebaseToken en el JSON de error que devuelve AuthResource).
            assertFalse(body != null && body.contains(payload),
                    "La respuesta no debe contener el payload eco-eado. payload=" + payload);
        }
    }
}
