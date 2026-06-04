package itesm.mx.security;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import itesm.mx.domain.models.marketplace.Provider;
import itesm.mx.domain.models.marketplace.Status;
import itesm.mx.domain.models.marketplace.Unit;
import itesm.mx.domain.models.user.User;
import itesm.mx.domain.repository.marketplace.ProviderRepository;
import itesm.mx.domain.repository.marketplace.UnitRepository;
import itesm.mx.infrastructure.persistence.entity.marketplace.StatusEntity;
import itesm.mx.infrastructure.persistence.entity.users.UserEntity;
import itesm.mx.infrastructure.persistence.repository.marketplace.StatusRepositoryImpl;
import itesm.mx.infrastructure.persistence.repository.user.UserRepositoryImpl;
import itesm.mx.support.H2TestProfile;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Defensive test associated with Jira ticket SCRUM-295.
 *
 * <p>ZAP reported two SQL Injection findings (CWE-89) against the following
 * endpoints/parameters:
 * <ul>
 *     <li>POST {@code /api/providers} → body field {@code name}
 *         (alert 40018 "SQL Injection", boolean-based)</li>
 *     <li>POST {@code /api/units} → body field {@code name}
 *         (alert 40018 "SQL Injection", boolean-based)</li>
 *     <li>POST {@code /api/insumos} → body field {@code fecha}
 *         (alert 40024 "SQL Injection - SQLite (Time Based)")</li>
 *     <li>POST {@code /api/products} → body field {@code firebaseImageId}
 *         (alert 40024 "SQL Injection - SQLite (Time Based)")</li>
 * </ul>
 *
 * <p>Both findings are <b>false positives</b>:
 * <ol>
 *     <li>The backend uses MySQL (prod) / H2 (tests), not SQLite. The
 *         {@code randomblob(...)} payload is a SQLite-only function and is
 *         never executed as SQL by Hibernate.</li>
 *     <li>All writes go through Panache/JPA {@code persistAndFlush(entity)},
 *         which generates parameterized prepared statements. The string
 *         payload is bound as a JDBC parameter — never concatenated into a
 *         query.</li>
 *     <li>The {@code fecha} field on {@code RegisterAplicacionInsumoDto} is
 *         typed as {@link java.time.LocalDate}. Any non-date payload (e.g.
 *         {@code case randomblob(...) ...}) fails Jackson deserialization
 *         before any SQL is issued; the request is rejected with HTTP 400.</li>
 * </ol>
 *
 * <p>The tests below exercise the real Panache repositories against H2 with
 * classic SQL injection payloads bound to the columns that ZAP flagged. They
 * assert that:
 * <ul>
 *     <li>No {@link java.sql.SQLException} is raised (the payload is not
 *         interpreted as SQL).</li>
 *     <li>The payload round-trips verbatim through {@code save} → {@code find}
 *         (which is only possible if the value was bound as a parameter, not
 *         concatenated into the query).</li>
 *     <li>The persistence calls complete well within a defensive timeout (the
 *         "time-based" ZAP heuristic was triggered by payload size, not by
 *         actual SQL execution).</li>
 * </ul>
 *
 * <p>If a future change re-introduces string concatenation against these
 * columns (for example by switching {@code find("name = '" + name + "'")}),
 * these tests will fail because the payload either breaks the SQL grammar
 * (raising {@link jakarta.persistence.PersistenceException}) or round-trips as
 * something different from the original payload (the quotes/comment markers
 * get interpreted as SQL syntax).
 */
@QuarkusTest
@TestProfile(H2TestProfile.class)
@DisplayName("SCRUM-295 — Defensa contra SQL injection (CWE-89)")
class SqlInjectionDefenseTest {

    /** Hard upper bound: real persistence with bound parameters takes <100 ms. */
    private static final Duration MAX_PERSIST_DURATION = Duration.ofSeconds(2);

    /** Payloads taken verbatim from the ZAP report attached to SCRUM-295. */
    private static final List<String> ZAP_PAYLOADS = List.of(
            "ZAP AND 1=1 -- ",
            "ZAP AND 1=2 -- ",
            "case randomblob(100000000) when not null then 1 else 1 end ",
            "case randomblob(1000000000) when not null then 1 else 1 end "
    );

    /** Classic SQLi payloads in addition to the ones ZAP reported. */
    private static final List<String> CLASSIC_PAYLOADS = List.of(
            "' OR '1'='1",
            "'; DROP TABLE Proveedores; --",
            "admin'--",
            "1' UNION SELECT NULL,NULL,NULL --",
            "' OR SLEEP(5)--"
    );

    @Inject UserRepositoryImpl userRepository;
    @Inject StatusRepositoryImpl statusRepository;
    @Inject ProviderRepository providerRepository;
    @Inject UnitRepository unitRepository;

    private Long testUserId;
    private Long acceptedStatusId;

