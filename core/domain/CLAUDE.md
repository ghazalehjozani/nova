# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Module Role

`trade-loan-core-domain` is the **pure domain layer** of the trade-loan service. It contains aggregates, value objects, domain events, specifications, and domain services for Morabehe (trade) loans. It extends the shared kernel `base-loan-core-domain` (abstract `AbstractLoanFacility`, `AbstractLoanType`, `LoanFacilityEventFactory`, etc.) with trade-specific behavior.

Zero framework, persistence, or transport dependencies — only `base-loan-core-domain`, `accounting-document-core` (platform), Guava, JSpecify annotations, and SLF4J. No Spring, no JPA, no HTTP. This module is consumed by `core/application/service` and friends; it must remain consumable as a plain library.

## Build & Test

```bash
# From this module
mvn clean test                                              # unit tests
mvn test -Dtest=TradeLoanArrangementTest                    # single test class
mvn test -Dtest=TradeLoanArrangementTest#methodName         # single test method
mvn clean install                                           # publish to local repo for downstream modules
```

Architecture tests live in `architecture-tests/`, not here — run them from that module.

## Package Layout (4 bounded sub-areas + shared)

```
ir.dotin.loan.trade.core.domain
├── loanfacility/        # TradeLoanFacility aggregate (the central root)
│   ├── entity/          # TradeLoanFacility, TradeLoanApplication, TradeSanctionedLoan, TradeLoanFacilityEventFactory
│   ├── event/           # 24 facility lifecycle events (Created, Approved, Disbursed, Reverted, …)
│   ├── specification/   # CollateralContractIssuanceEligibility, DisbursementMethodCompatibility
│   ├── service/         # TradeLoanFacilityService + validator/ (FacilityContractValidation, sanction validators)
│   ├── error/, vo/      # domain errors, ExistResult
├── loantype/            # TradeLoanType aggregate (product config)
│   ├── entity/          # TradeLoanType (extends AbstractLoanType)
│   ├── specification/   # MandatoryRelationTypeLoanTopicSpecification
│   ├── service/         # TradeLoanTypeValidationService
│   ├── event/, enums/   # incl. TradeRelationType
├── loanarrangement/     # TradeLoanArrangement entity (the concrete arrangement)
├── installmentschedule/ # TradeRepaymentSchedulingService + events + intraction
└── shared/
    ├── document/        # Accounting-document construction for trade flows
    │   ├── strategy/    # DocumentCalculationStrategy implementations + DisbursementStrategyProvider
    │   ├── factory/     # ArticleSpec factories (BankCommitment, DisbursedInterest, PaymentAmount, IssueContractBankCommitment)
    │   ├── builder/     # TradeArticleComponentBuilder
    │   ├── config/      # Per-strategy metadata configs
    │   ├── enums/, transaction/
    ├── formula/         # TradeLoanParameterProvider (implemented by TradeLoanFacility)
    ├── intraction/      # FindLoanArrangementByIdClient — domain-owned outbound interface for cross-aggregate lookup
    └── error/
```

## Key Design Patterns

- **Aggregate root via base-class extension.** `TradeLoanFacility extends AbstractLoanFacility<TradeLoanApplication, TradeSanctionedLoan, Builder>` — the shared kernel defines the protocol; trade specializes the type parameters and adds trade-only state. Same for `TradeLoanType extends AbstractLoanType`. **Always go through `AbstractLoanFacility` first** when touching facility behavior — most lifecycle logic lives there.
- **Builder + private constructor + static factory.** Aggregates are immutable-ish; mutation goes through methods that emit events. Construction is always `TradeLoanFacility.create(...)` (factory) or `builder().…buildInternal()` (rehydration). Never `new`.
- **Domain events as records/classes under `*/event/`.** `TradeLoanFacilityEventFactory` is the sole producer — do not instantiate events directly outside the factory and the aggregate.
- **Specifications (`*/specification/`)** encapsulate predicate-style business rules; they return `Result`/`Notification` (from `ir.dotin.platform.commons.core`) rather than throwing.
- **Document calculation strategies** under `shared/document/strategy/` are the trade-side implementations of `ir.dotin.platform.accounting.document.api.strategy.DocumentCalculationStrategy<TradeLoanFacility, TradeRelationType, …>`. `DisbursementStrategyProvider` (interface) + `DefaultDisbursementStrategyProvider` (impl) compose the active strategies per facility. Each strategy has a paired `factory/*ArticleSpecFactory` and `config/*MetadataConfig`. Add a new accounting flow → add strategy + factory + config + register in provider.
- **`Result<T>` and `Notification`** from `ir.dotin.platform.commons.core` are the canonical error-collection pattern. Domain code should accumulate violations in a `Notification` and return `Result<T>` rather than throwing exceptions for business rule failures. `requireNonNull` / `checkState` are reserved for programmer-error invariants.
- **`Clock` is injected** into every factory method that records time — do not call `Instant.now()` or `LocalDate.now()` directly. Tests pass a fixed `Clock`.
- **Domain-owned outbound interfaces** live under `shared/intraction/` (note the spelling — kept as-is for consistency, e.g. `FindLoanArrangementByIdClient`). Adapters implement them in `adapters/driven/*`. This module defines the contract; it never imports an adapter.

## Conventions That Will Bite You

- **Module is `jspecify`-annotated** (`@NonNull` / `@Nullable` from `org.jspecify.annotations`). Honor the annotations — downstream relies on them.
- **No Lombok.** Builders are hand-written to match the shared-kernel base builder contract.
- **`buildInternal()` vs `build()`**: factories use `buildInternal()` to skip the public-build invariants that don't apply at construction time. Don't change one to the other without reading the base class.
- **Events live in `event/` siblings of `entity/`**, not nested inside the entity. `TradeLoanFacilityEvents` is the registry/catalogue.
- **`shared/document/` is large and load-bearing.** Read `DisbursementStrategyProvider` + `DefaultDisbursementStrategyProvider` before adding a new disbursement variant; the strategy set composes the accounting articles emitted on disbursement.

## When Extending Behavior

1. New facility lifecycle transition → add event class under `loanfacility/event/`, register in `TradeLoanFacilityEvents`, add factory method to `TradeLoanFacilityEventFactory`, expose method on `TradeLoanFacility` (or `AbstractLoanFacility` if generic).
2. New business rule → write a `*Specification` (returns `Notification`/`Result`) and invoke it from the aggregate or a `*ValidationService`.
3. New accounting article on disbursement → add a `Strategy` + `ArticleSpecFactory` + `MetadataConfig`, then register in the strategy provider.
4. New cross-aggregate read → declare a domain-owned interface in `shared/intraction/`; do NOT call adapters or repositories from domain code.
