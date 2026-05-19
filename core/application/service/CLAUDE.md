# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Module: `trade-loan-core-application-service`

Application-service layer for the trade-loan (Morabehe) bounded context. Houses **use-case orchestration**: command handlers, saga orchestrators, compensation handlers, and supporting strategies/mappers/configuration. No HTTP, no persistence, no domain rules — those live elsewhere.

Maven coordinates: `ir.dotin.loan:trade-loan-core-application-service` (jar). Parent: `trade-loan-core-application`.

## See also

- Read side: [`core/application/query`](../query/CLAUDE.md)
- Command contracts: [`core/application/ports/inbound`](../ports/inbound/CLAUDE.md)
- Outbound contracts: [`core/application/ports/outbound`](../ports/outbound/CLAUDE.md)
- Domain: [`core/domain`](../../domain/CLAUDE.md)

## Dependencies (from `pom.xml`)

- `trade-loan-core-application-ports-inbound` — `Command` types this module handles.
- `trade-loan-core-application-ports-outbound` — repository / external-service interfaces this module calls.
- `ir.dotin.platform:platform-saga-api` — `@SagaHandler`, `SagaDefinition`, `SagaStep`, `CompensationMode`, `ExecutionStrategy`.
- `ir.dotin.platform:expression-kit-service` — formula evaluation (interest, schedules) via `TradeLoanFormulaEvaluationService`.
- `org.springframework:spring-tx` — `@Transactional` boundaries on handlers.
- `spring-boot-starter-test` (test scope) — no tests currently checked in.

Transitively pulls `base-loan-core-domain` through the ports modules (domain VOs like `LoanFacilityId`, `LoanArrangementCode`, `LoanTypeCode`, `DisbursementMethod`, `ConfirmType` are used directly).

## Package Layout — Use-Case-per-Folder

Root package: `ir.dotin.loan.trade.core.application.service`.

Every use case is its own subpackage. Inside each, the same set of role-folders recurs:

```
<usecase>/
├── commandhandler/   # CommandHandler<XCommand> — orchestrates one command
├── mapper/           # Command ↔ domain VO mappers (MapStruct or hand-written)
├── compensation/     # CompensateXCommandHandler — saga rollback path
├── saga/             # SagaInput / SagaData / Saga / Step (multi-step use cases only)
├── configuration/    # @Configuration bean — SagaDefinition, strategy wiring
├── strategy/         # Strategy interface + impls for branching behavior
├── factory/          # Factory selecting the right strategy
├── orchestrator/     # In-process orchestrator (non-saga) — see originateloanfacility
├── component/        # Reusable building blocks scoped to that use case
└── i18n/             # ErrorCodes / localized message keys
```

Use cases present (15):

`defineloantype`, `defineloanarrangement`, `originateloanfacility`, `submitfacilityforapproval`, `approvefacility`, `rejectfacility`, `cancelfacility`, `addfacilitycollateral`, `updatefacilitycollateral`, `planequalinstallmentschedule`, `issuefacilitycontract`, `regulardisbursement`, `lumpsumdisbursement`, `irregularprogressivedisbursement`, `collectinstallment`, `restructuringfacility`, `closefacilitypaidoff`, `closefacilitydefaulted`, `fullloanlifecycle`.

### Shared
`shared/` is cross-use-case:
- `shared/error/TradeLoanApplicationServiceErrors` — central error-code enum used by every handler.
- `shared/account/` — `AccountResolutionService`, deposit/account lookup helpers.
- `shared/formula/` — `TradeLoanFormulaEvaluationService` (wraps expression-kit).
- `shared/util/` — `DocumentMetadataUtils`, etc.

## Idioms (must follow when adding handlers)

### Command handler skeleton

```java
@Service
@RequiredArgsConstructor
public class XCommandHandler implements CommandHandler<XCommand> {
    private final TradeLoanFacilityRepository facilityRepo;
    // ... other outbound ports

    @Override
    public Result<List<DomainEvent<?>>> handle(XCommand command) {
        return Result.fromOptional(
                facilityRepo.findById(LoanFacilityId.of(command.loanFacilityId())),
                () -> Notification.ofError(TradeLoanApplicationServiceErrors.FACILITY_NOT_FOUND, command.loanFacilityId()))
            .flatMap(facility -> /* domain call returning Result */)
            .map(ignored -> { facilityRepo.save(facility); return facility.domainEvents(); });
    }
}
```

- Return type is **always** `Result<List<DomainEvent<?>>>` (from `ir.dotin.platform.commons.core.Result` + `DomainEvent`).
- Errors go through `Notification.ofError(ErrorCode, args...)` — never throw.
- Use `Result.fromOptional` / `.flatMap` / `.map` — no nested `if (opt.isPresent())`.
- Persist via outbound repo, then return `facility.domainEvents()` so the dispatcher publishes them.
- Lombok `@RequiredArgsConstructor` for DI; constructor injection only.

### Saga handlers (multi-step use cases)

`fullloanlifecycle/` and `issuefacilitycontract/` are the reference sagas. Pattern:
- `XInput` (saga input record) + `XSagaData` (mutable saga state) + `XStep` (enum of step ids) + `XSaga` (the `@SagaHandler`-annotated class returning a `SagaDefinition`).
- Each `SagaStep` references a `Command` and its compensating command.
- Compensation commands live in `<usecase>/compensation/` and implement `CommandHandler<CompensateXCommand>`.
- `<usecase>/configuration/` exposes the `SagaDefinition` as a Spring bean.

### Strategy + factory
Branching behavior (approval mode, schedule type, application-number source) uses `strategy/` + `factory/` (e.g. `ApprovalStrategyFactory` → `AutoApprovalStrategy` / `ManualApprovalStrategy`, `InstallmentScheduleStrategySelector` → `StandardScheduleStrategy` / `GradualScheduleStrategy`, `ApplicationNumberStrategySelector` → `Internal/ExternalApplicationNumberGenerationStrategy`). Add new variants by implementing the strategy interface and registering with the selector/factory.

## Architectural Boundaries (do NOT cross)

- This module depends only on **inbound ports** (commands), **outbound ports** (repositories/clients), shared platform libs, and `base-loan-core-domain` (transitively). It must **not** import any adapter package (`adapters.*`) or `container.*`.
- No HTTP / Servlet / Kafka / JPA types here — those belong in adapters. If a handler needs them, the access goes through an outbound port.
- Per repository-root rule: ID resolution and validation against domain state happen **here**, never in driving adapters. New use cases that resolve legacy IDs (e.g. `fileNumber` → `applicationNumber`) must do it via an outbound port called from the handler.

## Build / Test

Standard Maven from repo root or this directory:

```bash
mvn -pl core/application/service -am clean install   # build with deps
mvn -pl core/application/service test                 # tests (currently none)
```

Java sources under `src/main/java`; no `src/test/java` content presently. When adding tests, mirror the use-case folder structure under `src/test/java`.
