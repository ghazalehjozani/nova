# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Module Role

`architecture-docs` (Maven artifactId) is the **C4 model generator** for the trade-loan service. It is a tiny Java application — not a runtime dependency — whose only job is to scan the bytecode of every sibling Maven module, infer C4 model elements (System, Containers, Components, relationships) from package layout + annotations + class-name conventions, merge ADRs from `/documents/adr/`, and emit a Structurizr workspace plus rendered diagrams (Mermaid, PlantUML, DOT) into the surrounding `documents/c4/` tree.

Output is consumed by Structurizr Lite (see `../run-structurizr.sh`, which docker-runs `structurizr/lite` against `documents/c4/`). The generated artifacts (`workspace.json`, `generated-workspace.dsl`, `dot/*.dot`, `mermaid/*.mmd`, `plantuml/*.puml`) are **committed to the repo** — regenerate them by running this module.

**Scope rule:** the parent `documents/CLAUDE.md` (`SAW_102 v1.0` Architecture Documentation Standards) governs what counts as a documentation artifact, where it lives, and how it is formatted (Mermaid for ADRs/cmap, draw.io on Confluence for C4 hand-drawn diagrams, OpenAPI/AsyncAPI conventions). That doc applies to the **human-authored** docs under `/documents/`. This CLAUDE.md covers the **generator code** that emits the auto-generated C4 artifacts. Both apply to anything in this module — there is no conflict because the generator produces machine-readable C4 (Structurizr DSL + Mermaid/PlantUML/DOT exports), which is supplementary to the draw.io diagrams referenced from `c4/README.md`.

## Build & Run

```bash
# From this module — regenerates documents/c4/{workspace.json, generated-workspace.dsl, dot/, mermaid/, plantuml/}
mvn clean package

# What that runs (exec-maven-plugin, phase=process-classes):
#   java -cp <all sibling module jars + structurizr + classgraph> \
#        ir.dotin.loan.trade.architecture.ArchitectureGenerator \
#        ${project.basedir}/../          # output dir = documents/c4/

# Direct invocation (after a build):
mvn exec:java -Dexec.mainClass=ir.dotin.loan.trade.architecture.ArchitectureGenerator \
              -Dexec.args="$(pwd)/.."

# View the generated workspace locally
( cd .. && ./run-structurizr.sh )       # opens Structurizr Lite at http://localhost:8090
```

No tests today. There is no `src/test/`; the module is exercised entirely via the `process-classes` exec binding.

**Dependency-scope rule.** Every sibling module appears in `pom.xml` with **default (compile) scope** — `test` scope would hide the bytecode from ClassGraph at runtime and the scan would silently produce zero components. When adding a new sibling module to the trade-loan build, add it to this `pom.xml` in compile scope or its components will be invisible to the diagrams.

## Generation Pipeline (read this before changing anything)

```
ArchitectureGenerator.main(outputDir = documents/c4)
  │
  ├─ DefaultConfig.load() ─────────────────► ArchitectureConfig
  │     ├─ workspace name + basePackage ("ir.dotin.loan.trade")
  │     ├─ persons (Loan Officer, Branch Manager, Sysadmin, Customer)
  │     ├─ external systems (TPS_SSO, FCB, OTEL, Account/Customer/Deposit/Collateral/Loan/FormulaEvaluator)
  │     ├─ main system + containers (App, Postgres, Kafka, Redis, Kafdrop)
  │     └─ styles (per-tag colors/shapes)
  │
  ├─ ModelBuilder.buildStaticModel()
  │     consumes config → creates Persons, Systems, Containers, "Uses" relations
  │
  ├─ ComponentScanner.scanComponents()          ← THE CORE
  │     ClassGraph().acceptPackages(basePackage).scan()
  │     ├─ scanRestControllers     @RestController OR package ".adapters.driving.rest" + name endsWith "Controller"
  │     ├─ scanMessageConsumers    @KafkaListener OR package ".adapters.driving.messaging" + "Consumer"
  │     ├─ scanCommandHandlers     name endsWith "CommandHandler"   (Compensate* → COMPENSATION tag)
  │     ├─ scanQueryHandlers       name endsWith "QueryHandler"
  │     ├─ scanSagas               endsWith "Saga", not "SagaData"
  │     ├─ scanAggregatesAndEntities  package ".core.domain.*.entity"  ← aggregate allowlist hard-coded
  │     ├─ scanDomainServices      package ".core.domain.*.service", endsWith "Service"
  │     ├─ scanRepositories        endsWith "RepositoryAdapter" OR "Jpa…QueryAdapter" in driven.persistence
  │     ├─ scanExternalClients     adapters.driven (not persistence) endsWith "Adapter"/"ServiceImpl"
  │     └─ scanOutboxHandlers      endsWith "OutboxHandler"
  │     dependencies extracted from FIELD types whose FQN starts with basePackage
  │
  ├─ ModelBuilder.buildComponents(mainContainer, scanned)
  ├─ ModelBuilder.buildComponentRelationships(...)   ← turns scanned `deps` into Structurizr "uses"
  ├─ ViewBuilder.buildAllViews()                     ← SystemContext, Containers, Components, Components_2..4, Controllers, Handlers, Sagas, SagaFlow, CommandFlow, QueryFlow, Repositories, ExternalClients, Messaging, DomainModel
  ├─ StyleBuilder.applyAllStyles()                   ← per-tag colors/shapes from DefaultConfig.styles
  └─ AdrImporter.importAdrs(workspace, adrPath)      ← walks up to 5 parents for documents/adr/ or ./adr/

WorkspaceExporter.exportJson  →  documents/c4/workspace.json
                  exportDsl   →  documents/c4/generated-workspace.dsl
                  exportMermaid → documents/c4/mermaid/*.mmd
                  exportPlantUML → documents/c4/plantuml/*.puml
                  exportDot    → documents/c4/dot/*.dot
```

