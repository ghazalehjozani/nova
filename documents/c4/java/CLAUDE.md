# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Module Role

`architecture-docs` (Maven artifactId) is the **C4 model generator** for the trade-loan service. It is a
tiny Java application — not a runtime dependency — whose only job is to discover C4 components from the
compiled bytecode of its sibling Maven modules using the official **`structurizr-component`** finder
(Structurizr 5.x, BCEL-based — no Spring context, no classloading), wire in the static model
(persons/systems/containers) from `DefaultConfig`, merge ADRs from `/documents/adr/`, and emit a
Structurizr workspace plus rendered diagrams (Mermaid, PlantUML, DOT) into the surrounding `documents/c4/`
tree.

Output is consumed by Structurizr Lite (see `../run-structurizr.sh`, which docker-runs `structurizr/lite`
against `documents/c4/`). The generated artifacts (`workspace.json`, `generated-workspace.dsl`, `dot/*.dot`,
`mermaid/*.mmd`, `plantuml/*.puml`) are **committed to the repo** — regenerate them by running this module.

**Scope rule:** the parent `documents/CLAUDE.md` (`SAW_102` Architecture Documentation Standards) governs
what counts as a documentation artifact and how it is formatted. This CLAUDE.md covers the **generator
code**. The generated machine-readable C4 (Structurizr DSL + Mermaid/PlantUML/DOT) is the supplement to any
hand-drawn draw.io diagrams referenced from `../README.md`.

## Build & Run

The generator is **deliberately OFF the main Maven lifecycle**. The module is declared only in the
`c4-docs` profile in the nova root `pom.xml`, so `mvn install/verify/package` never compiles or runs it
(no churn of committed diagrams on ordinary builds).

```bash
# Regenerate documents/c4/{workspace.json, generated-workspace.dsl, dot/, mermaid/, plantuml/}
make c4
#   = mvn -Pc4-docs -pl documents/c4/java -am -DskipTests process-classes
#   -am compiles the sibling modules first so their target/classes exist for discovery.
#   process-classes is the phase the exec is bound to (after compile, before tests).

# View the generated workspace locally
make c4-view            # = ( cd .. && ./run-structurizr.sh ) → Structurizr Lite at http://localhost:8090
```

No tests today. There is no `src/test/`; the module is exercised via the `process-classes` exec binding.

**Dependency-scope rule.** Every discoverable sibling module appears in `pom.xml` with **default (compile)
scope** — `test` scope hides the bytecode from `structurizr-component` and the scan silently yields 0
components. **When a new sibling adapter/module is added to the trade-loan build, add it to this `pom.xml`
in compile scope** or its components are invisible. (The driven `fcb-messaging-*`, `mcp`, and
`reconciliation` modules were missing here historically — that is why the ExternalClients / MCP /
Reconciliation views were empty.)

## Generation Pipeline (read this before changing anything)

```
ArchitectureGenerator.main(outputDir = documents/c4)        # arg = ${project.basedir}/../
  reactorRoot = outputDir.parent.parent  (= nova/)
  │
  ├─ DefaultConfig.load() ─────────────────► ArchitectureConfig
  │     ├─ workspace name + basePackage ("ir.dotin.loan.trade")
  │     ├─ persons  (Operator / Admin)                     ← 2-actor model
  │     ├─ external systems (ESB, TPS_SSO, FCB, OTEL_COLLECTOR, Consul); ESB/uses → app
  │     ├─ containers (App, Postgres, Kafka, Redis, ActiveMQ Artemis)
  │     └─ styles (per-tag colors/shapes)
  │
  ├─ ModelBuilder.buildStaticModel()        # persons/systems/containers + container "uses"
  │
  ├─ ComponentDiscovery.discover(mainContainer, reactorRoot)        ← THE CORE
  │     • findClassDirectories(): walk reactorRoot for every <module>/target/classes
  │       that contains ir/dotin/loan/trade
  │     • ComponentFinderBuilder().forContainer(app).fromClasses(dir...).withStrategy(...)*
  │       one strategy per concern, each stamping a distinct technology:
  │         NameSuffixTypeMatcher "Controller" / "CommandHandler" / "QueryHandler"
  │                               / "RepositoryAdapter" / "QueryAdapter" / "OutboxHandler"
  │                               / "Consumer" / "McpTools" / "KafkaAdapter" / "Client"
  │         RegexTypeMatcher  ".*\.adapters\.driven\.reconciliation\..*"   (recon engine)
  │         RegexTypeMatcher  aggregate-name allowlist (anchored with $)
  │         RegexTypeMatcher  ".*\.core\.domain\..*Service$"               (domain services)
  │     • finder.run() — also infers component→component relationships automatically
  │     • tagComponents(): derive C4 tags from each component's stamped technology
  │
  ├─ ModelBuilder.wireExternalSystemUsage()       # ESB → Trade Loan Application
  ├─ ModelBuilder.wireComponentInfrastructure()   # tag-based: repo→PG, outbox/consumer→Kafka,
  │                                                # recon→PG/FCB, client→FCB+Artemis/Kafka
  ├─ ViewBuilder.buildAllViews()    # SystemContext, Containers, Components(_2.._4), Controllers,
  │                                 # Handlers, DomainModel, Repositories, ExternalClients, Messaging,
  │                                 # Mcp, Reconciliation, CommandFlow, QueryFlow, WorkflowFlow
  ├─ StyleBuilder.applyAllStyles()
  └─ AdrImporter.importAdrs(workspace, adrPath)   # walks up to 5 parents for documents/adr/

WorkspaceExporter.exportJson  →  documents/c4/workspace.json
                  exportDsl   →  documents/c4/generated-workspace.dsl
                  exportMermaid → documents/c4/mermaid/*.mmd   (clears stale *.mmd first)
                  exportPlantUML → documents/c4/plantuml/*.puml (clears stale *.puml first)
                  exportDot    →  documents/c4/dot/*.dot        (clears stale *.dot first)
```

