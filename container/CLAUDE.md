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
│   └── e2e/docker-compose-e2e.yml               # Testcontainers compose file
├── redis/                                       # sentinel templates + render script
├── scripts/                                     # secret/config sync + k8s helpers
├── Dockerfile, docker-compose.yml, .env(.example)
└── pom.xml
```

`NovaApplication` does a **filtered ComponentScan** over `ir.dotin.loan.baseloan.core.domain` that picks up only `@DomainComponent`, `@DomainService`, `@DomainFactory`. Do not broaden the scan — base-loan domain types must remain pure POJOs to the application context.

## Configuration Model (critical)

- `bootstrap.yml` is the **only** local configuration file. It contains Consul connection details and the service identity (`spring.application.name=nova-service`); nothing else.
- All runtime config (datasource, kafka, fcb, dispatcher, security, …) lives in **HashiCorp Consul KV**, owned by the separate `nova-config` repo and synchronised by GitLab CI (prod) or the `git2consul-sync` sidecar (dev/test).
- Secrets are **never** in Consul. They are injected as env vars (`${ENV_VAR}`) at runtime and resolved by Spring **after** the KV pull. Local-dev secrets live in `.env` (git-ignored). `.env.example` is the template.
- Active profile is selected with `SPRING_PROFILES_ACTIVE` (defaults to `dev`). The `e2e` profile is reserved for the Testcontainers suite — do not enable it in real deployments.

When something doesn't take effect, check Consul KV first; only fall back to looking inside this module if the key is genuinely absent from `nova-config`.

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

Master: `src/main/resources/db/changelog/db.changelog-master.xml` → includes `framework/db.changelog-framework.xml` and `trade-loan/db.changelog-trade-loan.xml`. **Every** new changelog file must be referenced from the appropriate `db.changelog-*.xml`; orphaned files compile but produce `column does not exist` failures at runtime. Per-release directories follow `trade-loan/vYYYY.M.N/NNN-description.xml`.

## Spring Boot 4.x / Test stack notes

- `@MockBean` and `@SpyBean` are **removed**. Use `@MockitoBean` / `@MockitoSpyBean` from `org.springframework.test.context.bean.override.mockito`.
- `@MockitoBean` must sit on the test class (or a superclass), **not** on a `@TestConfiguration`. We place them on `AbstractMessagingE2E`.
- Null annotations: use `org.jspecify` (`@Nullable`, `@NonNull`). Do **not** introduce `org.jetbrains:annotations`.
- Testcontainers Docker Compose v2 → `ComposeContainer`, not `DockerComposeContainer`.

## Operational scripts

- `scripts/sync-configs.sh` — push `nova-config` KV to a target Consul environment.
- `scripts/{create,update,get}-secret.sh` — manage k8s secrets that back `${ENV_VAR}` placeholders.
- `scripts/cleanup.sh` — remove stale local containers/volumes between runs.
- `redis/render-sentinel-conf.sh` — renders `sentinel.conf.tmpl` into env-specific Sentinel configs at container start.

## Do-not-commit

`.env`, `application-dev.yml`, `k8s/**/secret*.yml`, anything containing real credentials. The `.gitignore` covers the common cases; double-check before staging.
