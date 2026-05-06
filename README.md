# back-plaguie

![Quarkus](https://img.shields.io/badge/Quarkus-3.x-4695EB?logo=quarkus&logoColor=white)
![Java](https://img.shields.io/badge/Java-17-ED8B00?logo=openjdk&logoColor=white)
![Firebase](https://img.shields.io/badge/Firebase-Auth-FFCA28?logo=firebase&logoColor=black)
![MySQL](https://img.shields.io/badge/MySQL-8.x-4479A1?logo=mysql&logoColor=white)
![GCP](https://img.shields.io/badge/GCP-Cloud_Run-4285F4?logo=googlecloud&logoColor=white)
![Cloud Build](https://img.shields.io/badge/Cloud_Build-CI%2FCD-4285F4?logo=googlecloud&logoColor=white)

Backend REST API for **Plaguie** — an AgTech B2B platform that connects farmers with agrochemical technical sellers and enables phytosanitary monitoring (vigilancia fitosanitaria) across agricultural regions in Mexico.

---

## Table of Contents

- [Project Overview](#project-overview)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Prerequisites](#prerequisites)
- [Local Setup](#local-setup)
- [Commands](#commands)
- [Authentication & Roles](#authentication--roles)
- [REST Endpoints](#rest-endpoints)
- [Testing](#testing)
- [CI/CD Pipeline](#cicd-pipeline)

---

## Project Overview

Plaguie's backend provides a secure REST API consumed by a React frontend (`http://localhost:5173`). Its core responsibilities are:

- **User management** — registration, authentication, and role-based access for farmers, technical sellers, and administrators.
- **Location management** — GPS-based location registration linked to a four-level geographic catalog (State → Municipality → Locality → Property).
- **Phytosanitary surveillance** — CRUD operations for monitoring observations (`VigilanciaFitosanitaria`) referencing pest, host, and variety catalogs.

Authentication is delegated entirely to **Firebase Auth**. No JWT is issued by this service — Firebase ID tokens are verified on every protected request.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Runtime | Quarkus 3.x on Java 17 |
| Persistence | Hibernate ORM with Panache, MySQL 8 (JDBC), JTS for spatial types |
| Auth | Firebase Admin SDK (token verification) |
| Validation | Jakarta Bean Validation |
| API Docs | SmallRye OpenAPI / Swagger UI |
| Build | Apache Maven Wrapper (`./mvnw`) |
| Container | Docker (runtime-only image) |
| Cloud | GCP — Artifact Registry + Cloud Run |
| CI/CD | Google Cloud Build |

---

## Architecture

The project follows **Clean Architecture** with strict unidirectional dependencies:

```
interfaces/rest/   ──►  application/   ──►  domain/
                                              ▲
                        infrastructure/ ──────┘
```

| Package | Role |
|---|---|
| `domain/` | Pure Java models and repository interfaces. No framework dependencies. |
| `application/` | Use cases (one class per operation), DTOs, mappers, security context. |
| `infrastructure/` | Quarkus/Panache persistence, Firebase integration, Jackson customizations. |
| `interfaces/rest/` | JAX-RS resources — request/response handling and authorization checks only. |

**Key rules:**
- Domain models are plain Java — no JPA, Quarkus, or validation annotations.
- Repository interfaces live in `domain/`; implementations (`*RepositoryImpl`) live in `infrastructure/` using Panache.
- Use cases are `@ApplicationScoped`; all DB-mutating ones are `@Transactional`.
- REST resources must not contain business logic — that stays in use cases.

---

## Prerequisites

- **Java 17** (JDK — e.g. Eclipse Temurin 17)
- **Maven** (or use the included `./mvnw` wrapper)
- **MySQL 8** running locally with a `plaguie_db` database
- **Firebase project** with a service account JSON (download from Firebase Console → Project Settings → Service Accounts)
- **GraalVM** (optional — only for native builds)

---

## Local Setup

### 1. Clone and configure environment

Copy the example env file and fill in your values:

```bash
cp .env.example .env
```

| Variable | Dev Default | Description |
|---|---|---|
| `QUARKUS_DATASOURCE_USERNAME` | `root` | MySQL username |
| `QUARKUS_DATASOURCE_PASSWORD` | — | MySQL password |
| `QUARKUS_DATASOURCE_JDBC_URL` | `jdbc:mysql://localhost:3306/plaguie_db` | MySQL JDBC URL |
| `FIREBASE_SERVICE_ACCOUNT_LOCATION` | `src/main/resources/firebase-dev.json` | Path to Firebase service account JSON |

### 2. Place Firebase credentials

Put your Firebase service account JSON at the path specified by `FIREBASE_SERVICE_ACCOUNT_LOCATION` (default: `src/main/resources/firebase-dev.json`).

### 3. Start MySQL

Ensure a MySQL 8 instance is running and the `plaguie_db` database exists. In dev mode, Quarkus uses `drop-and-create` and seeds the database automatically from `src/main/resources/import.sql`.

### 4. Run in dev mode

```bash
./mvnw quarkus:dev
```

The API is available at `http://localhost:8080` and the Dev UI at `http://localhost:8080/q/dev/`.
Swagger UI is accessible at `http://localhost:8080/q/swagger-ui/`.

---

## Commands

```bash
# Start with live reload (Dev UI at http://localhost:8080/q/dev/)
./mvnw quarkus:dev

# Build JAR
./mvnw package

# Run unit tests only
./mvnw test

# Run a single test class
./mvnw test -Dtest=LoginUseCaseTest

# Run integration tests (H2 in-memory, Firebase mocked)
./mvnw verify -DskipITs=false

# Run a single integration test class
./mvnw verify -DskipITs=false -Dit.test=AuthResourceIntegrationTest

# Build native executable (requires GraalVM)
./mvnw package -Dnative

# Build native in container (no GraalVM required)
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

---

## Authentication & Roles

### Authentication Flow

Every request except `POST /api/auth/login` and `POST /api/auth/signup` passes through `FirebaseAuthFilter`:

1. Extracts the `Bearer` token from the `Authorization` header.
2. Verifies it via `FirebaseTokenVerifier` (wraps Firebase Admin SDK).
3. Looks up the user in the local DB by `firebaseUuid`.
4. Populates the `@RequestScoped` `AuthenticatedUserContext` with the current user.

Resources check `authenticatedUserContext.getCurrentUser()` and `getRoleId()` manually — there is no declarative security.

### Role System

| Role | Value | Sub-entity Table |
|---|---|---|
| `ADMIN` | `1` | `Administrador` |
| `FARMER` | `2` | `Agricultor` |
| `SELLER` | `3` | `Tecnico_Vendedor` |

Each role has its own sub-entity table that references `Usuario`. Non-admin roles also reference a `Ubicacion` (location).

### User Registration

- `POST /api/auth/register` (admin-only): creates the Firebase Auth user first, then the local DB record and role sub-entity. Firebase user is rolled back if the DB save fails.
- `POST /api/auth/signup` (public): registers a `SELLER` with no location — intended for self-registration.

---

## REST Endpoints

| Resource | Method | Path | Auth | Admin Only |
|---|---|---|---|---|
| `AuthResource` | POST | `/api/auth/login` | No | — |
| `AuthResource` | POST | `/api/auth/signup` | No | — |
| `AuthResource` | POST | `/api/auth/register` | Yes | Yes |
| `UserResource` | GET | `/api/users` | Yes | Yes |
| `UserResource` | GET | `/api/users/{id}` | Yes | Admin or self |
| `UserResource` | PUT | `/api/users/{id}` | Yes | Yes |
| `UserResource` | DELETE | `/api/users/{id}` | Yes | Yes (soft-delete) |
| `SubUsersResource` | GET | `/api/users/farmers` | Yes | Yes |
| `SubUsersResource` | GET | `/api/users/technical-sellers` | Yes | Yes |
| `SubUsersResource` | GET | `/api/users/administrators` | Yes | Yes |
| `LocationResource` | GET | `/api/locations` | Yes | No |
| `LocationResource` | POST | `/api/locations` | Yes | No |
| `VigilanciaFitosanitariaResource` | GET | `/api/vigilancias-fitosanitarias` | Yes | No |
| `VigilanciaFitosanitariaResource` | GET | `/api/vigilancias-fitosanitarias/{id}` | Yes | No |
| `VigilanciaFitosanitariaResource` | POST | `/api/vigilancias-fitosanitarias` | Yes | Yes |
| `VigilanciaFitosanitariaResource` | PUT | `/api/vigilancias-fitosanitarias/{id}` | Yes | Yes |
| `VigilanciaFitosanitariaResource` | DELETE | `/api/vigilancias-fitosanitarias/{id}` | Yes | Yes |
| `StatusResource` | GET | `/api/status` | No | No |

Error responses follow a consistent `{"error": "message"}` format via `ErrorResponseUtils`.

---

## Testing

Two test patterns are used:

### Unit Tests (`*Test.java`)

- Use `@ExtendWith(MockitoExtension.class)` — no Quarkus CDI.
- All dependencies mocked via `@Mock` / `@InjectMocks`.
- Focused on use-case business logic in isolation.

### Integration Tests (`*IntegrationTest.java`)

- Use `@QuarkusTest` with one of two H2 profiles:
  - **`H2TestProfile`** — for REST-layer tests (`AuthResourceIntegrationTest`, `LocationResourceIntegrationTest`). Drops and recreates the schema on each run.
  - **`UseCaseIntegrationTestProfile`** — for use-case integration tests with a separate H2 in-memory database.
- Both profiles use H2 in MySQL compatibility mode.
- Firebase dependencies (`FirebaseTokenVerifier`, `FirebaseUserManager`) are mocked via `@InjectMock` in all integration tests.
- Test data is set up in `@BeforeEach` `@Transactional` methods directly via Panache — not via `import.sql`.

When adding a new feature, provide both a unit test for the use case and an integration test for the REST resource.

---

## CI/CD Pipeline

The pipeline uses **Google Cloud Build** and deploys to **Cloud Run** via **Artifact Registry**.

### Branching Strategy

```
feature/* ──► develop ──► (release tag vX.Y.Z) ──► main (production)
```

Image version precedence: `TAG_NAME` → `SHORT_SHA` → `dev`.

### Pipeline Steps (`cloudbuild.yaml`)

| Step | Action |
|---|---|
| `package` | `./mvnw -B -DskipTests package` — builds `target/quarkus-app` using Maven + Temurin 17 |
| `build-and-push` | Builds Docker image tagged `:<version>` and `:latest`, pushes to Artifact Registry |
| `deploy` | Deploys the versioned image to Cloud Run (`--allow-unauthenticated`) |

### Cloud Build Trigger Recommendations

| Trigger | Action |
|---|---|
| Push to `develop` | Build + push (integration validation) |
| Push to `main` | Build + push + deploy to production |
| Tag `v*` | Release deployment |

### Cloud Run Environment Variables

Configure these on the Cloud Run service (as secrets or env vars):

- `QUARKUS_DATASOURCE_USERNAME`
- `QUARKUS_DATASOURCE_PASSWORD`
- `QUARKUS_DATASOURCE_JDBC_URL`
- `FIREBASE_SERVICE_ACCOUNT_LOCATION`

See [docs/ci-cd.md](docs/ci-cd.md) for the full pipeline reference.
