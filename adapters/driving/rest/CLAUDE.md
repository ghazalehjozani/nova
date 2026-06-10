# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Module: trade-loan-adapters-driving-rest

Synchronous **REST driving adapter** for the trade-loan service. Owns Spring MVC controllers (`spring-boot-starter-webmvc`), API versioning, OpenAPI/Swagger setup, and the dev-only OAuth2 callback proxy. Controllers are thin: they accept HTTP requests, build a `*Command` via the MapStruct mapper from `adapters/driving/contract` (or assemble it inline when no DTO mapping is needed), and hand it to `CommandDispatcher` / `QueryDispatcher`. Zero business logic lives here.

Maven artifact: `ir.dotin.loan:trade-loan-adapters-driving-rest`. Packaged as a plain `jar` — the runnable Spring Boot app lives in `container/`.

## Build / Test

See root [CLAUDE.md → Build & Test](../../../CLAUDE.md#build--test-single-source-of-truth). Maven path: `adapters/driving/rest`. Single test example: `mvn -pl adapters/driving/rest -am test -Dtest=ApproveFacilityControllerTest`.

No `src/main/resources` and no `src/test/java` today — controllers depend on container-side configuration for the running app, and behavioral tests live in the architecture-tests / container modules.

## Package Layout

```
ir.dotin.loan.trade.adapters.driving.rest
├── auth/      # dev/stage-only OAuth2 token-exchange proxy (DevAuthCallbackController)
├── command/
│   └── controller/   # one POST controller per command use case
├── query/
│   ├── loanfacility/         # GET controllers — facility reads
│   ├── loanarrangement/      # GET controllers — arrangement reads
│   ├── installmentschedule/  # GET controllers — installment-schedule reads
│   └── loantype/             # GET controllers — loan-type reads
└── config/
    ├── SwaggerConfig.java    # extends platform BaseSwaggerConfig; defines @Tag constants
    └── WebConfig.java        # API version resolver (SmartPathApiVersionResolver)
```

Controllers are **package-private** (`class Xxx extends BaseController`) — Spring still picks them up via `@RestController`. Keep them package-private; nothing outside this module should hold a reference.

## Routing & Versioning

- Every route prefixes `/api/{version}/…`. The `{version}` placeholder is resolved by `WebConfig.SmartPathApiVersionResolver` (path-index based, fallback `"1"` for non-`/api` paths).
- Method annotations use Spring 6 `@PostMapping(version = "1+")` / `@GetMapping(version = "1")`. The `version = "1+"` form means "v1 and forward-compatible".
- One controller per use case (`ApproveFacilityController`, `RegularDisbursementController`, …). Resource-style fan-out is fine inside a controller (e.g. `/approve` + `/approve/{sanctionSerial}` + `/approve/compensate`) when all endpoints share the same aggregate path and Swagger tag.
- Query side: GET-only controllers under `query/<aggregate>/`, returning `BaseResponse<…>` wrapping DTOs from `core/application/query`. Cursor pagination is provided by `CursorPaginationHelper` / `PagedResponseUtils` from the platform.

## Command Dispatch Pattern (canonical)

```java
@RestController
@RequestMapping("/api/{version}/facilities/{facilityId}/approve")
@RequiredArgsConstructor
class ApproveFacilityController extends BaseController {
    private final CommandDispatcher dispatcher;
    private final ApproveFacilityRequestToCommandMapper mapper;     // from -contract

    @PostMapping(version = "1+")
    public ResponseEntity<BaseResponse<Void>> approve(
            @PathVariable UUID facilityId,
            @RequestBody ApproveFacilityRequest request) {
        var command = mapper.toCommand(facilityId, null, request).toBuilder()
                .uid(getIdempotencyKey())     // BaseController helper
                .build();
        dispatcher.dispatch(command);
        return ResponseEntity.ok(BaseResponse.success());
    }
}
```

Rules:

- **Mapper from `-contract`, not inline DTO→command building** — unless the command has no DTO payload (e.g. `FullLoanFacilityLifecycleController` builds `FullLifecycleRevertCommand` inline because the request mirrors the command 1:1).
- **`uid = getIdempotencyKey()`** — every command carries an idempotency key sourced from the `Idempotency-Key` header (or generated). Do not bypass this; the application service relies on it.
- **No business logic.** No validation against domain state, no ID lookups, no enrichment. If you find yourself injecting a repository or query handler into a command controller, stop — that work belongs in the application service.
- **No outbound ports.** This module must not see `core.application.ports.outbound.*`. ArchUnit will fail the build if it does.

## Query Dispatch Pattern

```java
@GetMapping(value = "/{facilityId}", version = "1")
public ResponseEntity<BaseResponse<TradeFacilityQueryDto>> getById(@PathVariable UUID facilityId) {
    var query = GetFacilityByIdQuery.builder().loanFacilityId(facilityId).build();
    return ResponseEntity.ok(BaseResponse.success(queryDispatcher.dispatch(query)));
}
```

Query DTOs (`TradeFacilityQueryDto`, `LoanFacilityQueryResult`, …) come from `core/application/query` and are returned as-is — no remapping at the REST boundary. If a field needs renaming for the wire, add it in the query module; do not introduce REST-only response DTOs here.

## REST contract (SWA-101)

Company standard **SWA-101 v1.2**. This adapter inherits the contract from the platform — the authoritative wire rules
(headers, status mapping, envelope, idempotency) live in the pangaea `pangaea-spring-boot-starter-protocol-rest`
CLAUDE.md. Nova-relevant points:

- **URL naming:** logical path starts at `/v{major}` (`context-name` is added by the API Gateway — never here).
  kebab-case (`loan-facilities`, `loan-types`), plural collections, **no verbs in paths** (RPC-action exception only
  when resource-oriented design is not defensible, e.g. `/approve` on a facility), ≤3 sub-resource levels, camelCase
  query params; pagination `cursor`+`size` (preferred) or `page`+`size`, `sortBy`+`order`.
- **Headers are invisible in controllers:** never `@RequestHeader`; read via `BaseController` helpers (e.g.
  `getIdempotencyKey()`) / the ambient `InvocationContext`. The platform `HeaderValidationFilter` enforces required
  headers — commands need `Idempotency-Key` + `X-Correlation-ID` (UUIDs), all requests need `X-Request-DateTime` +
  `Accept-Language`. Queries carry no idempotency/correlation requirement.
- **Status / response shape:** commands → `201 Created` + `Location` (resource create) or `204 No Content` (execute);
  queries → `200 OK` + `BaseResponse<T>`. Let `CommandResponseFactory` / `BaseController` pick — don't hand-build
  statuses or envelopes.
- **Errors:** nova has **no** `@RestControllerAdvice`. The platform `GlobalExceptionHandler` produces the SWA-101
  `ErrorResponse`; do not catch domain exceptions to remap them. **Success = empty `errorList []`**; a non-empty
  `errorList` is always paired with a 4xx/5xx (never 2xx). No `RsCode` / `IsSuccess` (legacy ESB) anywhere.

## Dependency Rules (this module)

- **May depend on:**
  - `adapters/driving/contract` — request DTOs + their mappers
  - `core/application/ports/inbound` — `*Command` types
  - `core/application/query` — query types + result DTOs (driving adapters are allowed to read directly from the query module per root rules)
  - `platform-dispatcher-starter` — `CommandDispatcher`, `QueryDispatcher`
  - `platform-spring-boot-starter-protocol-rest` — `BaseController`, `BaseResponse`, `BaseSwaggerConfig`, pagination helpers
  - `platform-spring-boot-starter-security-oauth2` — `PlatformSecurityProperties` (used only in `auth/`)
  - `spring-boot-starter-webmvc`, `springdoc-openapi-starter-webmvc-ui`, MapStruct, Lombok
- **Must NOT depend on:**
  - `core/application/ports/outbound` (forbidden — driving adapters are read-only against the inbound side)
  - `core/application/service` (services are injected via the dispatcher, never directly)
  - `core/domain` (domain types never cross the REST boundary)
  - any driven adapter (`adapters/driven/persistence`, `adapters/driven/fcb-messaging`)
  - sibling driving adapters (`messaging-kafka`, `messaging-activemq`)
- Architecture compliance is enforced by ArchUnit in the root `architecture-tests` module.

## Swagger / OpenAPI

- `SwaggerConfig` extends `BaseSwaggerConfig` and registers a `GroupedOpenApi` per logical area, plus `OpenApiCustomizer` beans that inject example payloads loaded from JSON resources in the **container** module (no resources live here).
- Tag constants (`TAG_FACILITY_APPROVAL`, `TAG_FACILITY_QUERIES`, …) live as `public static final String` on `SwaggerConfig`. **Always use these constants on `@Tag(name = …)`** — string literals will silently desync the grouping.
- Persian descriptions on `@Operation(summary = …)` / `@Parameter(description = …)` are intentional (Persian-speaking ops/PM audience). Preserve them; do not translate without product approval.
- `@Hidden` is used to exclude dev-only endpoints (`DevAuthCallbackController`) from generated docs.

## Auth Adapter (`auth/`)

`DevAuthCallbackController` is a **dev/stage-only** OAuth2 token-exchange proxy (`@Profile({"dev", "stage"})`) that lets the local Swagger UI complete the auth-code flow without exposing the IdP client secret to the browser. Not part of the product surface — if tempted to add anything beyond token proxying here, push back and ask whether it belongs in the platform security starter instead.

## API Versioning Gotchas

- `SmartPathApiVersionResolver` only resolves a version when the request URI starts with `/api`. Anything outside `/api` (actuator, swagger, dev endpoints already prefixed) falls back to `"1"`. Do not move actuator under `/api`.
- Adding a new supported version requires updating `WebConfig.configureApiVersioning(...).addSupportedVersions(...)`. Keep `setVersionRequired(true)` — requests missing a resolvable version should fail loudly, not default silently.
- Deprecating an old version is half-wired (see commented `setDeprecationHandler` block in `WebConfig`). If we ever cut v2, finish that wiring rather than copy-pasting.

## Pitfalls

- Do not inject `EntityManager`, JPA repositories, Feign clients, or any outbound port into a controller. The build will allow some of these at compile time but ArchUnit will reject them.
- Do not catch domain exceptions in controllers to remap them. Global exception handling lives in the platform `BaseController` / advice; let it through.
- Do not introduce response DTOs that diverge from the query module's DTOs just to rename a field for the wire. Push the rename into `core/application/query` so the contract is consistent across transports.
- `@RestController` classes must remain package-private. If you make one `public`, design review will push back.
- When a command's payload is identical to a request DTO, prefer adding the mapping to `adapters/driving/contract` rather than building the command inline — keeps the anti-corruption seam in one place. The lifecycle/compensation controllers are the documented exception (no DTO field translation needed).
