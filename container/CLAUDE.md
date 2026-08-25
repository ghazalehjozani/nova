# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Module: trade-loan-container

Spring Boot **application entry point** for the trade-loan service. Assembles every hexagonal layer (domain, application services, driving + driven adapters) into one runnable microservice and ships the only configuration the JVM reads at startup. Also hosts the **E2E test suite** that boots the full context against real PostgreSQL, Kafka, and Redis via Testcontainers.

Main class: `ir.dotin.loan.trade.NovaApplication`. Maven artifact: `ir.dotin.loan:trade-loan-container`, packaging `jar`. Builds an executable Spring Boot jar via the `spring-boot-application` profile and a container image via `Dockerfile`.

## Build / Test

Generic commands: see root [CLAUDE.md → Build & Test](../CLAUDE.md#build--test-single-source-of-truth). Container-specific:

```bash
# build executable jar + run locally (.env must exist; see README-SETUP.md)
mvn clean package -Pk8s,spring-boot-application -pl :trade-loan-container -am

# E2E (requires Docker daemon). Resumable form for re-runs:
mvn verify -Pe2e -P!dev
mvn verify -Pe2e -P\!dev -rf :trade-loan-container

# E2E with auth token override (REST tests)
mvn verify -Pe2e -P!dev -De2e.auth.token="Bearer eyJ..."

# single E2E test
mvn -pl :trade-loan-container -Pe2e -P!dev -Dit.test=FullLoanFacilityLifecycleE2ETest verify
```

Failsafe pattern for E2E: `**/e2e/**/*E2E.java`, `**/e2e/**/*E2ETest.java`. Without the `e2e` profile they are skipped.

## Layout

```
container/
├── src/main/java/ir/dotin/loan/trade/
│   ├── NovaApplication.java                     # @SpringBootApplication entry point
│   ├── config/
│   │   ├── DomainConfig.java                    # base-loan domain ComponentScan re-export
│   │   ├── ApplicationReadyListener.java        # startup banner / readiness log
│   │   └── serialization/{JacksonConfiguration,RelationTypeKeyDeserializer}.java
│   └── error/                                   # ErrorCodeContributor SPIs (BaseLoan, Saga, RestAdapter, TradeLoan)
├── src/main/resources/
│   ├── bootstrap.yml                            # the ONLY local config file
│   ├── logback-spring.xml, banner.txt
│   ├── i18n/messages_{en,fa}.properties
│   ├── swagger/*.json                           # static OpenAPI exports
│   └── db/changelog/                            # Liquibase master + per-release changelogs
├── src/test/java/ir/dotin/loan/trade/e2e/       # E2E suite (see below)
├── src/test/resources/
│   ├── application-e2e.yml                      # e2e profile overrides
│   └── e2e/docker-compose-e2e.yml               # Testcontainers compose file (stays here)
├── scripts/                                     # secret/config sync + k8s helpers
├── Dockerfile, .env(.example)                   # app image + app runtime env (client side)
└── pom.xml
```

> **Local dev infra moved out.** PostgreSQL, the legacy Redis Sentinel HA stack, the broker, and the
> `redis/` configs now live in the sibling repo [`../../nova-dev-stack`](../../nova-dev-stack)
> (`docker-compose.yml` + `.env`). Nova itself is Cluster-only; that preserved Sentinel stack is not a compatible
> Redis endpoint until it is migrated separately. Shared secrets (`DB_*`, `REDIS_PASSWORD`, `ARTEMIS_*`) remain
> duplicated between `.env` here and there.
> Only the E2E Testcontainers compose stays in this module.

`NovaApplication` does a **filtered ComponentScan** over `ir.dotin.loan.baseloan.core.domain` that picks up only `@DomainComponent`, `@DomainService`, `@DomainFactory`. Do not broaden the scan — base-loan domain types must remain pure POJOs to the application context.

## Configuration Model (critical)

- `bootstrap.yml` is the **only** local configuration file. It contains Consul connection details and the service identity (`spring.application.name=nova-service`); nothing else.
- All runtime config (datasource, kafka, fcb, dispatcher, security, …) lives in **HashiCorp Consul KV**, owned by the separate `nova-config` repo and synchronised by GitLab CI (prod) or the `git2consul-sync` sidecar (dev/test).
- Secrets are **never** in Consul. They are injected as env vars (`${ENV_VAR}`) at runtime and resolved by Spring **after** the KV pull. Local-dev secrets live in `.env` (git-ignored). `.env.example` is the template.
- Active profile is selected with `SPRING_PROFILES_ACTIVE` (defaults to `dev`). The `e2e` profile is reserved for the Testcontainers suite — do not enable it in real deployments.
- **`@ConfigurationProperties` classes MUST be regular classes, not Java `record`s.** `@RefreshScope` proxies via CGLIB and cannot subclass a `final` record → hot reload silently breaks and a stale snapshot is served for the JVM's lifetime. Use a mutable POJO (or `@ConstructorBinding` class) with setters.
- **Any add/rename/remove on a `@ConfigurationProperties` field requires a matching edit in [`nova-config`](../../nova-config/CLAUDE.md)** under the service's KV context. The service has no local `application.yml`, so an out-of-sync key binds to its default (or fails `spring.config.import` if `fail-fast` is on). Ship both PRs together.

- **Local clock offset — `application-dev.yml` only, never `bootstrap.yml`.** Every server this service talks to (the Nova hosts, Artemis, FCB, and the OTel/ELK backends) reports timezone UTC while running exactly **+3:30 ahead of true UTC**. The skew is uniform, so deployed rings agree with each other; a developer workstation with a correct clock is the odd one out, and its envelopes are rejected by FCB as 3.5 hours stale. To match the fleet locally, put

  ```yaml
  platform:
    clock:
      offset: PT3H30M
  ```

  in `container/src/main/resources/application-dev.yml`, which `.gitignore:188` excludes and which loads automatically because `SPRING_PROFILES_ACTIVE` defaults to `dev`. It must **not** go in `bootstrap.yml`: that file is tracked and ships inside the jar to every ring, where a non-zero offset would shift persisted timestamps, lock expiries, retry deadlines and cache TTLs. Consul carries an explicit `PT0S` for every ring. A non-zero offset also logs a WARN at startup and is published as the `platform.clock.offset.seconds` gauge.

When something doesn't take effect, check Consul KV first; only fall back to looking inside this module if the key is genuinely absent from `nova-config`.

## i18n message bundles (`src/main/resources/i18n/`)

- `messages.properties` (base/`en`) + `messages_fa.properties` + `i18n-catalog.json` are **generated** by the
  [`i18n-extractor-maven-plugin`](../../pangaea/plugins/i18n-extractor-maven-plugin/CLAUDE.md), bound to
  `generate-resources` in `container/pom.xml` (`extract-i18n` execution). Raw UTF-8 (no `\uXXXX`). Two key families:
  `error.{issuer-lc}.{code}` (issuer `LOAN`) and `enum.{Simple}.{CONST}` display-enum labels (base-loan + accounting
  `LocalizedEnum` enums, scanned from dependency JARs). Error/message enums never produce `enum.*` keys.
- **Regenerate** after adding an enum constant / error code: `mvn -pl :trade-loan-container -am generate-resources`
  (add `-Denforcer.skip=true` only if a pre-existing dependency-convergence enforcer error blocks it). New base keys get
  a humanized English value; new `fa` keys get a BLANK skeleton; existing `fa` translations are preserved; stale keys are
  pruned. Then translate the new blank `fa` values.
- **Boot fails on an untranslated or parameter-drifted key** — pangaea's `I18nValidationAutoConfiguration`
  (`platform.i18n.fail-fast`, default ON) validates every `fa` key against `i18n-catalog.json` at startup. Set
  `platform.i18n.fail-fast=false` in a dev `nova-config` context to boot with a half-translated bundle locally.
- **Add a language**: add a `<locale>xx</locale>` to the plugin's `<locales>` in `container/pom.xml`, regenerate,
  translate the new `messages_xx.properties` blanks; the catalog + runtime validator pick it up automatically.

## E2E Test Infrastructure

`E2ETestConfiguration` starts a `ComposeContainer` (Testcontainers, Docker Compose v2 — **not** the deprecated `DockerComposeContainer`) from `src/test/resources/e2e/docker-compose-e2e.yml`, exposing:

- **PostgreSQL 18.0** (alpine) — dynamic port, wired in via `@DynamicPropertySource`
- **Kafka** (cp-kafka 7.9.4, KRaft) — fixed host port `9094:9094`, **SASL_PLAINTEXT / SCRAM-SHA-256** to match prod
- **Redis 8.2.2** (alpine) — password-protected

Test class hierarchy:

```
AbstractE2E              # @SpringBootTest(classes=NovaApplication), @ActiveProfiles("e2e")
├── AbstractMessagingE2E # KafkaTemplate, send/await helpers, @MockitoBean outbound ports
└── AbstractRestE2E      # REST client + auth-token plumbing
```

Fixtures (`e2e/fixture/`) seed the DB with loan types, arrangements, and disbursed facilities; helpers (`KafkaTestHelper`, `BaseResponseAssertions`, `PrerequisiteOrchestrator`, `MockPortConfigurator`) are the surface the test bodies should use.

### Adding an E2E test

1. Extend `AbstractMessagingE2E` (Kafka flows) or `AbstractRestE2E` (HTTP flows).
2. `@Import` only the fixtures you actually need.
3. Use `@TestInstance(PER_CLASS)` + `@BeforeAll` for fixture setup.
4. Outbound-port mocks are **declared on `AbstractMessagingE2E`** (not on a `@TestConfiguration`). Override per-test with `when(...).thenReturn(...)`.
5. Assert async outcomes with Awaitility.

### Kafka SASL gotchas (production-parity)

- Host port mapping **must** be fixed `9094:9094`; `KAFKA_ADVERTISED_LISTENERS` for the SASL listener **must** use `localhost`, otherwise host-side clients receive unreachable broker metadata.
- SCRAM credentials are bootstrapped via `kafka-storage format --add-scram` in the compose `command:` block (user `admin` / pass `abcd1234`).
- Inter-broker listener: PLAINTEXT on `9092` (used for healthcheck only). Client listener: SASL_PLAINTEXT on `9094`.
- `E2ETestConfiguration` connects to `localhost:9094` directly — **do not** read the ambassador port from `ComposeContainer` for this listener.
- `KafkaTestHelper.createResponseConsumer()` carries the SASL props for manually-built consumers.

### Mocked outbound ports (defaults return `Result.success()`)

`LoanServicePort`, `AccountServicePort`, `TransactionPostingPort`, `CollateralServicePort`, `DepositServicePort`, `CustomerServicePort`, `FindOrCreateAccountPort`, `FindAccountByIdPort`, `FetchSanctionDetailsPort`.

Use `Result.success()` (no-arg); `Result.success(null)` throws NPE.

## Liquibase

Greenfield baseline (nova ADR-0006, pangaea ADR-0030 / ADR-0028 / ADR-0029 / ADR-0031). Master: `src/main/resources/db/changelog/db.changelog-master.xml` → includes `framework/db.changelog-framework.xml` and `trade-loan/db.changelog-trade-loan.xml`. The **framework** master explicit-`<include>`s the per-module pangaea/expression-kit masters (`db/changelog/db.changelog-{persistence,inbox,audit,reconciliation,workflow,expression-kit}.xml`) that ship as classpath root resources in the `*-data-jpa`/starter dependency jars, in dependency order (`persistence` first — it carries ShedLock — then the rest). The **trade-loan** master explicit-`<include>`s the domain's own formatted-SQL leaves under `trade-loan/changes/YYYYMMDDTHHMMSS_*.sql` (one changeset/file, explicit `--rollback`, stable `logicalFilePath`, append-only timestamp ordering; PK ids are UUID v7). No `includeAll` anywhere. **Every** new SQL leaf must be `<include>`d from its module master; orphaned files compile but produce `column does not exist` failures at runtime.

### Operational tooling (`-Pliquibase-ops`)

`container/pom.xml` carries a **profile-gated** `liquibase-maven-plugin` (id `liquibase-ops`, INACTIVE in normal builds/CI) for running migrations from the command line against any environment. The plugin's own `liquibase-core` is **pinned to 5.0.3** to match the app's runtime Liquibase (drift changes checksum/parse behaviour between `mvn liquibase:*` and Spring Boot startup). `changeLogFile=db/changelog/db.changelog-master.xml` resolves from the module classpath — the framework changelogs ship in the `*-data-jpa` dependency jars, so **`make install` (reactor → `.m2`) must run first** or the plugin can't resolve those changelog jars. DB connection comes from `-Dliquibase.url=...` overriding env-var defaults (`jdbc:postgresql://${env.DB_HOST}:${env.DB_PORT}/${env.DB_NAME}`, `${env.DB_USERNAME}`/`${env.DB_PASSWORD}`); never hardcode secrets.

Operator entrypoint is the repo-root [`Makefile`](../Makefile): `make db-status | db-validate | db-update | db-sql` (dry-run `updateSQL` → `container/target/`) `| db-tag TAG=… | db-rollback TAG=… | db-rollback-count N=… | db-history | db-release-locks`. Each target sources `$(ENV_FILE)` (`container/.env`) and guards `DB_*`. Raw form: `mvn -pl container -Pliquibase-ops liquibase:<goal>`.

**stage/prod migration policy** (dedicated runner Job, NOT in-app) lives in [`documents/runbooks/RB-0004.liquibase-prod-migrations.md`](../documents/runbooks/RB-0004.liquibase-prod-migrations.md).

## Spring Boot 4.x / Test stack notes

- `@MockBean` and `@SpyBean` are **removed**. Use `@MockitoBean` / `@MockitoSpyBean` from `org.springframework.test.context.bean.override.mockito`.
- `@MockitoBean` must sit on the test class (or a superclass), **not** on a `@TestConfiguration`. We place them on `AbstractMessagingE2E`.
- Null annotations: use `org.jspecify` (`@Nullable`, `@NonNull`). Do **not** introduce `org.jetbrains:annotations`.
- Testcontainers Docker Compose v2 → `ComposeContainer`, not `DockerComposeContainer`.

## Operational scripts

- `scripts/sync-configs.sh` — push `nova-config` KV to a target Consul environment.
- `scripts/{create,update,get}-secret.sh` — manage k8s secrets that back `${ENV_VAR}` placeholders.
- `scripts/cleanup.sh` — remove stale local containers/volumes between runs.
- Local infra bring-up (postgres / redis / broker) + `render-sentinel-conf.sh` now live in
  [`../../nova-dev-stack`](../../nova-dev-stack).

## Do-not-commit

`.env`, `application-dev.yml`, `k8s/**/secret*.yml`, anything containing real credentials. The `.gitignore` covers the common cases; double-check before staging. `application-dev.yml` matters more than it looks: it is where a non-zero `platform.clock.offset` lives (see Configuration Model), and committing it would ship a 3.5-hour time shift to every ring.