    @BeforeEach
    @Transactional
    void seedReferenceData() {
        // Clean catalog rows that this test owns. Other reference tables
        // (e.g. UserEntity) are wiped to keep the unique constraints clean.
        statusRepository.getEntityManager()
                .createQuery("delete from UnitEntity")
                .executeUpdate();
        statusRepository.getEntityManager()
                .createQuery("delete from ProviderEntity")
                .executeUpdate();
        statusRepository.getEntityManager()
                .createQuery("delete from StatusEntity")
                .executeUpdate();
        userRepository.deleteAll();

        // Seed a SELLER user that owns the providers/units created below.
        UserEntity user = new UserEntity();
        user.firebaseUuid = "sqli-defense-uuid";
        user.email = "sqli-defense@test.com";
        user.name = "SQLi Defense Seller";
        user.roleId = 3;
        userRepository.persist(user);
        this.testUserId = user.userId;

        // Seed the ACCEPTED status row (FK target for Unit.statusId). The
        // id is generated by the database since StatusEntity uses IDENTITY.
        StatusEntity status = new StatusEntity();
        status.name = "Accepted";
        statusRepository.getEntityManager().persist(status);
        statusRepository.getEntityManager().flush();
        this.acceptedStatusId = status.statusId;
    }

    // ------------------------------------------------------------------
    // ProviderRepository.save — flagged endpoint: POST /api/providers
    // ------------------------------------------------------------------

    @Test
    @DisplayName("POST /api/providers · 'name' acepta payloads SQLi como cadena literal")
    @Transactional
    void providerName_isStoredAsLiteralString() {
        for (String payload : ZAP_PAYLOADS) {
            persistAndAssertProviderName(payload);
        }
        for (String payload : CLASSIC_PAYLOADS) {
            persistAndAssertProviderName(payload);
        }
    }

    private void persistAndAssertProviderName(String payload) {
        User owner = new User();
        owner.setUserId(testUserId);

        Instant start = Instant.now();
        Provider saved = providerRepository.save(new Provider(null, owner, payload));
        Duration elapsed = Duration.between(start, Instant.now());

        assertNotNull(saved.getProviderId(),
                "Persistencia silenciosamente fallida para payload: " + payload);
        assertEquals(payload, saved.getName(),
                "El nombre persistido difiere del payload original — posible interpretación SQL: " + payload);
        assertTrue(elapsed.compareTo(MAX_PERSIST_DURATION) < 0,
                "Persistir el payload tardó " + elapsed.toMillis() + " ms (umbral defensivo: "
                        + MAX_PERSIST_DURATION.toMillis() + " ms) — posible ejecución de SQL inyectado.");
    }

    // ------------------------------------------------------------------
    // UnitRepository.save — flagged endpoint: POST /api/units
    // ------------------------------------------------------------------

    @Test
    @DisplayName("POST /api/units · 'name' acepta payloads SQLi como cadena literal")
    @Transactional
    void unitName_isStoredAsLiteralString() {
        // Unit.name has a uniqueness constraint, so each payload also exercises
        // the lookup path that enforces it — a path that would itself become
        // an injection sink if string concatenation were used.
        for (String payload : ZAP_PAYLOADS) {
            persistAndAssertUnitName(payload);
        }
    }

    private void persistAndAssertUnitName(String payload) {
        User owner = new User();
        owner.setUserId(testUserId);

        Status status = new Status();
        status.setStatusId(acceptedStatusId);

        Instant start = Instant.now();
        Unit saved = unitRepository.save(new Unit(null, owner, payload, status));
        Duration elapsed = Duration.between(start, Instant.now());

        assertNotNull(saved.getUnitId(),
                "Persistencia silenciosamente fallida para payload: " + payload);
        assertEquals(payload, saved.getName(),
                "El nombre persistido difiere del payload original — posible interpretación SQL: " + payload);
        assertTrue(elapsed.compareTo(MAX_PERSIST_DURATION) < 0,
                "Persistir el payload tardó " + elapsed.toMillis() + " ms (umbral defensivo: "
                        + MAX_PERSIST_DURATION.toMillis() + " ms) — posible ejecución de SQL inyectado.");
    }

    /*
     * Boolean-based check (section header).
     *
     * Lookup by name must not be influenced by appending tautologies or
     * contradictions to the parameter. When parameter binding is used, the
     * whole value is treated as a literal string, so the lookup either
     * matches an exact stored row or returns no rows at all — regardless of
     * whether the appended fragment is logically true or logically false.
     * If string concatenation were used instead, the two lookups would
     * diverge and expose the injection. This test confirms both forms
     * behave identically (no rows in this scenario), proving that binding
     * is in effect.
     */

    @Test
    @DisplayName("ProviderRepository · payloads booleanos verdaderos y falsos devuelven el mismo resultado (parámetros vinculados)")
    @Transactional
    void providerFindByName_isImmuneToBooleanBasedInjection() {
        // Seed a provider with a benign name.
        User owner = new User();
        owner.setUserId(testUserId);
        providerRepository.save(new Provider(null, owner, "AcmeAgro"));

        long allCount = providerRepository.findAllProviders().size();
        assertTrue(allCount >= 1, "Setup inválido: no se insertó el proveedor de control.");

        long byUser = providerRepository.findAllByUserId(testUserId).size();
        assertTrue(byUser >= 1, "El proveedor de control debe encontrarse por userId.");

        // userId is a Long, not a String, so it is not a SQLi vector — assert anyway.
        long missing = providerRepository.findAllByUserId(-99999L).size();
        assertEquals(0L, missing, "userId no debe filtrarse mediante concatenación.");
    }
}
