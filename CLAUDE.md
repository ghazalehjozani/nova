# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## ⚠️ MANDATORY skills (invoke before any other action)

- Skill `superpowers:using-superpowers` — at conversation start.
- Skill `claude-md-management:revise-claude-md` — whenever editing or auditing any `CLAUDE.md` in this repo.

If you skip these, stop and restart the turn with them.

## Harness Engineering (how to operate here)

`Agent = Model + Harness`. This file is the agent's entrypoint into the repo — it states where things are, the team
conventions, and the *current* architecture. A stale harness yields garbage even from a strong model, so keeping this
accurate (real module paths, current standards) is load-bearing.

- **Plan ≠ execute.** Plan/critique with the superpowers skills (`brainstorming` / `writing-plans` → `requesting-code-review`)
  before `executing-plans`. *Context beats instructions* — show the roadmap + current file state, not long abstract prompts.
- **State in JSON, narrative in Markdown.** For multi-session progress prefer a durable JSON state file (agents corrupt
  structured JSON far less than free-form prose over long runs).
- **Build to delete (harness decay).** Every guardrail here is a hypothesis about a model weakness; prune the ones that
  become overhead as the model gets stronger — don't let token cost accrue.
- **Harnessability over prompt polish.** Agent-readable code + accurate docs + tight feedback loops (compile, ArchUnit,
  tests) beat clever prompting.

## Repo at a glance

- `ir.dotin.loan:trade-loan` — Trade loan microservice.
- Parent: `ir.dotin.platform:platform-parent` (Java + Spring Boot, `${revision}` versioning).
- Depends on `ir.dotin.loan:base-loan-*` (separate repo, consumed as Maven artifacts — NOT in this tree).
- Hexagonal + DDD + CQRS. Strict driving/driven port split.

## Module map (each has its own CLAUDE.md — read it before working there)

Linked = has its own `CLAUDE.md`. Plain = exists but undocumented yet.

| Path                                    | Role                                                       | Start-here doc                                        |
|-----------------------------------------|------------------------------------------------------------|-------------------------------------------------------|
| `core/domain`                           | Trade-specific aggregates, VOs, strategies                 | [link](core/domain/CLAUDE.md)                         |
| `core/application/ports/inbound`        | Command + query contracts                                  | [link](core/application/ports/inbound/CLAUDE.md)      |
| `core/application/ports/outbound`       | Repo/client SPIs (`Result<T>` pattern)                     | [link](core/application/ports/outbound/CLAUDE.md)     |
| `core/application/query`                | Read-side handlers (REST + service consumers)              | [link](core/application/query/CLAUDE.md)              |
| `core/application/service`              | Command handlers, workflow orchestration, use-case logic   | [link](core/application/service/CLAUDE.md)            |
| `adapters/driving/contract`             | DTOs + mappers (anti-corruption layer)                     | [link](adapters/driving/contract/CLAUDE.md)           |
| `adapters/driving/rest`                 | REST controllers, OpenAPI (SWA-101 wire contract)          | [link](adapters/driving/rest/CLAUDE.md)               |
| `adapters/driving/messaging-kafka`      | Kafka consumers, idempotent inbox                          | [link](adapters/driving/messaging-kafka/CLAUDE.md)    |
| `adapters/driving/mcp`                  | MCP driving adapter (read + ops tools)                     | *(no CLAUDE.md yet)*                                  |
| `adapters/driven/persistence`           | JPA + outbox + Redis                                        | [link](adapters/driven/persistence/CLAUDE.md)         |
| `adapters/driven/fcb-messaging-contract`| Shared FCB request/reply + event contract (de-Kafka'd)     | *(no CLAUDE.md yet)*                                  |
| `adapters/driven/fcb-messaging-kafka`   | FCB integration over Kafka                                 | [link](adapters/driven/fcb-messaging-kafka/CLAUDE.md) |
| `adapters/driven/fcb-messaging-artemis` | FCB integration over Artemis (primary transport)           | *(no CLAUDE.md yet)*                                  |
| `adapters/driven/reconciliation`        | Nova↔FCB reconciliation state + driver (LN-59442)          | *(no CLAUDE.md yet)*                                  |
| `container`                             | Spring Boot main, wiring, Liquibase, profiles              | [link](container/CLAUDE.md)                           |
| `architecture-tests`                    | ArchUnit enforcement                                       | [link](architecture-tests/CLAUDE.md)                  |
| `documents/c4/java`                     | C4 model scanner                                           | [link](documents/c4/java/CLAUDE.md)                   |
| `documents/`                            | Architecture standards (SAW.102), ADRs, OpenAPI/AsyncAPI   | [link](documents/CLAUDE.md)                           |

## Build & Test (single source of truth)

```bash
mvn clean verify -P!dev -DskipITs=true        # full build, unit only (CI default)
mvn clean verify                              # full build incl. integration tests
mvn clean install                             # install to local repo
mvn -pl <module-path> -am test                # single module + deps
mvn -pl <module-path> -am verify -DskipITs=true
mvn test -Dtest=**/*ArchitectureTest*         # ArchUnit only
mvn clean verify -Pspotbugs                   # optional: SpotBugs
mvn clean verify -Psecurity                   # optional: OWASP dep check
./configure-project-hooks.sh                  # one-time hook setup
```

Module files no longer repeat these — they link back here.

## Architecture rules (authoritative, enforced by ArchUnit)

1. **Driving adapters MUST NOT depend on outbound ports.** Use inbound ports (commands) or `core/application/query` for read access.
2. **Adapters are thin.** ID resolution, validation against domain state, and orchestration belong in the application service. Adapters deserialize, map, and delegate — nothing else.
3. **Anti-corruption layer.** Legacy terms (e.g. `fileNumber`) translate to domain terms (e.g. `applicationNumber`) at the adapter boundary — see [`adapters/driving/contract`](adapters/driving/contract/CLAUDE.md). Commands, handlers, outbound ports, and domain code use domain terms only.
4. **Domain layer has zero framework/adapter deps.** Pure Java + JSR-310 + base-loan shared kernel.
5. **Immutable VOs, aggregate-root invariants, factories for complex construction** (`DocumentFactory`, `ArticleSpecFactory`).

### Module dependency cheat sheet

```
driving adapters → inbound ports (commands) | application/query (reads)
driving adapters ✗ outbound ports                               (FORBIDDEN)
application/service → outbound ports → driven adapters (impl)
driven adapters → outbound ports (implements)
contract (DTOs) ↔ rest / messaging-* (consumed) ↔ service (commands target)
```

## When editing CLAUDE.md files

- Always invoke `claude-md-management:revise-claude-md` first.
- Root owns: build commands, architecture rules, module index, mandatory skills, cross-cutting links.
- Module files own: their own purpose, package roots, key types, in-repo deps, sibling links. They MUST NOT duplicate the build stanza — link back here.

## CI / Tooling pointers

- `.gitlab-ci.yml` — GitLab CI pipeline.
- `checkstyle.xml` + `checkstyle-suppressions.xml` — style rules.
- `documents/adr/` — Architecture Decision Records.

## External dependencies (high-signal)

- `ir.dotin.platform:*` — platform commons + dispatcher.
- `ir.dotin.loan:base-loan-*` — shared loan kernel (separate repo).
- Spring Boot (Web, Data JPA, Kafka, Security, Validation), MapStruct, SpringDoc, ArchUnit, JUnit 5 + AssertJ + Mockito.
