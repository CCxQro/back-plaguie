---
name: Location module only
overview: "Focus only on location registration flow: mapper layer, repository implementation with register logic inside repository, then domain/application pieces needed to consume it."
todos:
  - id: loc-models-mappers
    content: Create location domain models and mappers for state/municipality/locality/location
    status: pending
  - id: loc-repo-contract
    content: Define single-method LocationRepository contract for registerLocation
    status: pending
  - id: loc-repo-impl
    content: Implement registerLocation logic entirely in repository with resolve-or-create behavior
    status: pending
  - id: loc-usecase-dtos
    content: Add RegisterLocation DTOs and use case consuming LocationRepository
    status: pending
  - id: loc-tests
    content: Add integration tests for create/reuse/validation scenarios
    status: pending
  - id: loc-point-review
    content: Review migration from latitude/longitude columns to POINT and define ORM strategy
    status: pending
isProject: false
---

# Location-Only Plan

## Goal

Implement only the location module so it can register a location and return `locationId`, leaving user-role registration for later.

## Scope Boundaries

- Include: location entities mapping, repository contract + implementation, location models, and a register-location use case.
- Exclude for now: farmer/technical/admin persistence, auth endpoint splitting, and user registration orchestration.

## Phase 0: POINT feasibility review (before coding)

Evaluate replacing `latitude` + `longitude` with a single spatial `POINT` column.

### Review checklist

- **ORM support in your stack**: verify if Quarkus + Hibernate in this project can map spatial types directly (e.g., with Hibernate Spatial + JTS `Point`) without breaking current conventions.
- **Database compatibility**: confirm DB engine spatial support and expected SQL type (`POINT`) semantics for inserts/queries/indexes.
- **Migration impact**:
  - schema migration from two numeric columns to one spatial column
  - backfill strategy (compose `POINT(longitude latitude)` from existing rows)
  - rollback path if migration must be reverted
- **Query impact**: identify current and planned lookups that will change (exact match, proximity search, indexing).
- **DTO/API impact**: decide whether external API remains `latitude/longitude` while ORM maps to `POINT`, or API also changes.

### Output of this phase

- A short technical decision note: keep lat/long for now vs migrate to `POINT` now.
- If migrating now, define exact entity mapping approach and required dependency/migration steps before Phase 1.

## Phase 0.1: Implement lat/long → POINT (do this first)
Viability is confirmed, so we will implement the schema + ORM mapping before building the registration repository logic.

### DB schema + data migration
- Replace `Ubicacion.latitud` + `Ubicacion.longitud` with a single `Ubicacion.punto` (SQL type `POINT`).
- Backfill existing rows by composing the new `POINT` from existing columns (typically `POINT(longitude latitude)` depending on DB).
- Decide whether you need a spatial index now (only needed if you plan distance/proximity queries soon).

### ORM mapping (Quarkus/Hibernate)
- Update `LocationEntity` to remove `latitude`/`longitude` and add a `point` field mapped to the DB `POINT` column.
- Keep API/DTO inputs as `latitude`/`longitude` (recommended) and construct the `POINT` during persistence, unless you explicitly want to expose `POINT` externally.

### Repository implications
- Update the location “exists?” lookup to use `POINT` equality (or a defined normalization/rounding strategy) instead of separate `latitude`/`longitude` comparisons.

## Phase 1: Location domain models and mappers

Create clear model-to-entity conversion for each location table first.

### Files to add/update

- `[/Users/leonibernabe/Documents/TECdeMTY/6th Semester/AndresTorres/proyecto/back-plaguie/src/main/java/itesm/mx/infrastructure/mapper]( /Users/leonibernabe/Documents/TECdeMTY/6th Semester/AndresTorres/proyecto/back-plaguie/src/main/java/itesm/mx/infrastructure/mapper )`
  - `StateMapper`
  - `MunicipalityMapper`
  - `LocalityMapper`
  - `LocationMapper`
- Domain models package (if missing for location):
  - `State`
  - `Municipality`
  - `Locality`
  - `Locatio`

### Decisions

