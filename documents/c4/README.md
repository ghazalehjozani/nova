# C4 Architecture Diagrams — Trade Loan Service

The C4 model for the trade-loan microservice is **generated from the compiled code**, not hand-drawn.
A small build-time module (`documents/c4/java`, artifactId `architecture-docs`) discovers components
straight from bytecode with the official [`structurizr-component`](https://github.com/structurizr/java)
finder (Structurizr 5.x), merges the ADRs under `documents/adr/`, and emits a Structurizr workspace plus
rendered diagrams in Mermaid, PlantUML and DOT.

These generated artifacts are the machine-readable C4 supplement. Where SAW_102 also calls for hand-drawn
draw.io/Confluence diagrams, link them from this file as they are produced.

## How to regenerate

The generator is intentionally **off the main Maven lifecycle** — `mvn install/verify/package` neither
compiles nor runs it (the module lives only in the `c4-docs` profile). Regenerate explicitly:

```bash
make c4          # = mvn -Pc4-docs -pl documents/c4/java -am -DskipTests process-classes
```

Commit the regenerated `workspace.json`, `generated-workspace.dsl`, `mermaid/`, `plantuml/` and `dot/`
files in the **same change** as the code that altered the model.

## How to view

```bash
make c4-view     # runs documents/c4/run-structurizr.sh → Structurizr Lite at http://localhost:8090
```

Or open any `mermaid/*.mmd` in a Mermaid viewer, or `plantuml/*.puml` in a PlantUML renderer.

## Diagram index

### System Context (Level 1)
- `mermaid/SystemContext.mmd` · `plantuml/SystemContext.puml`

Two actors drive the service — the **Operator/Admin** (OPS endpoints `/v1/ops/*` + MCP ops tools) and the
**ESB** (all business loan-lifecycle REST traffic). Downstream: **FCB Core Banking** over the corridor
(ActiveMQ Artemis primary / Kafka fallback), **TPS SSO** (OAuth2/OIDC + JWKS), the **OpenTelemetry
Collector** (OTLP http/protobuf), and **Consul** (runtime config).

### Containers (Level 2)
- `mermaid/Containers.mmd` · `plantuml/Containers.puml`

The Spring Boot application plus its data/broker containers: **PostgreSQL 18**, **Redis 8.6** (Sentinel HA),
**Kafka** (SASL/SCRAM — events + request/reply fallback), and **ActiveMQ Artemis 2.43** (primary FCB
corridor).

### Components (Level 3)
Whole-container views (paged) and per-concern slices:

| View | File | Shows |
|------|------|-------|
| All components | `Components.mmd` (+ `_2`, `_3`, `_4`) | every discovered component, paged |
| Controllers | `Controllers.mmd` | REST controllers |
| Handlers | `Handlers.mmd` | command + query handlers (compensations flagged) |
| Domain model | `DomainModel.mmd` | aggregate roots + domain services |
| Repositories | `Repositories.mmd` | JPA repository/query adapters + PostgreSQL |
| External clients | `ExternalClients.mmd` | FCB corridor clients (Artemis + Kafka) |
| Messaging | `Messaging.mmd` | Kafka consumers, outbox handlers + Kafka/Artemis |
| MCP | `Mcp.mmd` | MCP driving adapter (read + ops tools) |
| Reconciliation | `Reconciliation.mmd` | Nova ↔ FCB reconciliation engine |
| Command flow | `CommandFlow.mmd` | CQRS write path |
| Query flow | `QueryFlow.mmd` | CQRS read path |
| Workflow flow | `WorkflowFlow.mmd` | end-to-end workflow orchestration path |

### Code (Level 4)
Not generated — add draw.io diagrams on Confluence only for genuinely complex logic and link them here.

## How it works (for maintainers)

See [`java/CLAUDE.md`](java/CLAUDE.md) for the generation pipeline, the discovery strategies, and the rules
for extending it (new component type, new aggregate, new sibling module).
