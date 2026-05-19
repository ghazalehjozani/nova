# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Module Purpose

`trade-loan-core-application-query` — CQRS **read side** for trade-loan. Holds query objects, query handlers, query DTOs, read-only outbound ports (`*QueryRepository`), and pagination primitives. The command/write side lives in `core/application/service`; this module never mutates state.

This module is the application-layer query slice referenced by the root CLAUDE.md rule *"driving adapters → application query module (for read access)"*. Driving adapters (REST, messaging) depend on this module to read data without crossing into outbound write ports.

## See also

- Write side: [`core/application/service`](../service/CLAUDE.md)
- Repository implementations: [`adapters/driven/persistence`](../../../adapters/driven/persistence/CLAUDE.md)
- Consumed by: [`adapters/driving/rest`](../../../adapters/driving/rest/CLAUDE.md), [`adapters/driving/messaging-kafka`](../../../adapters/driving/messaging-kafka/CLAUDE.md)

## Module Layout

```
src/main/java/ir/dotin/loan/trade/core/application/query/
├── shared/
│   ├── pagination/   # CursorPageRequest, CursorPage, OffsetPageRequest, OffsetPage,
│   │                 # CursorPosition, CursorEncoder (Base64-url JSON via Jackson 3)
│   └── exception/    # CursorEncodingException, InvalidCursorException
├── loanarrangement/  # {dto, repository, request, handler}
├── loanfacility/     # {dto, repository, request, handler, i18n}
├── loantype/         # {dto, repository, request, handler}
└── installmentschedule/ # {dto, repository, request, handler, i18n}
```

Per-aggregate package convention (strict — preserve when adding aggregates):
- `request/` — query objects (records) implementing `ir.dotin.platform.dispatcher.api.query.Query<R>`. Naming: `Get<X>ByIdQuery`, `FindAll<X>Query`, `<X>FilterQuery`.
- `handler/` — `@Component` classes implementing `QueryHandler<Q, R>`. One handler per query.
- `repository/` — **outbound port interface** named `Trade<X>QueryRepository`. Implemented by `adapters/driven/persistence`. No `@Repository` annotation; this is a port, not a Spring Data interface.
- `dto/` — projection records returned by repositories (`Trade<X>QueryDto`) and result wrappers (`<X>QueryResult`) returned by handlers.
- `i18n/` — error-code enums where needed.

## Build

```bash
mvn -pl core/application/query -am clean test    # from repo root
mvn clean test                                    # from this directory
mvn test -Dtest=FindAllLoanFacilitiesQueryHandlerTest   # single test
```

The module is `jar`, parented by `trade-loan-core-application`. Reactor build from repo root: `mvn clean verify -P!dev -DskipITs=true`.

## Architectural Rules (enforced by `architecture-tests` module)

- **No write-side dependencies.** Do not import command objects, command handlers, or write ports here. The dependency on `trade-loan-core-application-ports-outbound` exists for shared port types only — repositories defined in this module are query-only.
- **No persistence / framework leakage in ports.** `*QueryRepository` interfaces must use domain UUIDs and DTO records from this module — never JPA, Spring Data `Pageable`, or driving-adapter types.
- **Handlers are stateless `@Component` beans.** Construct via `@RequiredArgsConstructor`. Do not put business logic in handlers beyond mapping query → port call → result. Validation policy belongs in the command side; query side trusts inputs from inbound ports.
- **Anti-corruption.** Domain terminology only. Legacy terms (e.g. `fileNumber`) must already have been translated upstream by the driving adapter before reaching a query here.

## Pagination Conventions

Two shapes coexist intentionally:

- **Cursor pagination** (`CursorPageRequest` / `CursorPage`) — for unbounded list endpoints (`findAll`). Cursor is a Base64-url-encoded JSON `CursorPosition` produced by `CursorEncoder` (Jackson 3, `tools.jackson.databind`). Page-size bounds: 1..100, defaults to 20 if out of range. Handler returns `*QueryResult.forCursor(...)`.
- **Offset pagination** (`OffsetPageRequest` / `OffsetPage`) — for filter/search endpoints that need total count and page jumps. Handler returns `*QueryResult.forOffset(...)`.

When adding a new list query: choose cursor unless the consumer genuinely needs total count or random page access. Do not invent a third pagination shape.

## Dispatcher Contract

Queries and handlers integrate with `ir.dotin.platform.dispatcher.api.query` (`Query<R>`, `QueryHandler<Q, R>`). Driving adapters dispatch through the platform query bus; they never call handlers directly. Keep `Query` record fields primitive/value-object only (no entities, no JPA types) so they remain serializable across the dispatcher boundary.

## When Adding a New Query

1. Define request record in `<aggregate>/request/` implementing `Query<R>`.
2. Define result type in `<aggregate>/dto/` (reuse `*QueryResult` if shape matches).
3. If a new read shape is needed, extend `Trade<X>QueryRepository` with a method returning DTO records / `CursorPage` / `OffsetPage` — never entities, never JPA types.
4. Implement `@Component` handler in `<aggregate>/handler/`.
5. Implement the new repository method in `adapters/driven/persistence` (separate module).
6. Add handler test under `src/test/java/...` mirroring the package.