- Keep mappers simple and deterministic (no business logic).
- Normalize naming responsibility in repository logic (not mapper), per your preference.

## Phase 2: Location repository contract

Define one repository API focused on registration.

### Contract

- `LocationRepository.registerLocation(RegisterLocationCommand command): Location`

`registerLocation` should encapsulate all internal steps:

1. resolve/create `State`
2. resolve/create `Municipality`
3. resolve/create `Locality`
4. resolve/create final `Location`
5. return created-or-existing `Location` model (with `locationId`)

## Phase 3: Repository implementation with full logic inside

Implement all dedup and creation logic in the repository layer (single public method).

### Files to add

- Domain repository interface in domain layer.
- Persistence implementation in infrastructure layer using Panache over:
  - `[/Users/leonibernabe/Documents/TECdeMTY/6th Semester/AndresTorres/proyecto/back-plaguie/src/main/java/itesm/mx/infrastructure/persistence/entity/location/StateEntity.java](/Users/leonibernabe/Documents/TECdeMTY/6th Semester/AndresTorres/proyecto/back-plaguie/src/main/java/itesm/mx/infrastructure/persistence/entity/location/StateEntity.java)`
  - `[/Users/leonibernabe/Documents/TECdeMTY/6th Semester/AndresTorres/proyecto/back-plaguie/src/main/java/itesm/mx/infrastructure/persistence/entity/location/MunicipalityEntity.java](/Users/leonibernabe/Documents/TECdeMTY/6th Semester/AndresTorres/proyecto/back-plaguie/src/main/java/itesm/mx/infrastructure/persistence/entity/location/MunicipalityEntity.java)`
  - `[/Users/leonibernabe/Documents/TECdeMTY/6th Semester/AndresTorres/proyecto/back-plaguie/src/main/java/itesm/mx/infrastructure/persistence/entity/location/LocalityEntity.java](/Users/leonibernabe/Documents/TECdeMTY/6th Semester/AndresTorres/proyecto/back-plaguie/src/main/java/itesm/mx/infrastructure/persistence/entity/location/LocalityEntity.java)`
  - `[/Users/leonibernabe/Documents/TECdeMTY/6th Semester/AndresTorres/proyecto/back-plaguie/src/main/java/itesm/mx/infrastructure/persistence/entity/location/LocationEntity.java](/Users/leonibernabe/Documents/TECdeMTY/6th Semester/AndresTorres/proyecto/back-plaguie/src/main/java/itesm/mx/infrastructure/persistence/entity/location/LocationEntity.java)`

### Internal behavior in `registerLocation`

- Normalize strings (`trim`, compact spaces, case-insensitive compare).
- For each catalog (`State`, `Municipality`, `Locality`):
  - query by normalized name
  - insert when missing
- For `Location`:
  - query by full combination (`stateId`, `municipalityId`, `localityId`, `propertyId`, and `POINT` equality/strategy)
  - insert when missing
- Wrap in one transaction for consistency.

## Phase 4: Application use case and DTOs

Create application-facing entrypoint to consume repository method cleanly.

### Files to add

- `RegisterLocationDto` (input)
- `RegisterLocationResponseDto` (output with `locationId` and resolved IDs)
- `RegisterLocationUseCase`:
  - validate required fields
  - call `LocationRepository.registerLocation(...)`
  - map result to response DTO

## Phase 5: Minimal integration surface + tests

Finish location module as independently usable and testable.

### Deliverables

- Optional temporary REST endpoint (`/api/locations/register`) to test flow in isolation.
- Integration tests:
  - creates all missing catalog rows + location
  - reuses existing rows on repeated request
  - invalid payload returns `400`

## Recommended order

1. POINT feasibility review
2. Implement lat/long → POINT mapping + migration
3. Domain models for location
4. Mappers for location entities
5. `LocationRepository` contract
6. Repository implementation (`registerLocation` full logic)
7. DTOs + `RegisterLocationUseCase`
8. test endpoint + integration tests

## Notes aligned with your preference

- Business logic stays inside repository method for now.
- Use case remains thin (validation + orchestration only).
- This gives a stable location module before touching user-role registration.

