# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Module: trade-loan-adapters-driven-persistence

JPA persistence adapter for the trade-loan service. Implements outbound command repositories and query repositories defined in `core/application/ports/outbound` and `core/application/query`. Also hosts Redis cache config and outbox event handlers wired into the platform `OutboxHandler` SPI.

Maven artifact: `ir.dotin.loan:trade-loan-adapters-driven-persistence`.

## See also

- Outbound ports implemented: [`core/application/ports/outbound`](../../../core/application/ports/outbound/CLAUDE.md)
- Query handlers consuming repos: [`core/application/query`](../../../core/application/query/CLAUDE.md)

## Build / Test

See root [CLAUDE.md → Build & Test](../../../CLAUDE.md#build--test-single-source-of-truth). Maven path: `adapters/driven/persistence`. Single test example: `mvn -pl adapters/driven/persistence -am test -Dtest=TradeLoanFacilityRepositoryAdapterTest`.

No `src/main/resources` in this module — Liquibase changesets and `application*.yml` live in the `container` module. `@EnableJpaRepositories` / `@EntityScan` are anchored on `ir.dotin.loan.trade.adapters.driven.persistence` via `config/LoanPersistenceConfiguration`.

## Package Layout (by aggregate)

```
ir.dotin.loan.trade.adapters.driven.persistence
├── config/                  # LoanPersistenceConfiguration, RedisConfig, ResilientCacheErrorHandler
├── embdeddable/             # JPA @Embeddable VOs (note: typo'd dir name — keep as-is)
├── mapper/                  # BaseMapperConfig, ValueObjectMapper (shared MapStruct config)
├── shared/query/            # AbstractCursorPagingAdapter, SortBuilder (cursor pagination kit)
├── loanfacility/            # TradeLoanFacility aggregate
│   ├── entity/              # JPA entities (+ TradeLoanFacilityOutboxEventEntity)
│   ├── mapper/              # Entity ↔ domain, outbox event mapper
│   ├── repository/          # Spring Data JpaRepository + outbox repo
│   ├── query/               # JpaFacilityQueryAdapter + JpaApplicationNumberResolver
│   ├── TradeLoanFacilityRepositoryAdapter.java   # implements outbound command port
│   └── TradeLoanFacilityOutboxHandler.java       # platform OutboxHandler<E, M>
├── loanarrangement/         # same layout (+ projection/)
├── loantype/                # same layout (+ projection/)
└── installmentschedule/     # same layout
```

Per-aggregate convention is mandatory: each aggregate gets its own `entity/ mapper/ repository/ query/` plus a top-level `*RepositoryAdapter` and `*OutboxHandler`. Do not flatten or share packages across aggregates.

## Adapter Patterns

**Command repository adapter** — implements outbound port from `core/application/ports/outbound/command/repository/`:
- `@Repository @Transactional(readOnly = true)` at class level; override with `@Transactional` on mutating methods (`save`, `delete`).
- `@RequiredArgsConstructor` + `final` deps (Lombok).
- Returns domain types only (e.g. `TradeLoanFacility`). Map entity ↔ domain via a MapStruct mapper using `BaseMapperConfig`.
- `requireNonNull(arg, "...")` guards on every public method.
- Some methods return `ir.dotin.platform.commons.core.Result<T>` — preserve that signature; do not change to `Optional` or raw throws.

**Query adapter** — implements query port from `core/application/query/.../port/`:
- Reuses the same JPA entities as the command side (single source of truth — no separate read entities).
- Maps `Entity → QueryModel` (query models live in the application-query module).
- Cursor pagination → extend `shared/query/AbstractCursorPagingAdapter`; sort spec → `SortBuilder`.
- Use projections (`*/projection/`) for narrow lookups (e.g. `TradeLoanTypeIdProjection`) to avoid loading full graphs.

**Outbox handler** — implements `ir.dotin.platform.adapter.messaging.persistence.handler.OutboxHandler<E, M>`:
- One per aggregate; `aggregateType()` returns the aggregate-root class.
- Owns its own `*OutboxEventEntity` + `*OutboxRepository` + `*OutboxEventMapper`.
- Backed by `platform-outbox-data-jpa`. Do not write your own polling/dispatch.

**Embeddables** — all value objects of an aggregate are persisted as `@Embeddable` types in `embdeddable/` (sic — keep the spelling, renaming would churn imports across the repo). Naming: `<DomainName>Emb.java`.

## Caching (Redis)

`config/RedisConfig` builds a Lettuce + Sentinel `RedisConnectionFactory` and `RedisCacheManager` gated by `@ConditionalOnProperty`. `ResilientCacheErrorHandler` swallows Redis errors so a cache outage does not break command paths — keep that behavior; do not propagate cache exceptions to the domain. Because the `ClientResources`/`ConnectionFactory` are custom, Spring Boot's Lettuce observation auto-config backs off — `lettuceClientResources` wires `MicrometerTracing` explicitly so Redis CLIENT spans (`db.system=redis`) appear in APM. Keep `includeCommandArgsInSpanTags=false` (don't leak keys/args into spans).

Add `@Cacheable` only on query adapters (read-only). Never cache aggregate writes.

## Dependency Rules (this module)

- Implements ports from: `core/application/ports/outbound`, `core/application/query`.
- May depend on: `core/domain`, `base-loan` domain VOs (e.g. `LoanFacilityId`, `ApplicationNumber`), platform libs (`platform-spring-boot-starter-persistence-jpa`, `platform-outbox-data-jpa`, `platform-envelope-api`, `expression-kit-infrastructure`).
- Must NOT depend on: any driving adapter, `container`, `core/application/service` implementations, `core/application/ports/inbound`.
- Architecture compliance is enforced by ArchUnit in the root `architecture-tests` module — run those before pushing structural changes.

## Anti-Corruption Boundary

Legacy/external terms (e.g. `fileNumber`) must not appear here. Domain terms only (`applicationNumber`). If a query needs to resolve a legacy identifier, do it in a dedicated resolver in `query/` (see `JpaApplicationNumberResolver`) and expose only the domain ID upstream.
