# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Module: `trade-loan-core-application-service`

Application-service layer for the trade-loan (Morabehe) bounded context. Houses **use-case orchestration**: workflow command handlers, step components, compensation handlers, and supporting strategies/mappers/configuration. No HTTP, no persistence, no domain rules — those live elsewhere.

Maven coordinates: `ir.dotin.loan:trade-loan-core-application-service` (jar). Parent: `trade-loan-core-application`.

## See also

- Read side: [`core/application/query`](../query/CLAUDE.md)
- Command contracts: [`core/application/ports/inbound`](../ports/inbound/CLAUDE.md)
- Outbound contracts: [`core/application/ports/outbound`](../ports/outbound/CLAUDE.md)
- Domain: [`core/domain`](../../domain/CLAUDE.md)

## Orchestration model (pangaea-workflow)

Every command handler extends `WorkflowCommandHandler<C, D>` (`ir.dotin.platform.pangaea.workflow.api.command`). The handler is a **declaration**: its first method is `route(WorkflowRoute<D> route)` — a Camel-style fluent route that reads like a flowchart — followed by `seed(command)`; fields and constructor come last. Step bodies live in `@Component` classes under `<usecase>/step/`, never inline in the handler.

```java
@Override
protected Workflow<LumpSumData> route(WorkflowRoute<LumpSumData> route) {
    return route.type("lump-sum-disbursement")
            .read(LumpSumDisbursementStep.VALIDATE_FACILITY, validateFacilityStep)
            .remote(LumpSumDisbursementStep.RESOLVE_ACCOUNTS, resolveAccountsStep)
            .retry(RetryPolicy.CONSERVATIVE).timeout(Duration.ofSeconds(30))
            .remote(LumpSumDisbursementStep.POST_TRANSACTIONS, postTransactionsStep)
            .retry(RetryPolicy.CONSERVATIVE).timeout(Duration.ofSeconds(30))
            .write(LumpSumDisbursementStep.APPLY_DISBURSEMENT, applyDisbursementStep)
            .build();
}
```

- Step components implement `ReadActivity<D>` / `RemoteActivity<D>` / `WriteActivity<D>`, plus `Compensable<D>` when they compensate — the route **auto-wires** compensation from `Compensable`; no explicit `.compensatedBy` for components.
- Single-write use cases return `route.singleWrite("<type>", ctx -> ...)` (ephemeral fast-path — no run rows).
- **Durable invariants:** `workflowType` strings, step-id enum values (`<usecase>/workflow/XStep`), and step order are frozen — durable runs exist in production. Declaration-shape tests lock them.
- **Remote-IO rules (runtime-enforced):** handlers (any `WorkflowCommandHandler` subclass) must NOT inject a `RemotePort`-marked port as a field — startup verifier fails boot. `RemoteWritePort` methods run only inside `RemoteStep` bodies/compensations; `RemoteReadPort` is free in `seed()`/prepare components; see [`ports/outbound`](../ports/outbound/CLAUDE.md).

## Package Layout — Use-Case-per-Folder

Root package: `ir.dotin.loan.trade.core.application.service`.

```
<usecase>/
├── commandhandler/   # WorkflowCommandHandler subclass: route() + seed() only (+ seed assembler if heavy)
├── step/             # @Component step bodies: execute(ctx) (+ compensate(ctx)) — own the remote/repo ports
├── workflow/         # XStep enum (WorkflowStepId) + XData record — FROZEN for durable workflows
├── compensation/     # CompensateXCommandHandler — operator-driven revert commands (REST), also workflow-backed
├── component/        # seed-phase loaders/validators/builders scoped to the use case
├── mapper/           # Command ↔ domain VO mappers (MapStruct or hand-written)
├── strategy/ + factory/  # branching behavior (approval mode, schedule type, app-number source)
├── configuration/    # @Configuration / @ConfigurationProperties for that use case
└── i18n/             # per-usecase error codes
```

Use cases (20): `addfacilitycollateral`, `approvefacility`, `cancelfacility`, `closefacilitydefaulted`, `closefacilitypaidoff`, `collectinstallment`, `defineloantype`, `definetradeloanarrangement`, `irregularprogressivedisbursement`, `issuefacilitycontract`, `loanfacilityrestructuring`, `lumpsumdisbursement`, `originateloanfacility`, `planequalinstallmentschedule`, `regulardisbursement`, `rejectfacility`, `submitfacilityforapproval`, `updatefacilitycollateral` + `interaction/`, `configuration/`.

### Shared (`shared/`)
- `shared/disbursement/` — `FacilityDependencyLoader` (facility/loanType/arrangement/schedule loads), `AccountResolutionSupport` (resolve/close accounts), `TransactionPostingSupport` (post/reverse + tracked-number rebuild; owns `TransactionPostingPort`) — reused by all disbursement-family steps.
- `shared/account/` — `AccountResolutionService`, `LoanTopicResolver`, `FcbTransactionReverser`.
- `shared/authz/` — `BranchAccessValidator` (seed-phase branch coverage check).
- `shared/error/TradeLoanApplicationServiceErrors` — central error-code enum.
- `shared/formula/` — `TradeLoanFormulaEvaluationService` (wraps expression-kit).
- `shared/util/` — `DocumentMetadataUtils`, etc.

### Reference use cases
- Multi-step durable: `lumpsumdisbursement` (cleanest), `irregularprogressivedisbursement`, `issuefacilitycontract`, `addfacilitycollateral`.
- Single-write ephemeral: `originateloanfacility` (composite `FacilityValidator` fans out read-side validation rules under `component/validation/` via `ParallelFanout`).

## Idioms (must follow when adding handlers)

- Handler = `route()` first, `seed()` second, fields/ctor last; explicit constructor calling `super(engine)`; no Lombok on handlers.
- Step components: `@Component` + `@RequiredArgsConstructor`, method shapes `StepResult<Void> execute(WorkflowContext<D>)` (read/remote), `StepResult<List<DomainEvent<?>>> execute(...)` (write), `StepResult<Void> compensate(...)`.
- Errors via `Notification.ofError(ErrorCode, args...)` carried in `Result` — never throw; `Result.fromOptional`/`flatMap`/`map` chains.
- Remote WRITES only in remote-step bodies/compensations; pre-validation remote READS in `seed()` via `component/` collaborators.
- No code comments/Javadoc.

## Tests

Mirror use-case folders under `src/test/java` (JUnit 5 + Mockito + AssertJ, no Spring context):
- Declaration-shape test per durable handler — asserts `definition().workflowType()` and the exact ordered step-id list (durable-compat lock).
- One focused test class per step component (execute + compensate with mocked ports).
- Validation-rule tests + composite aggregation test for `originateloanfacility/component/validation`.

## Architectural Boundaries (do NOT cross)

- Depends only on **inbound ports** (commands), **outbound ports**, pangaea platform libs, and `base-loan-core-domain` (transitively). Never import `adapters.*` or `container.*`.
- No HTTP / Servlet / Kafka / JPA types here — access goes through outbound ports.
- ID resolution and validation against domain state happen **here**, never in driving adapters.

## Build / Test

See root [CLAUDE.md](../../../CLAUDE.md#build--test-single-source-of-truth). Module path: `core/application/service`.
