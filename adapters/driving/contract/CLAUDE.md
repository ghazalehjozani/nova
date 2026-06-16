# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Module: trade-loan-adapters-driving-contract

Shared **driving-side contract** for the trade-loan service: external-facing request/message DTOs plus the MapStruct mappers that translate them into inbound `*Command` types from `core/application/ports/inbound`. Consumed by sibling driving adapters: `adapters/driving/rest`, `adapters/driving/messaging-kafka`. This module owns no transport — no controllers, no listeners.

Maven artifact: `ir.dotin.loan:trade-loan-adapters-driving-contract`.

## See also

- Command targets: [`core/application/ports/inbound`](../../../core/application/ports/inbound/CLAUDE.md), [`core/application/service`](../../../core/application/service/CLAUDE.md)
- DTOs consumed by: [`adapters/driving/rest`](../rest/CLAUDE.md), [`adapters/driving/messaging-kafka`](../messaging-kafka/CLAUDE.md)

## Build / Test

See root [CLAUDE.md → Build & Test](../../../CLAUDE.md#build--test-single-source-of-truth). Maven path: `adapters/driving/contract`. Single test example: `mvn -pl adapters/driving/contract -am test -Dtest=OriginateLoanFacilityRequestMapperTest`.

No `src/test`, no `src/main/resources` today — pure DTO + mapper module. MapStruct generates implementations into `target/generated-sources/annotations`.

## Package Layout

```
ir.dotin.loan.trade.adapters.driving.contract
├── dto/        # request/message records (one per use case + payload sub-records)
└── mapper/     # MapStruct @Mapper interfaces — DTO → inbound Command
```

One DTO file per use case (`OriginateLoanFacilityRequest`, `ApproveFacilityRequest`, `CancelFacilityMessage`, …). Mapper filename mirrors the DTO: `XxxRequestToCommandMapper.java` for **sync REST requests** (`BaseRequest`); `XxxMessageMapper.java` for **async FCB→nova messages** (`CommandPayload`) — name async DTOs `Xxx*Message`, not `*Request`. Keep this 1-DTO / 1-mapper convention — sibling adapters discover mappers by Spring component scan over this package.

## Two Payload Families

Pick the right base type when adding a new DTO:

- **`ir.dotin.platform.pangaea.protocol.api.request.BaseRequest`** — synchronous REST requests. Used by `adapters/driving/rest`. DTOs carry Swagger `@Schema` annotations (Persian descriptions are intentional — preserve them).
- **`ir.dotin.platform.pangaea.messaging.api.command.CommandPayload`** — asynchronous message payloads. Used by the Kafka driving adapter. Plain records, usually `@Builder(toBuilder = true)`, no Swagger annotations.

Both flow through the same application service layer — the mapper's job is to erase that transport difference and produce the same `*Command` regardless of source.

## SWA-101 field conventions (v1.3)

DTO field shapes follow the SWA-101 wire contract (authoritative in pangaea `protocol-api`). When adding/editing a DTO:

- Business fields sit at the **root** (no `payload` wrapper); optional `metadata` only for processing hints. camelCase names.
- **Temporal type must match business meaning:** a calendar day (installment due date, value date, maturity/سررسید,
  birth date) is **date-only** `LocalDate` (`yyyy-MM-dd`, no zone) — *not* a date-time; a true instant
  (event/registration time) is **date-time** `Instant` (UTC). Sending a day as a zeroed `...T00:00:00Z` is a bug
  (TZ shift moves the day).
- Money/amount → `string` (never `float`/`double`), ≤4 decimals. On **sync REST request DTOs** enforce the
  ≤4-decimal rule with the `dto/validation/Money` constraint (composed `@Digits(fraction=4)`) — money fields only,
  **not** interest rates/percentages (they may carry more precision). Enums travel as a bare `string` `code` on the
  request side (responses use the `{code,label}` object, rendered by the query side, not here).
- **Period/term/grace are unitless `Integer` with the unit in the field name** (`requestedLoanDurationMonths`,
  `gracePeriodDays`, `installmentPeriodMonths`, regulatory `*Months`) — the mapper wraps them to `java.time.Period`
  at the seam (`OriginateLoanFacilityRequestMapper`, `DefineTradeLoanArrangementRequestToCommandMapper`). This is the
  accepted convention (SAW.101 §3): do **not** put `Period`/ISO-8601 duration strings on the wire. State the unit in
  each field's `@Schema` description (e.g. `(ماه)` / `(روز)`).

## Mapper Conventions

```java
@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedSourcePolicy = ReportingPolicy.WARN,
    unmappedTargetPolicy = ReportingPolicy.ERROR,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface XxxRequestToCommandMapper { ... }
```

- `componentModel = SPRING` — mappers are `@Component` beans; inject them.
- `unmappedTargetPolicy = ERROR` — every command field must be mapped or explicitly `@Mapping(target = "...", ignore = true)`. Build will fail otherwise. This is the safety net for the anti-corruption layer.
- Inline `default` methods wrap primitives into command-layer value-object DTOs (`AmountDto`, `CurrencyTypeDto`, `OriginateLoanFacilityCommand.LoanDurationDto`, …). Keep wrapping here — never in the application service.
- Sealed-DTO hierarchies (`PartyRequestDto`, `DisburseDestinationRequestDto`) translate to sealed command-DTO hierarchies via `switch` pattern-matching in `default` methods.
- Time conversions (months → `Period`, days → `Period`) belong in mapper `default` methods, not in the DTO and not in the service.

## Anti-Corruption Boundary (CRITICAL)

This module **is** the anti-corruption layer for inbound traffic. Per root rules:

- Legacy/external vocabulary lives **only** in this module's DTO field names (e.g. `fileNumber`, `requestedLoanDurationMonths`, `gracePeriodDays`).
- The mapper translates to domain terminology before it crosses into `core/application/ports/inbound` (`applicationNumber`, `LoanDurationDto(Period)`, `GracePeriodDto(Period)`).
- Mappers may **not** call services, repositories, or anything outside `core/application/ports/inbound.*` types. Resolution that needs domain state (e.g. resolving a legacy `fileNumber` to a domain `applicationNumber`) is the application service's job, not the mapper's.
- Document every legacy↔domain term swap in the mapper (an `@Mapping(source=..., target=...)` line is self-documenting; for derived translations, a one-line comment naming both terms is enough).

## Dependency Rules (this module)

- **May depend on:** `core/application/ports/inbound` (commands + their nested DTOs), `base-loan` domain enums used in transport (`ApplicantChannel`, `DisbursementMethod`), `platform-messaging-api` (`CommandPayload`), `platform-protocol-api` (`BaseRequest`), `springdoc-openapi` (`@Schema`), `time4j-base`, Lombok.
- **Must NOT depend on:** any other adapter (driven or driving), `container`, `core/application/service`, `core/application/ports/outbound`, `core/application/query`. Mappers consume inbound port types only.
- **Must NOT depend on `core/domain` value objects directly** for the request side — wrap raw primitives into inbound-port DTOs (`AmountDto`, `CurrencyTypeDto`, …). Domain types are reserved for the service layer to instantiate.
- Architecture compliance is enforced by ArchUnit in the root `architecture-tests` module — run those before pushing structural changes.

## Pitfalls

- Adding a field to a command in `core/application/ports/inbound` without updating the corresponding mapper will fail the build (`unmappedTargetPolicy = ERROR`). That is intentional — fix the mapper, do not relax the policy.
- Do not enrich DTOs with computed/derived fields. The DTO is a faithful wire-format record; derivation belongs in the mapper or downstream.
- JSR-303 constraints (`@NotNull`, `@Valid`, `dto/validation/Money`) belong on **sync REST request DTOs** (the `BaseRequest` family) — the REST controller triggers them with `@Valid @RequestBody`, and `@Valid` must cascade onto nested DTOs/collections for the constraints to fire. Keep **async message DTOs** (the `CommandPayload` family) annotation-light — message validation is policy in the messaging adapters, and those DTOs are reused across transports.
- `embdeddable` typo in the persistence module is a separate concern — do not "fix" identifiers here to match it.
