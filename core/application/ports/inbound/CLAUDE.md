# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Module Purpose

`trade-loan-core-application-ports-inbound` — driving-side (inbound) port of the hexagonal architecture for the trade-loan service. Defines the **command contract** that driving adapters (REST, Kafka, ActiveMQ) send into the application service layer. No interfaces, no handlers, no logic — just immutable command + DTO shapes plus Jakarta validation rules.

This module is consumed by:
- All `adapters/driving/*` modules (they construct and dispatch these commands)
- `core/application/service` (handlers receive these commands via the platform dispatcher)

It must NOT depend on `core/application/ports/outbound`, the domain, or any adapter.

## Build & Test

```bash
# From this directory
mvn clean install -DskipITs=true

# From repo root, build only this module + deps
mvn -pl core/application/ports/inbound -am clean install
```

Only test dependency is `spring-boot-starter-test`. Module has no production tests today; any added must stay isolated (validate annotations / record shape, never reach into the domain).

## Code Layout

```
src/main/java/ir/dotin/loan/trade/core/application/ports/inbound/
├── command/   # *Command records implementing platform `Command`
└── dto/       # Shared value DTOs referenced by multiple commands
```

## Conventions (Critical)

1. **Commands are Java records** that `implements ir.dotin.platform.dispatcher.api.command.Command`. The dispatcher routes them to handlers in the service module by type.

2. **Every command carries `UUID uid` and `Long version`** as the first two components — `uid` is the dispatcher idempotency/correlation key, `version` is the optimistic-lock expectation against the target aggregate (`@Nullable` for creation commands like `OriginateLoanFacilityCommand`, required for state-changing ones).

3. **Lombok `@Builder(toBuilder = true)`** on every command and nested DTO. Callers always go through the builder; do not add positional factory methods.

4. **Validation is declarative.** Use `jakarta.validation` annotations (`@NotNull`, `@NotBlank`, `@NotEmpty`, `@Valid`, `@Pattern`, `@DecimalMin`). The dispatcher / adapter triggers validation before the handler runs — never re-validate in the handler.

5. **Nested DTOs live inside the owning command record** when they are command-specific (see `OriginateLoanFacilityCommand.LoanApplicationDto`). Promote a DTO to the shared `dto/` package only when ≥2 commands reuse it.

6. **Anti-corruption boundary.** This module speaks **domain terminology only** (`applicationNumber`, `loanFacilityId`, `loanArrangementCode`). Legacy / external terms (e.g. `fileNumber`) must be translated in the driving adapter's mapper and never leak into a command field.

7. **Compensation pairs.** Each Saga step has a `*Command` and a matching `Compensate*Command` (e.g. `OriginateLoanFacilityCommand` ↔ `CompensateOriginationCommand`). Add them together; compensation commands carry `loanFacilityId`, `version`, and a `String reason`. Keep the Javadoc on compensation commands noting the valid source state (e.g. `/** Valid from: APPLICATION_SUBMITTED */`).

8. **No business logic, no defaults, no behavior.** Records are pure data carriers. Lookups, ID resolution, and orchestration belong in `core/application/service`. If you feel pulled to put a method on a command other than the generated record accessors / builder, the logic belongs in a handler.

9. **Imports from `baseloan`** are limited to enums (`ApplicantChannel`, `DisbursementMethod`, `CollateralType`). Do not import baseloan entities or VOs — translate them into local DTOs.

## Command Catalog (high-level)

The records here form the trade-loan lifecycle saga; reading the filenames in `command/` gives the full state-machine surface. Key clusters:

- **Origination:** `OriginateLoanFacilityCommand`, `SubmitFacilityForApprovalCommand`, `ApproveFacilityCommand`, `RejectFacilityCommand`, `CancelFacilityCommand`
- **Setup:** `DefineLoanTypeCommand`, `DefineTradeLoanArrangementCommand`, `AddFacilityCollateralCommand`, `UpdateCollateralCommand`, `PlanEqualInstallmentScheduleCommand`, `IssueFacilityContractCommand`
- **Disbursement:** `LumpSumDisbursementCommand`, `RegularDisbursementCommand`, `IrregularProgressiveDisbursementCommand`
- **Collection / closure:** `CollectInstallmentCommand`, `CloseFacilityPaidOffCommand`, `CloseFacilityDefaultedCommand`, `LoanFacilityRestructuringCommand`
- **Orchestrators:** `FullLoanFacilityLifecycleCommand`, `FullLifecycleRevertCommand` — composite saga entry points
- **Compensations:** every `Compensate*Command` (origination, approval, contract issuance, lump-sum / irregular disbursement, collateral, collect-installment, close-paid-off, approval-submission)

When adding a new lifecycle step, expect to add: forward command + compensation command + (if needed) shared DTO + a builder test in the service module.

## Don't

- Don't add Spring `@Component`, `@Service`, or any framework wiring here — module must remain a plain `jar` with no Spring context.
- Don't reference outbound ports, repositories, or domain aggregates.
- Don't add MapStruct mappers; mapping from external payloads → commands belongs in the calling driving adapter.
- Don't widen validation to "soft" checks (length warnings, business rules). Validation here is **shape only**; business invariants are enforced by the domain.
