package itesm.mx.application.usecase;

import io.quarkus.test.junit.QuarkusTestProfile;

import java.util.Map;

public class UseCaseIntegrationTestProfile implements QuarkusTestProfile {

    @Override
    public Map<String, String> getConfigOverrides() {
        return Map.of(
                "quarkus.datasource.db-kind", "h2",
                "quarkus.datasource.username", "sa",
                "quarkus.datasource.password", "",
                "quarkus.datasource.jdbc.url", "jdbc:h2:mem:plaguie_usecase_test;DB_CLOSE_DELAY=-1;MODE=MySQL",
                "quarkus.hibernate-orm.schema-management.strategy", "drop-and-create"
        );
    }
}
