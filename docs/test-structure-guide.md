# Test Structure Guide

This document explains the two testing patterns used in this project and provides step-by-step instructions for replicating them in any new Quarkus + Clean Architecture backend.

---

## Overview

The project enforces a strict separation between test types that mirrors the Clean Architecture layers.

| Pattern | File naming | Framework | What it tests |
|---|---|---|---|
| **Unit test** | `*Test.java` | JUnit 5 + Mockito | One use case class in isolation — no framework, no DB |
| **Integration test** | `*IntegrationTest.java` | Quarkus + REST Assured | A full vertical slice through the running app |

Integration tests are further divided into two sub-profiles depending on whether HTTP or direct DB access is needed.

---

## Dependencies

Add these to `pom.xml` with `<scope>test</scope>`:

```xml
<!-- Quarkus test runner + CDI context -->
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-junit</artifactId>
    <scope>test</scope>
</dependency>

<!-- @InjectMock — Mockito inside Quarkus CDI -->
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-junit-mockito</artifactId>
    <scope>test</scope>
</dependency>

<!-- HTTP-level assertions -->
<dependency>
    <groupId>io.rest-assured</groupId>
    <artifactId>rest-assured</artifactId>
    <scope>test</scope>
</dependency>

<!-- In-memory database -->
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-jdbc-h2</artifactId>
    <scope>test</scope>
</dependency>
```

---

## Pattern 1 — Unit Tests

### Purpose

Test a single use case class. Every dependency is replaced by a Mockito mock. No Quarkus, no database, no HTTP server — just plain Java executing in milliseconds.

### Key annotations

| Annotation | Target | What it does |
|---|---|---|
| `@ExtendWith(MockitoExtension.class)` | Class | Activates Mockito injection, skips Quarkus CDI entirely |
| `@Mock` | Field | Creates a Mockito mock of that type |
| `@InjectMocks` | Field | Instantiates the real use case and injects all `@Mock` fields into it |

### Template

```java
@ExtendWith(MockitoExtension.class)
class MyUseCaseTest {

    @Mock
    MyRepository myRepository;

    @Mock
    SomeExternalService externalService;

    @InjectMocks
    MyUseCase myUseCase;

    @Test
    void execute_WhenInputIsValid_ReturnsExpectedResult() {
        // 1. Arrange — configure mock behaviour
        when(myRepository.findById(1L)).thenReturn(Optional.of(someEntity));

        // 2. Act
        MyResponseDto result = myUseCase.execute(1L);

        // 3. Assert return values
        assertNotNull(result);
        assertEquals("expected", result.someField);

        // 4. Verify collaborator calls
        verify(myRepository).findById(1L);
        verifyNoMoreInteractions(myRepository);
    }

    @Test
    void execute_WhenInputIsNull_ThrowsIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> myUseCase.execute(null)
        );
        assertEquals("Expected error message", ex.getMessage());
        verifyNoInteractions(myRepository);
    }
}
```

### What to cover in unit tests

- **Happy path** — valid input produces the correct DTO with all fields mapped
- **Null / blank inputs** → `IllegalArgumentException`
- **Business rule violations** (duplicate email, forbidden state change, etc.) → `IllegalStateException`
- **External service failures** (Firebase throws, DB throws) → correct exception type is rethrown and any rollback calls are verified with `verify()`
- **DTO field mapping** — assert every field of the response individually

---

## Pattern 2 — Integration Tests

Integration tests boot a real Quarkus application against an H2 in-memory database. Firebase is always mocked because it is an external system. There are two sub-profiles with different responsibilities.

### The two test profiles

Both profiles must be created once and reused by every integration test class.

#### `H2TestProfile` — for REST resource tests

Place this file at `src/test/java/.../support/H2TestProfile.java`.