## Discovery Strategies — What You Need to Know to Extend

Discovery is convention-based via `structurizr-component` matchers. A type becomes a C4 Component only if a
strategy matches it; tags are then derived from the technology that strategy stamped (see
`ComponentDiscovery.tagComponents`). Two rules avoid footguns:

- **Matchers must be mutually exclusive.** `structurizr-component` aborts the whole `finder.run()` on the
  first duplicate (a type matched by two strategies). E.g. the reconciliation matcher is scoped to
  `adapters.driven.reconciliation` so it does NOT also grab the *driving* `…rest.ops.reconciliation`
  controller (already a Controller). Aggregate/domain regexes are anchored with `$` so
  `…RepositoryAdapter` cannot also match an aggregate name.
- **No package widening.** `basePackage = "ir.dotin.loan.trade"`. Shared kernel (`ir.dotin.loan.baseloan.*`)
  and platform libs are intentionally invisible — they are external systems / shared kernel, not components
  inside the trade-loan container.

When extending:

1. **New component type** → add a strategy in `ComponentDiscovery.discover(...)` with a matcher +
   distinct technology, a tag rule in `tagComponents(...)`, a `Tags.*` constant, optionally a style in
   `DefaultConfig.styles()`, and a view in `ViewBuilder`.
2. **New aggregate** → add the simple class name to the anchored allowlist regex in `ComponentDiscovery`.
3. **New container / external system / person** → edit `DefaultConfig.containers()` / `externalSystems()`
   / `persons()`, referencing `ArchitectureConstants` (`Systems.*`, `Containers.*`, `Technologies.*`,
   `Tags.*`) — do not hard-code strings.
4. **New sibling module** → add it to this module's `pom.xml` (compile scope) so its bytecode is scanned.
5. **Verify after change**: `make c4`, then read the stdout (`📦 Class directories scanned: N`,
   `🔍 Total discovered: N`). A category dropping to 0 means a naming convention broke, a module dep was
   missed/test-scoped, or two matchers collided (look for `⚠️ Component discovery error: … already exists`).

## ADR Import

`AdrImporter` walks up to 5 parent directories for `documents/adr/` (then `adr/`). Each Markdown file
becomes a Structurizr `Decision` wrapped in an RTL `<div>` and lands in `workspace.json`. Honor the Persian
MADR template from the parent `documents/CLAUDE.md`; the importer treats files as opaque markdown.

## Conventions That Will Bite You

- **The module is profile-only.** It is in the `c4-docs` profile's `<modules>` in nova root `pom.xml`, NOT
  the default reactor. `mvn -pl documents/c4/java …` WITHOUT `-Pc4-docs` fails with "Could not find the
  selected project in the reactor". Always use `make c4` / `-Pc4-docs`.
- **`structurizr.version = 5.0.3`** (property in `pom.xml`). Upgrade `structurizr-core`, `-dsl`, `-export`,
  `-component`, `-annotation` together. `structurizr-export` is the source of the DOT/Mermaid/PlantUML
  writers.
- **Generated files are version-controlled.** Commit regenerated `workspace.json` + diagrams in the same
  change as a scanner/config edit. The exporters clear stale `*.mmd`/`*.puml`/`*.dot` first, so removed or
  renamed views leave no straggler files.
- **Console output uses emoji.** The `📦 / 🔍 / ✅` stdout lines are the only feedback channel; preserve them.
- **No Spring runtime here.** Despite reading Spring annotations off bytecode, this runs as a plain `java`
  main — do not pull in `spring-boot-starter-*`.

## Source Layout

```
documents/c4/java/
├── pom.xml                                # artifactId architecture-docs; exec:java in process-classes
└── src/main/java/ir/dotin/loan/trade/architecture/
    ├── ArchitectureGenerator.java         # main(); orchestrates generate(reactorRoot) + export()
    ├── ComponentDiscovery.java            # structurizr-component finder + tag derivation (see above)
    ├── builder/
    │   ├── ModelBuilder.java              # static model + ESB usage + tag-based infra/external wiring
    │   ├── ViewBuilder.java               # all views (SystemContext … Mcp, Reconciliation, WorkflowFlow)
    │   ├── StyleBuilder.java              # applies StyleConfig from DefaultConfig
    │   └── AdrImporter.java               # parents-walk for documents/adr or ./adr
    ├── config/
    │   ├── ArchitectureConfig.java        # nested records (WorkspaceConfig … UsesConfig, StyleConfig)
    │   ├── ArchitectureConstants.java     # Systems, Containers, Persons, Technologies, Tags, Views
    │   └── DefaultConfig.java             # the canonical wiring — edit here to change the static model
    └── export/
        └── WorkspaceExporter.java         # JSON, DSL, Mermaid, PlantUML, DOT (+ stale-file cleanup)
```
