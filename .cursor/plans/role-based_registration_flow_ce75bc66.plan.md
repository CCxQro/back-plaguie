---
name: Role-based registration flow
overview: "Implement split registration flows: one for farmer/technical seller requiring location resolution, and one for admin without location. Persist user in `Usuario` plus role-specific table, with conflict on duplicate email."
todos:
  - id: phase1-location
    content: Create location repository layer and resolve-or-create location use case
    status: pending
  - id: phase2-specialized-users
    content: Add farmer/technical/admin repositories and persist specialized entities after user creation
    status: pending
  - id: phase3-endpoints
    content: Split registration endpoints and DTOs for location-required vs admin flows
    status: pending
  - id: phase4-tests
    content: Add/update integration tests for all role registration scenarios and conflicts
    status: pending
isProject: false
---

# Role-Based Registration Plan

## Goal
Implement registration in 3 phases:
1) reusable location resolution/creation,
2) role-specific persistence for users,
3) split auth endpoints and tests.

## Current Baseline
- `RegisterUserUseCase` currently creates Firebase user + `Usuario` only, with duplicate email conflict.
- `FarmerEntity` and `TechnicalSellerEntity` require `id_ubicacion`.
- `AdministratorEntity` requires only `id_usuario`.
- There are no repository abstractions yet for location tables or specialized user tables.

Key current code paths:
- [`/Users/leonibernabe/Documents/TECdeMTY/6th Semester/AndresTorres/proyecto/back-plaguie/src/main/java/itesm/mx/interfaces/rest/AuthResource.java`](/Users/leonibernabe/Documents/TECdeMTY/6th Semester/AndresTorres/proyecto/back-plaguie/src/main/java/itesm/mx/interfaces/rest/AuthResource.java)
- [`/Users/leonibernabe/Documents/TECdeMTY/6th Semester/AndresTorres/proyecto/back-plaguie/src/main/java/itesm/mx/application/usecase/RegisterUserUseCase.java`](/Users/leonibernabe/Documents/TECdeMTY/6th Semester/AndresTorres/proyecto/back-plaguie/src/main/java/itesm/mx/application/usecase/RegisterUserUseCase.java)

## Phase 1: Location resolution module (first)
Create a dedicated location repository/service layer to resolve each location part by name and create missing rows.

### Scope
- Add domain repository contract(s) for:
  - `State` by normalized name
  - `Municipality` by normalized name
  - `Locality` by normalized name
  - `Location` by full combination (`stateId`, `municipalityId`, `localityId`, coordinates/property as needed)
- Add persistence implementations using Panache for:
  - [`/Users/leonibernabe/Documents/TECdeMTY/6th Semester/AndresTorres/proyecto/back-plaguie/src/main/java/itesm/mx/infrastructure/persistence/entity/location/StateEntity.java`](/Users/leonibernabe/Documents/TECdeMTY/6th Semester/AndresTorres/proyecto/back-plaguie/src/main/java/itesm/mx/infrastructure/persistence/entity/location/StateEntity.java)
  - [`/Users/leonibernabe/Documents/TECdeMTY/6th Semester/AndresTorres/proyecto/back-plaguie/src/main/java/itesm/mx/infrastructure/persistence/entity/location/MunicipalityEntity.java`](/Users/leonibernabe/Documents/TECdeMTY/6th Semester/AndresTorres/proyecto/back-plaguie/src/main/java/itesm/mx/infrastructure/persistence/entity/location/MunicipalityEntity.java)
  - [`/Users/leonibernabe/Documents/TECdeMTY/6th Semester/AndresTorres/proyecto/back-plaguie/src/main/java/itesm/mx/infrastructure/persistence/entity/location/LocalityEntity.java`](/Users/leonibernabe/Documents/TECdeMTY/6th Semester/AndresTorres/proyecto/back-plaguie/src/main/java/itesm/mx/infrastructure/persistence/entity/location/LocalityEntity.java)
  - [`/Users/leonibernabe/Documents/TECdeMTY/6th Semester/AndresTorres/proyecto/back-plaguie/src/main/java/itesm/mx/infrastructure/persistence/entity/location/LocationEntity.java`](/Users/leonibernabe/Documents/TECdeMTY/6th Semester/AndresTorres/proyecto/back-plaguie/src/main/java/itesm/mx/infrastructure/persistence/entity/location/LocationEntity.java)
- Implement an application service/use case `ResolveOrCreateLocationUseCase` that returns `locationId`.

### Rules
- Normalize names (`trim`, collapse spaces, case-insensitive comparison strategy).
- If location part exists, reuse ID; otherwise insert and use new ID.
- Keep this transactional so the full location chain is consistent.

## Phase 2: Role-specific persistence after `Usuario` creation
Extend registration flow to persist in specialized table based on role:
- Role `2` (`SELLER`) -> insert into `Tecnico_Vendedor` with `id_usuario`, `id_ubicacion`, `isActive=true`
- Role `3` (`FARMER`) -> insert into `Agricultor` with `id_usuario`, `id_ubicacion`, `isActive=true`
- Role `1` (`ADMIN`) -> insert into `Administrador` with `id_usuario`, `isActive=true`

### Scope
- Add repository contracts + implementations for:
  - `FarmerEntity`
  - `TechnicalSellerEntity`
  - `AdministratorEntity`
- Refactor/extend `RegisterUserUseCase` into role-aware orchestration:
  1. Validate input by role (location required only for roles 2/3)
  2. Check duplicate email (keep conflict behavior)
  3. Create Firebase user
  4. Create `Usuario`
  5. Create specialized row (and location resolution first when required)
  6. If DB step fails, rollback Firebase user (existing behavior)

## Phase 3: Split API endpoints (as selected)
In [`/Users/leonibernabe/Documents/TECdeMTY/6th Semester/AndresTorres/proyecto/back-plaguie/src/main/java/itesm/mx/interfaces/rest/AuthResource.java`](/Users/leonibernabe/Documents/TECdeMTY/6th Semester/AndresTorres/proyecto/back-plaguie/src/main/java/itesm/mx/interfaces/rest/AuthResource.java):
- Keep `/register` for admin-managed user creation if desired, but split by responsibility.
- Add endpoint for registration with location (roles 2/3 payload with location block).
- Add endpoint for admin registration (role 1, no location block).
- Keep `/signup` for self-registration and make it create `Usuario` + specialized row (likely role 2).

### DTO changes
- Introduce role-specific request DTOs:
  - `RegisterWithLocationDto` (name, email, password, roleId, location fields)
  - `RegisterAdminDto` (name, email, password)
- Keep response DTO compatible with existing consumers.

## Tests and validation
- Update/add integration tests around AuthResource:
  - successful registration role 2/3 with location creation/reuse
  - successful admin registration without location
  - duplicate email -> `409 CONFLICT`
  - invalid role-to-payload combinations -> `400 BAD_REQUEST`
- Add repository-level tests (or integration assertions) for location dedup logic.

## Suggested implementation order
1. Location repositories + `ResolveOrCreateLocationUseCase`
2. Specialized user repositories (farmer/technical/admin)
3. Extend registration orchestration in `RegisterUserUseCase`
4. Add split endpoints + new DTOs in `AuthResource`
5. Update tests

## Risks to control
- Partial writes across Firebase and DB tables: keep transaction + rollback strategy.
- Duplicate location rows due to inconsistent normalization: centralize normalization in one helper/service.
- Role drift (hardcoded values in multiple places): use `RoleConstants` everywhere.