```java
public class H2TestProfile implements QuarkusTestProfile {
    @Override
    public Map<String, String> getConfigOverrides() {
        return Map.of(
            "quarkus.datasource.db-kind", "h2",
            "quarkus.datasource.username", "sa",
            "quarkus.datasource.password", "",
            "quarkus.datasource.jdbc.url",
                "jdbc:h2:mem:myapp_test;DB_CLOSE_DELAY=-1;MODE=MySQL",
            "quarkus.hibernate-orm.schema-management.strategy", "drop-and-create",
            "quarkus.hibernate-orm.sql-load-script", "no-file",
            "quarkus.hibernate-orm.log.sql", "true"
        );
    }
}
```

#### `UseCaseIntegrationTestProfile` — for use case integration tests

Place this file at `src/test/java/.../usecase/UseCaseIntegrationTestProfile.java`.

```java
public class UseCaseIntegrationTestProfile implements QuarkusTestProfile {
    @Override
    public Map<String, String> getConfigOverrides() {
        return Map.of(
            "quarkus.datasource.db-kind", "h2",
            "quarkus.datasource.username", "sa",
            "quarkus.datasource.password", "",
            "quarkus.datasource.jdbc.url",
                "jdbc:h2:mem:myapp_usecase_test;DB_CLOSE_DELAY=-1;MODE=MySQL",
            "quarkus.hibernate-orm.schema-management.strategy", "drop-and-create"
        );
    }
}
```

> The two profiles use **different in-memory database URLs** (`myapp_test` vs `myapp_usecase_test`). This prevents data leaking between test suites when both run in the same JVM.

Both profiles use `MODE=MySQL` so H2 behaves like MySQL — same dialect, same reserved-word rules, same type coercions.

---

### Sub-profile A: REST resource tests (`H2TestProfile`)

**Purpose:** Test the HTTP layer only. Verify authentication gates, role-based authorization, request validation, and response shapes. Use cases are mocked — no real business logic runs here.

#### Key annotations

| Annotation | Target | What it does |
|---|---|---|
| `@QuarkusTest` | Class | Boots the full Quarkus application |
| `@TestProfile(H2TestProfile.class)` | Class | Applies the H2 datasource overrides |
| `@InjectMock` | Field | Replaces a CDI bean with a Mockito mock inside the running app |
| `@Inject` | Field | Injects a real CDI bean (used only for seeding data) |
| `@BeforeEach @Transactional` | Method | Runs setup inside a transaction so DB writes are committed before the test |

#### Template