## Detection Rules — What You Need to Know to Extend

The scanner is **convention-based**. A class becomes a C4 Component only if it matches one of the predicates above. Two filtering rules are easy to trip over:

- **`ComponentScanner.isValid()`** drops: interfaces, abstract classes, inner classes, anything ending in any of `EXCLUDED_SUFFIXES = {Test, Tests, IT, Config, Configuration, Properties, Mapper, Dto, DTO, Request, Response, Exception, Emb, Projection}`, anything containing `$` or `_`. So naming a real component `*Config` or `*Mapper` will silently hide it.
- **Domain detection looks at the package, not annotations.** Aggregates/entities must live under `.core.domain.*.entity`; services under `.core.domain.*.service`. The aggregate set is a **hard-coded allowlist** in `scanAggregatesAndEntities` (`TradeLoanFacility`, `TradeLoanArrangement`, `TradeLoanType`, `InstallmentSchedule`, `TradeLoanApplication`, `TradeSanctionedLoan`). New aggregates must be added there or they will be rendered as plain entities.

When extending:

1. **New component type** (e.g., projector, scheduler) → add a `scanX(...)` method, wire it from `scanComponents()`, define matching `Tags.*` in `ArchitectureConstants`, optionally add a `Map.entry(Tags.X, new ElementStyleConfig(...))` in `DefaultConfig.styles()`, and add a view in `ViewBuilder` if you want a dedicated diagram.
2. **New aggregate** → add the simple class name to the allowlist in `ComponentScanner.scanAggregatesAndEntities`.
3. **New container / external system / person** → edit `DefaultConfig.containers()` / `externalSystems()` / `persons()`. Names should reference `ArchitectureConstants` (`Systems.*`, `Containers.*`, `Persons.*`, `Technologies.*`, `Tags.*`) — do not hard-code strings.
4. **New diagram** → add a `build…View()` call in `ViewBuilder.buildAllViews()`; the corresponding files appear automatically under `dot/`, `mermaid/`, `plantuml/`.
5. **Verify after change**: rebuild this module, then inspect the scanner's stdout (`Controllers: N`, `Consumers: N`, …, `Total scanned: N`). A category dropping to 0 unexpectedly means a naming convention was broken or a sibling-module dep was moved to test scope.

## ADR Import

`AdrImporter` walks up to 5 parent directories looking first for `documents/adr/`, then `adr/`. If found, each Markdown file becomes a Structurizr `Decision` and ends up in `workspace.json`. If the path is not found, the run prints a warning and continues — the workspace is still emitted, just without decisions. Honor the Persian MADR template from the parent `documents/CLAUDE.md` (SAW_102 §4) for any new ADR; the importer treats them as opaque markdown.

## Conventions That Will Bite You

- **The exec binding is `process-classes`, not `verify` or `package`.** `mvn clean compile` already triggers generation. Running `mvn test` does *not* — there are no tests.
- **`workspace.json` and the rendered diagrams are version-controlled.** If you change scanner output, commit the regenerated files in the same change so reviewers can read the diff. Stale generated artifacts are a known footgun.
- **`structurizr.version = 3.2.0`** (property in `pom.xml`). The `structurizr-export` module is the source of the DOT/Mermaid/PlantUML writers — upgrade all three Structurizr artifacts together.
- **Console output uses emoji and ANSI-clean text.** The `process-classes` log lines (`📦 Packages found: N`, `🔍 Total scanned: N`) are the only feedback channel; preserve them when refactoring.
- **`ComponentScanner.basePackage = "ir.dotin.loan.trade"` (from `Packages.BASE`).** Anything outside that package (shared kernel `ir.dotin.loan.baseloan.*`, platform libs) is *intentionally* invisible. Do not widen the scan — those modules are represented as external systems / shared kernel relationships, not as components inside the trade-loan container.
- **No Spring runtime here.** Despite scanning Spring annotations, this generator runs as a plain `java` main; do not pull in `spring-boot-starter-*`. Annotations are read off bytecode by ClassGraph, not by an `ApplicationContext`.

## Source Layout

```
documents/c4/java/
├── pom.xml                                # artifactId architecture-docs; exec:java in process-classes
└── src/main/java/ir/dotin/loan/trade/architecture/
    ├── ArchitectureGenerator.java         # main(); orchestrates generate() + export()
    ├── ComponentScanner.java              # ClassGraph-based detection rules (see above)
    ├── ScannedComponent.java              # record: name, description, technology, tags, deps
    ├── builder/
    │   ├── ModelBuilder.java              # static model (persons/systems/containers) + component wiring
    │   ├── ViewBuilder.java               # all views (SystemContext, Containers, Components, flows, …)
    │   ├── StyleBuilder.java              # applies StyleConfig from DefaultConfig
    │   └── AdrImporter.java               # parents-walk for documents/adr or ./adr
    ├── config/
    │   ├── ArchitectureConfig.java        # nested records: WorkspaceConfig, PersonConfig, …, UsesConfig, StyleConfig
    │   ├── ArchitectureConstants.java     # Systems, Containers, Persons, Technologies, Tags (string constants)
    │   └── DefaultConfig.java             # the canonical wiring — edit here to change persons/containers/styles
    └── export/
        └── WorkspaceExporter.java         # JSON, DSL, Mermaid, PlantUML, DOT
```
