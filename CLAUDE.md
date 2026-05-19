# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## ⚠️ MANDATORY skills (invoke before any other action)

- Skill `superpowers:using-superpowers` — at conversation start.
- Skill `claude-md-management:revise-claude-md` — whenever editing or auditing any `CLAUDE.md` in this repo.

If you skip these, stop and restart the turn with them.

## Repo at a glance

- `ir.dotin.loan:trade-loan` — Morabehe (Trade) loan microservice.
- Parent: `ir.dotin.platform:platform-parent` (Java + Spring Boot, `${revision}` versioning).
- Depends on `ir.dotin.loan:base-loan-*` (separate repo, consumed as Maven artifacts — NOT in this tree).
- Hexagonal + DDD + CQRS. Strict driving/driven port split.

## Module map (each has its own CLAUDE.md — read it before working there)

| Path                                  | Role                                                     | Start-here doc                                        |
|---------------------------------------|----------------------------------------------------------|-------------------------------------------------------|
| `core/domain`                         | Trade-specific aggregates, VOs, strategies               | [link](core/domain/CLAUDE.md)                         |
| `core/application/ports/inbound`      | Command + query contracts                                | [link](core/application/ports/inbound/CLAUDE.md)      |
| `core/application/ports/outbound`     | Repo/client SPIs (`Result<T>` pattern)                   | [link](core/application/ports/outbound/CLAUDE.md)     |
| `core/application/query`              | Read-side handlers (REST + service consumers)            | [link](core/application/query/CLAUDE.md)              |
| `core/application/service`            | Command handlers, sagas, use-case orchestration          | [link](core/application/service/CLAUDE.md)            |
| `adapters/driving/contract`           | DTOs + mappers (anti-corruption layer)                   | [link](adapters/driving/contract/CLAUDE.md)           |
| `adapters/driving/rest`               | REST controllers, OpenAPI                                | [link](adapters/driving/rest/CLAUDE.md)               |
| `adapters/driving/messaging-kafka`    | Kafka consumers, idempotent inbox                        | [link](adapters/driving/messaging-kafka/CLAUDE.md)    |
| `adapters/driving/messaging-activemq` | ActiveMQ request/reply                                   | [link](adapters/driving/messaging-activemq/CLAUDE.md) |
| `adapters/driven/persistence`         | JPA + outbox + Redis                                     | [link](adapters/driven/persistence/CLAUDE.md)         |
| `adapters/driven/fcb-messaging`       | FCB outbound integration                                 | [link](adapters/driven/fcb-messaging/CLAUDE.md)       |
| `container`                           | Spring Boot main, wiring, Liquibase, profiles            | [link](container/CLAUDE.md)                           |
| `architecture-tests`                  | ArchUnit enforcement                                     | [link](architecture-tests/CLAUDE.md)                  |
| `documents/c4/java`                   | C4 model scanner                                         | [link](documents/c4/java/CLAUDE.md)                   |
| `documents/`                          | Architecture standards (SAW_102), ADRs, OpenAPI/AsyncAPI | [link](documents/CLAUDE.md)                           |

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