```java
@QuarkusTest
@TestProfile(H2TestProfile.class)
class MyResourceIntegrationTest {

    // Always mock Firebase — it's an external system
    @InjectMock
    FirebaseTokenVerifier firebaseTokenVerifier;

    @InjectMock
    FirebaseUserManager firebaseUserManager;

    // Mock use cases — the HTTP layer is the only thing under test here
    @InjectMock
    MyUseCase myUseCase;

    // Inject the real repository only to seed the users that FirebaseAuthFilter needs
    @Inject
    UserRepositoryImpl userRepository;

    private static final String ADMIN_TOKEN = "admin-token";
    private static final String USER_TOKEN  = "user-token";

    private Long adminId;
    private Long userId;

    @BeforeEach
    @Transactional
    void setup() {
        userRepository.deleteAll();

        UserEntity admin = new UserEntity();
        admin.firebaseUuid = "uuid-admin";
        admin.name = "Admin";
        admin.email = "admin@test.com";
        admin.roleId = 1;
        admin.isActive = true;
        userRepository.persist(admin);
        adminId = admin.userId;

        UserEntity user = new UserEntity();
        user.firebaseUuid = "uuid-user";
        user.name = "Regular User";
        user.email = "user@test.com";
        user.roleId = 2;
        user.isActive = true;
        userRepository.persist(user);
        userId = user.userId;
    }

    // 1. No auth header → 401
    @Test
    void get_WhenNoAuthHeader_Returns401() {
        given().when().get("/api/my-resource").then().statusCode(401);
    }

    // 2. Valid token but wrong role → 403
    @Test
    void get_WhenNonAdmin_Returns403() throws Exception {
        when(firebaseTokenVerifier.verifyTokenAndGetUid(USER_TOKEN)).thenReturn("uuid-user");

        given()
            .header("Authorization", "Bearer " + USER_TOKEN)
        .when()
            .get("/api/my-resource")
        .then()
            .statusCode(403)
            .body("error", equalTo("Solo un administrador puede hacer esto"));
    }

    // 3. Happy path
    @Test
    void get_WhenAdmin_Returns200AndBody() throws Exception {
        when(firebaseTokenVerifier.verifyTokenAndGetUid(ADMIN_TOKEN)).thenReturn("uuid-admin");
        when(myUseCase.execute()).thenReturn(List.of(new MyResponseDto(1L, "something")));

        given()
            .header("Authorization", "Bearer " + ADMIN_TOKEN)
        .when()
            .get("/api/my-resource")
        .then()
            .statusCode(200)
            .body("[0].id", equalTo(1))
            .body("[0].name", equalTo("something"));
    }

    // 4. Use-case exception → mapped HTTP error
    @Test
    void get_WhenNotFound_Returns404() throws Exception {
        when(firebaseTokenVerifier.verifyTokenAndGetUid(ADMIN_TOKEN)).thenReturn("uuid-admin");
        when(myUseCase.execute(99999L))
            .thenThrow(new IllegalStateException("No encontrado con id: 99999"));

        given()
            .header("Authorization", "Bearer " + ADMIN_TOKEN)
        .when()
            .get("/api/my-resource/99999")
        .then()
            .statusCode(404);
    }

    // 5. Invalid Firebase token → 401
    @Test
    void get_WhenInvalidToken_Returns401() throws Exception {
        FirebaseAuthException mockEx = mock(FirebaseAuthException.class);
        when(mockEx.getMessage()).thenReturn("Token inválido");
        when(firebaseTokenVerifier.verifyTokenAndGetUid("bad-token")).thenThrow(mockEx);

        given()
            .header("Authorization", "Bearer bad-token")
        .when()
            .get("/api/my-resource")
        .then()
            .statusCode(401);
    }
}
```

#### What to cover at this layer

- `401` — no `Authorization` header present
- `401` — Firebase rejects the token (mock `FirebaseAuthException`)
- `403` — authenticated but wrong role
- `400` — missing or invalid request body
- `200`/`201`/`204` — happy path, assert exact response JSON shape
- `4xx` — every exception type the resource catches mapped to its HTTP status

Do **not** test business rules here. That belongs to the use case tests.

---

### Sub-profile B: Use case integration tests (`UseCaseIntegrationTestProfile`)

**Purpose:** Test use cases wired to a real H2 database. Validates that queries, transactions, field mappings, and domain logic all work end-to-end against actual SQL. No HTTP server is involved.

#### Template

```java
@QuarkusTest
@TestProfile(UseCaseIntegrationTestProfile.class)
class MyUseCaseIntegrationTest {

    @Inject
    MyUseCase myUseCase;                    // real implementation

    @Inject
    MyEntityRepositoryImpl repository;      // real Panache repo for seeding and asserting

    @InjectMock
    FirebaseTokenVerifier firebaseTokenVerifier;

    @InjectMock
    FirebaseUserManager firebaseUserManager;

    @BeforeEach
    @Transactional
    void setup() {
        repository.deleteAll();

        MyEntity e = new MyEntity();
        e.name = "Seed Record";
        e.isActive = true;
        repository.persist(e);
    }

    // 1. Happy path — DTO fields mapped correctly
    @Test
    void execute_HappyPath_ReturnsMappedDto() {
        MyResponseDto result = myUseCase.execute();

        assertNotNull(result);
        assertEquals("Seed Record", result.name);
    }

    // 2. Persistence — data written by the use case is actually in the DB
    @Test
    void execute_PersistsRecordToDatabase() {
        myUseCase.create(new MyCreateDto("New Record"));

        assertEquals(2, repository.count());
    }

    // 3. Soft-delete — record still exists with flag flipped
    @Test
    void execute_SoftDelete_RecordRemainsWithInactiveFlag() {
        Long id = repository.listAll().get(0).id;

        myUseCase.deactivate(id);

        Optional<MyEntity> result = repository.findByIdOptional(id);
        assertTrue(result.isPresent());
        assertFalse(result.get().isActive);
    }

    // 4. Not found
    @Test
    void execute_WhenNotFound_ThrowsIllegalStateException() {
        assertThrows(IllegalStateException.class, () -> myUseCase.execute(99999L));
    }

    // 5. Validation guards
    @Test
    void execute_WhenIdIsNull_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> myUseCase.execute(null));
    }

    @Test
    void execute_WhenIdIsNegative_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> myUseCase.execute(-1L));
    }
}
```

