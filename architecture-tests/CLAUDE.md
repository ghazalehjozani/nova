# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Module Role

`trade-architecture-tests` is the **structural-rule gate** for the trade-loan service. It contains no production code — only ArchUnit tests that import every sibling module on the test classpath and enforce hexagonal-architecture invariants against the compiled bytecode of those modules.

Today it ships **one** test: `TradeLoanDomainArchitectureTest`, which validates the `core/domain` module against the platform's `BaseDomainArchitectureTest` (aggregate, entity, value-object, factory, domain-service rules from `ir.dotin.platform.archunit.domain.*`). The POM already pulls in `core/application/ports/{inbound,outbound}`, `core/application/service`, `adapters/driving/rest`, and `adapters/driven/persistence` as test-scope deps — the corresponding `BaseApplicationArchitectureTest` / `BasePortArchitectureTest` / adapter-layer tests are **expected to be added here**, not in each module. The driven-messaging dep is commented out in `pom.xml` and should be uncommented once those tests are added.

## Build / Test

See root [CLAUDE.md → Build & Test](../CLAUDE.md#build--test-single-source-of-truth). Maven path: `architecture-tests`. Single class: `mvn -pl architecture-tests test -Dtest=TradeLoanDomainArchitectureTest`. Single rule: append `#methodName`. CI picks these up via root's `mvn test -Dtest=**/*ArchitectureTest*`.

## How the Tests Are Wired

```
ir.dotin.platform.archunit.domain.BaseDomainArchitectureTest   (platform-test-archunit)
        ▲
        │ extends
        │
TradeLoanDomainArchitectureTest
  @AnalyzeClasses(packages = "ir.dotin.loan.trade.core.domain",
                  importOptions = ImportOption.DoNotIncludeTests.class)
  getConfiguration() = ArchUnitConfiguration.builder("ir.dotin.loan.trade")
                          .withAdditionalAllowedPackage("ir.dotin.loan.baseloan.core.domain..")
                          .build()
```

Key points:

- **`@AnalyzeClasses` scope.** The package string scopes which bytecode ArchUnit imports. Domain rules scan only `ir.dotin.loan.trade.core.domain`; adapter / application tests must use their own package roots. `DoNotIncludeTests.class` keeps test classes out of the analyzed set.
- **`ArchUnitConfiguration` is the seam for project-specific allowlists.** The root package (`ir.dotin.loan.trade`) is the boundary; `withAdditionalAllowedPackage(...)` opens *narrow* doors for legitimate cross-bounded-context deps. Currently the only allowance is the shared kernel `ir.dotin.loan.baseloan.core.domain..` — domain rules treat references into the kernel as in-bounds. **Adding another allowance is a design decision: justify it in the commit message; do not casually whitelist a leak.**
- **Rules are inherited, not redefined here.** The `Base*ArchitectureTest` superclasses pull in named rule groups from `DomainRules`, `EntityRules`, `ValueObjectRules`, `AggregateRules`, `FactoryRules`, `DomainServiceRules`, `DomainLayerRules`. To debug a failing rule, read the matching `*Rules` class in `platform-test-archunit` sources (already extracted at `/tmp/archunit-src/` during this session, otherwise pull the `-sources.jar` from `~/.m2/repository/ir/dotin/platform/platform-test-archunit/`).

## Adding a New Architecture Test

One subclass per layer of the hexagon. Follow the existing pattern:

1. **Pick the right base class** from `ir.dotin.platform.archunit.*`:
   - `BaseDomainArchitectureTest` → domain module (already in place).
   - `BaseApplicationArchitectureTest` → `core/application/service` + use-case orchestration.
   - `BasePortArchitectureTest` → `core/application/ports/{inbound,outbound}`.
   - Adapter-layer base classes (if/when the platform exposes them) → driving / driven adapters.
2. **Annotate** with `@AnalyzeClasses(packages = "<target-package>", importOptions = ImportOption.DoNotIncludeTests.class)`.
3. **Override `getConfiguration()`** returning an `ArchUnitConfiguration` rooted at `ir.dotin.loan.trade`. Add `withAdditionalAllowedPackage(...)` only for legitimate, justified cross-package references (e.g. the shared kernel).
4. **Verify the module is a test-scope dependency in `pom.xml`.** ArchUnit needs the bytecode on the classpath — without the dep, the rules silently see zero classes and pass vacuously. If you uncomment the `trade-loan-adapters-driven-messaging` block, add the matching test class in the same commit.
5. **Do not add production code.** Packaging is `jar` only because Maven requires it; this module ships no runtime artifact.

## Conventions That Will Bite You

- **Vacuous passes.** A rule scoped to a package containing zero classes (typo in the package, dep missing from `pom.xml`) passes silently. After adding a new test, deliberately introduce a violation locally to confirm the rule actually fires before committing.
- **`ImportOption.DoNotIncludeTests.class` is non-negotiable.** Without it, test classes (including fixtures with relaxed conventions) pollute the analysis and produce spurious failures or hide real ones.
- **The configuration root package is `ir.dotin.loan.trade`, NOT `ir.dotin.loan.trade.core.domain`.** The `@AnalyzeClasses` package scopes *what gets imported*; the configuration root scopes *what counts as "our code"* for purposes of the allowed-package check. Mixing them up loosens the rules.
- **`base-loan` shared kernel is the only currently allowed external domain package.** Anything else (`ir.dotin.platform..` accounting, commons, etc.) is already handled by the platform's default allowlist inside `ArchUnitConfiguration`; do not duplicate.
- **No Spring, JPA, or framework imports.** Even though this is a test module, the rules under test forbid framework leakage into domain — adding such a dep here would not break compilation but would defeat the point of the gate.

## Where Things Live

```
architecture-tests/
├── pom.xml                                          # test-scope deps on every module under test
└── src/test/java/ir/dotin/loan/trade/architecture/
    └── TradeLoanDomainArchitectureTest.java        # only test today; add siblings here
```

No `src/main/`. No resources. No production output.
