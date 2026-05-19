# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Module Purpose

`trade-loan-core-application-ports-outbound` — **driven (outbound) port** interfaces for the trade-loan service. Pure contract module: defines what the application layer *needs* from the outside world (persistence, core-banking clients, query resolvers). Implementations live in `adapters/driven/*`.

This module is the **outbound side of the hexagon**. It is consumed by `core/application/service` (command handlers) and `core/application/query`. It must **not** be consumed by driving adapters (see root `CLAUDE.md` dependency rules — driving adapters depending on this module is forbidden).

## Build

```bash
mvn -pl core/application/ports/outbound -am clean install
mvn -pl core/application/ports/outbound test
```

No `src/test` exists yet — the only declared test dep is `spring-boot-starter-test`. Contract module rarely needs unit tests; behaviour belongs in implementations.

## Package Layout

```
ir.dotin.loan.trade.core.application.ports.outbound
├── client/                       # ports to external/core-banking services
│   ├── accountservice/           # AccountServicePort, TransactionPostingPort
│   ├── customerservice/          # CustomerServicePort
│   ├── depositservice/           # DepositServicePort
│   ├── loanservice/              # LoanServicePort, CollateralServicePort
│   ├── samat/                    # ValidateSamatPort (regulator)
│   ├── request/                  # input DTOs for the ports above
│   ├── response/                 # output DTOs returned by the ports
│   ├── error/                    # CoreBankingErrors (shared error catalogue)
│   ├── ConcurrentApplicationOperationException.java
│   └── (loose ports) FindOrCreateAccountPort, FindAccountByIdPort, FetchSanctionDetailsPort
├── command/repository/           # aggregate repositories used by command handlers
│   ├── TradeLoanFacilityRepository
│   ├── TradeLoanArrangementRepository
│   ├── TradeLoanTypeRepository
│   └── InstallmentScheduleRepository
└── query/                        # lightweight read-side resolvers for command handlers
    └── ApplicationNumberResolver
```

Three sub-port families with distinct contracts:

1. **`command/repository/`** — aggregate-root repositories returning domain entities (e.g. `TradeLoanFacility`). Persistence adapter implements these.
2. **`client/`** — gateway interfaces over external systems. Return `Result<T>` (`ir.dotin.platform.commons.core.Result`) to carry domain errors without exceptions. DTOs in `request/` + `response/` are plain Java records/classes — not domain VOs.
3. **`query/`** — narrow read-side ports for command handlers that need to translate legacy identifiers (e.g. `applicationNumber` → `LoanFacilityId`). Distinct from the broader `core/application/query` module which serves driving adapters.

## Conventions

- **Domain terms only.** No legacy terminology (e.g. `fileNumber`) in port signatures. Anti-corruption translation belongs in the implementing adapter (see root `CLAUDE.md` rule 3).
- **`Result<T>` over exceptions** for client ports — see `CoreBankingErrors` for the shared catalogue. Repository ports use `Optional<T>` for lookup and throw for true errors.
- **DTOs are local to this module.** `request/` and `response/` types are inputs/outputs of these ports — do not leak into domain code. Domain VOs (e.g. `LoanFacilityId`, `AccountInfo`, `ApplicationNumber`) come from `base-loan` or `trade core/domain`.
- **No Spring annotations** — ports are pure interfaces. DI wiring happens in adapter modules.
- **Adding a new port:** drop it under the matching `client/<service>/` package; create the subpackage if integrating a new external system. Put DTOs in shared `request/` and `response/` unless the system is large enough to warrant its own DTO package (none yet).

## Dependencies

- `base-loan` domain VOs (`LoanFacilityId`, `AccountInfo`, `LoanTopic`, `ApplicationNumber`, …).
- `trade core/domain` aggregates (`TradeLoanFacility`, …).
- `ir.dotin.platform.commons.core.Result` and `ir.dotin.platform.accounting.document.api.model.*` (`AccountId`, `AccountNumber`, `BranchCode`).
- Jakarta validation annotations on port parameters.

No Spring, no JPA, no MapStruct. Keep this module dependency-light.