#### What to cover at this layer

- **Persistence round-trips** — data written by the use case is readable from the repository
- **Filters and queries** — `findBy*` methods return exactly the right rows with the right values
- **Transactions** — a failure mid-use-case rolls back the entire operation
- **Soft-delete** — records remain in the DB with the flag flipped, not hard-deleted
- **Field mapping** — domain model → DTO fields are all correct
- **Edge cases** — empty table, duplicate key, not-found, invalid IDs

---

## The Three-Layer Testing Pyramid

```
          ┌─────────────────────────────────────┐
          │       REST Integration Tests         │
          │       H2TestProfile                  │
          │  - HTTP status codes                 │
          │  - Auth/role gates                   │
          │  - Response JSON shape               │
          │  - Use cases are MOCKED              │
          └─────────────────────────────────────┘
         ┌───────────────────────────────────────┐
         │     Use Case Integration Tests         │
         │     UseCaseIntegrationTestProfile      │
         │  - Real DB queries                    │
         │  - Transactions and rollbacks         │
         │  - Field mapping end-to-end           │
         │  - Firebase is MOCKED                 │
         └───────────────────────────────────────┘
        ┌─────────────────────────────────────────┐
        │             Unit Tests                   │
        │             MockitoExtension             │
        │  - Business logic and rules             │
        │  - Exception paths and rollbacks        │
        │  - DTO mapping from domain model        │
        │  - Everything is MOCKED                 │
        └─────────────────────────────────────────┘
```

Each new feature requires one test class at **each level**:

1. A unit test for the use case (`*Test.java`)
2. A use case integration test for DB behavior (`*IntegrationTest.java` with `UseCaseIntegrationTestProfile`)
3. A REST resource integration test for HTTP behavior (`*IntegrationTest.java` with `H2TestProfile`)

---

## Rules to Follow in Every Test

| Rule | Why |
|---|---|
| `@BeforeEach` must be `@Transactional` when writing to the DB directly | Panache writes outside a transaction are silently ignored or throw |
| Add `@Transactional` to a `@Test` method only when the test body itself calls `deleteAll()`, `persist()`, or similar | The use case under test manages its own transaction — wrapping it again can mask rollback behavior |
| Never rely on `import.sql` for test data | Seed in `@BeforeEach` so every test starts from a known, isolated state |
| Always mock `FirebaseTokenVerifier` and `FirebaseUserManager` in every integration test | They make real network calls; mocking them keeps tests fast and deterministic |
| Use `verifyNoInteractions(repo)` in unit tests that should throw early | Confirms no repository or service was called when validation rejects the input |
| Name tests as `methodName_WhenCondition_ExpectedOutcome` | Makes failures self-documenting without reading the body |

---

## Quick-Start Checklist for a New Feature

- [ ] Create `MyUseCase.java` in `application/usecase/`
- [ ] Create `MyUseCaseTest.java` — unit test with `@ExtendWith(MockitoExtension.class)`
- [ ] Create `MyUseCaseIntegrationTest.java` — with `@TestProfile(UseCaseIntegrationTestProfile.class)`
- [ ] Create `MyResource.java` in `interfaces/rest/`
- [ ] Create `MyResourceIntegrationTest.java` — with `@TestProfile(H2TestProfile.class)`
- [ ] `H2TestProfile` and `UseCaseIntegrationTestProfile` already exist — reuse them, do not create new ones
- [ ] Run `./mvnw verify -DskipITs=false` to confirm all integration tests pass
