# Trade Loan :: Container Module

## Overview

The container module is the Spring Boot application entry point (`ir.dotin.loan.trade.NovaApplication`). It assembles all hexagonal architecture layers (domain, application services, adapters) into a runnable microservice. It also contains **E2E integration tests** that boot the full application with real infrastructure via Testcontainers.

## Build Commands

e2e tests only run in docker and devcontainer

```bash
# Build with unit tests only
cd /workspace/trade-loan && mvn clean verify -P!dev -DskipITs=true

# Run E2E tests (requires Docker)
cd /workspace/trade-loan && mvn verify -Pe2e -P!dev
cd /workspace/trade-loan && mvn verify -Pe2e -P\!dev -rf :trade-loan-container

# Run E2E tests with auth token
cd /workspace/trade-loan && mvn verify -Pe2e -P!dev -De2e.auth.token="Bearer eyJ..."
```

## E2E Test Infrastructure

### How It Works

E2E tests use **Testcontainers with Docker Compose** to spin up real infrastructure:
- **PostgreSQL** (database)
- **Kafka** (KRaft mode, PLAINTEXT)
- **Redis** (password-protected)

Tests boot the full Spring Boot context (`@SpringBootTest`) with `@ActiveProfiles("e2e")` and send real Kafka messages, verifying outcomes in the database and on Kafka response topics.

### Key Files

| File                                                            | Purpose                                                                                          |
|-----------------------------------------------------------------|--------------------------------------------------------------------------------------------------|
| `src/test/java/.../e2e/E2ETestConfiguration.java`               | Test config: DockerCompose container, dynamic properties                                         |
| `src/test/java/.../e2e/AbstractMessagingE2E.java`               | Base class: KafkaTemplate, ObjectMapper, auth token, send helpers, **@MockitoBean declarations** |
| `src/test/java/.../e2e/fixture/KafkaTestHelper.java`            | Kafka utilities: build records with headers, create consumers                                    |
| `src/test/java/.../e2e/fixture/LoanArrangementTestFixture.java` | Creates loan arrangements in DB                                                                  |
| `src/test/java/.../e2e/fixture/LoanTypeTestFixture.java`        | Creates loan types in DB                                                                         |
| `src/test/java/.../e2e/fixture/LoanFacilityTestFixture.java`    | Creates disbursed facilities with installment schedules                                          |
| `src/test/resources/application-e2e.yml`                        | E2E Spring profile config                                                                        |
| `src/test/resources/e2e/docker-compose-e2e.yml`                 | Docker Compose for Testcontainers                                                                |

### Test Classes

- **`FullLoanFacilityLifecycleE2ETest`** - Tests full lifecycle saga (create + disburse via Kafka message)
- **`InstallmentCollectionE2ETest`** - Tests installment collection with request/reply pattern

### Adding New E2E Tests

1. Extend `AbstractMessagingE2E`
2. Use `@Import` to bring in needed fixtures
3. Use `@BeforeAll` with `@TestInstance(PER_CLASS)` for fixture setup
4. Mock ports are inherited from `AbstractMessagingE2E` (`@MockitoBean` on the base class)
5. Send messages via `sendAndWait(record)` or `buildRecord()` + `sendAndWait()`
6. Assert with Awaitility for async outcomes

### Test Profile Activation

E2E tests only run with the `e2e` Maven profile (defined in `platform-parent`):
```bash
mvn verify -Pe2e
```
Pattern: `**/e2e/**/*E2E.java` and `**/e2e/**/*E2ETest.java` (Maven Failsafe)

## Embeddable Entity Field Names (Gotchas)

When writing test fixtures that create JPA entities directly, use the correct setter names for `@Embeddable` types:

| Embeddable Class                | Field                                                                                                                                                                                                                        | Setter                                                      |
|---------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------|
| `TitleEmb`                      | `value`                                                                                                                                                                                                                      | `setValue()` (NOT `setFaTitle`/`setEnTitle`)                |
| `CurrencyTypeEmb`               | `value`                                                                                                                                                                                                                      | `setValue()` (NOT `setCurrencyType`)                        |
| `InstallmentCountEmb`           | `value`                                                                                                                                                                                                                      | `setValue()` (NOT `setCount`)                               |
| `LoanTypeCodeEmb`               | `value`                                                                                                                                                                                                                      | `setValue()`                                                |
| `InterestPolicyEmb`             | `baseInterestRate`, `preferentialMinRate`, `preferentialMaxRate`, `dailyInterest`                                                                                                                                            | Direct setters                                              |
| `PenaltyPolicyEmb`              | `penaltyRate`, `deferralInterestRate`                                                                                                                                                                                        | Direct setters                                              |
| `InstallmentPolicyEmb`          | `installmentPeriodDays`                                                                                                                                                                                                      | `setInstallmentPeriodDays()`                                |
| `GracePeriodPolicyEmb`          | `minGracePeriodDays`, `maxGracePeriodDays`                                                                                                                                                                                   | Direct setters                                              |
| `PeriodRangeEmb`                | `minPeriod`, `maxPeriod`                                                                                                                                                                                                     | `setMinPeriod(PeriodEmb)`, `setMaxPeriod(PeriodEmb)`        |
| `AmountRangeEmb`                | `minAmount`, `maxAmount`, `currency`                                                                                                                                                                                         | Direct setters                                              |
| `GracePeriodEmb`                | `days`, `months`, `years`                                                                                                                                                                                                    | Direct setters                                              |
| `MoneyEmb`                      | `amount`, `currency`                                                                                                                                                                                                         | `setAmount(BigDecimal)`, `setCurrency(String)`              |
| `PeriodEmb`                     | `years`, `months`, `days`                                                                                                                                                                                                    | Direct setters                                              |
| `RepaymentPriorityPolicyEmb`    | `installmentMainAmountPriority`, `installmentInterestAmountPriority`, `installmentPenaltyAmountPriority`, `installmentIncomeAmountPriority`, `insuranceAmountPriority`, `insurancePenaltyAmountPriority`, `hasEqualPriority` | Direct setters (NOT `principalPriority`/`interestPriority`) |
| `RegulatoryCompliancePolicyEmb` | `overDuePeriod`, `deferralPeriod`, `suspiciousPeriod`                                                                                                                                                                        | Direct setters (NOT `overDuePeriodMonths`)                  |
| `CollateralPolicyEmb`           | `totalPercent`, `collateralTypes`, `collateralCalculationType`                                                                                                                                                               | Direct setters. **`collateralCalculationType` is NOT NULL** |
| `ConfirmTypeEmb`                | `personCode`                                                                                                                                                                                                                 | `setPersonCode(String)` (NOT a raw String list)             |

## Important Enum Values (Gotchas)

Common enum values that differ from what you might expect:

| Enum                        | Correct Values                                                                                   | NOT This                                                   |
|-----------------------------|--------------------------------------------------------------------------------------------------|------------------------------------------------------------|
| `ApplicantChannel`          | `INTERNET_BANK`, `DIGITAL_BANK`                                                                  | ~~BRANCH~~                                                 |
| `GatewayType`               | `CARD`, `DIGITAL_BANK`, `LOAN`, `GUARANTEE`, `CHEQUE`, `LETTER_OF_CREDIT`, `COLLATERAL`, `STAFF` | ~~API_GATEWAY~~                                            |
| `FacilityStatus`            | `FULLY_DISBURSED`, `PARTIALLY_DISBURSED`                                                         | ~~DISBURSED~~                                              |
| `InstallmentScheduleType`   | `EQUAL_INSTALLMENTS`, `GRADUAL_INSTALLMENTS`                                                     | ~~EQUAL~~                                                  |
| `InstallmentStatus`         | `SCHEDULED`, `PAID`, `PARTIALLY_PAID`, `OVERDUE`, `CANCELLED`                                    | ~~PENDING~~                                                |
| `CollateralCalculationType` | `BASED_ON_PRINCIPAL`, `BASED_ON_PRINCIPAL_AND_TOTAL_INTEREST`                                    | (no NONE value)                                            |
| `DisbursementType`          | `LUMP_SUM`, `PROGRESSIVE`                                                                        | (arrangement-level, controls allowed `DisbursementMethod`) |
| `DisbursementMethod`        | `LUMP_SUM`, `REGULAR_PROGRESSIVE`, `IRREGULAR_PROGRESSIVE`                                       | (facility-level)                                           |
| `LoanSecondaryType`         | `GENERAL`, `SPECIFIC`, `GENERAL_AND_SPECIFIC`, `NONE`                                            |                                                            |
| `SectionType`               | `FIXED`, `CURRENT`, `FIXED_AND_CURRENT`, `NONE`                                                  |                                                            |

## Important Enum Locations

| Enum                                             | Package                                                               |
|--------------------------------------------------|-----------------------------------------------------------------------|
| `GatewayType`                                    | `ir.dotin.loan.baseloan.core.domain.shared.enums` (NOT in trade-loan) |
| `FacilityStatus`                                 | `ir.dotin.loan.baseloan.core.domain.loanfacility.enums`               |
| `PartyRole`, `PartyType`                         | `ir.dotin.loan.baseloan.core.domain.shared.enums`                     |
| `ApplicantChannel`, `DisbursementMethod`         | `ir.dotin.loan.baseloan.core.domain.loanfacility.enums`               |
| `InstallmentStatus`, `InstallmentScheduleStatus` | `ir.dotin.loan.baseloan.core.domain.installmentschedule.enums`        |
| `CollateralCalculationType`                      | `ir.dotin.loan.baseloan.core.domain.shared.enums`                     |
| `DisbursementType`                               | `ir.dotin.loan.baseloan.core.domain.loanarrangement.enums`            |

## External Service Mocking

All outbound ports to external services are mocked via `@MockitoBean` in `AbstractMessagingE2E` (the base test class):

| Port                       | Purpose                                                 |
|----------------------------|---------------------------------------------------------|
| `LoanServicePort`          | Economic sectors, topics, application numbers, branches |
| `AccountServicePort`       | Account validation, account opening                     |
| `TransactionPostingPort`   | Transaction posting                                     |
| `CollateralServicePort`    | Collateral/assurance validation                         |
| `DepositServicePort`       | Deposit info, debtor deposit validation                 |
| `CustomerServicePort`      | Customer info, related customers                        |
| `FindOrCreateAccountPort`  | Account resolution                                      |
| `FindAccountByIdPort`      | Account lookup                                          |
| `FetchSanctionDetailsPort` | Sanction checks                                         |

Default stubs return `Result.success()`. Override in individual tests with `when(...).thenReturn(...)`.

**IMPORTANT**: Use `Result.success()` (no-arg), NOT `Result.success(null)`. The `Result.success(T)` method throws NPE for null values.

## Docker Requirements

E2E tests require Docker to be running. The Docker Compose file starts:
- **PostgreSQL 18.0** (alpine) on dynamic port
- **Kafka** (cp-kafka:7.9.4, KRaft mode, **SASL_PLAINTEXT** on fixed port 9094, SCRAM-SHA-256)
- **Redis 8.2.2** (alpine) with password protection

### Kafka SASL Configuration (Production-Like)

Kafka uses SASL_PLAINTEXT with SCRAM-SHA-256 to match production. Key points:
- **Fixed port mapping** `9094:9094` (NOT dynamic) so `KAFKA_ADVERTISED_LISTENERS` uses `localhost:9094`
- **SCRAM credentials** bootstrapped via `kafka-storage format --add-scram` in custom command
- **Credentials**: username=`admin`, password=`abcd1234`
- **Inter-broker listener**: PLAINTEXT on port 9092 (internal only, also used for healthcheck)
- **Client listener**: SASL_PLAINTEXT on port 9094 (external, used by tests and Spring Boot app)
- **E2ETestConfiguration** uses `localhost:9094` directly (NOT ComposeContainer ambassador port)
- **`KAFKA_ADVERTISED_LISTENERS`** MUST use `localhost` (not container hostname) for the SASL_PLAINTEXT listener, otherwise Kafka metadata returns unreachable addresses to host-side clients
- **KafkaTestHelper.createResponseConsumer()** includes SASL properties for the manual consumer

## Spring Boot 4.x Notes

- **`@MockBean` is removed.** Use `@MockitoBean` from `org.springframework.test.context.bean.override.mockito` instead.
- **`@MockitoBean` must be on test class or its superclass**, NOT on `@TestConfiguration` classes. Place them in `AbstractMessagingE2E`.
- **`@SpyBean` is removed.** Use `@MockitoSpyBean` from the same package.
- **Null annotations:** This project uses `org.jspecify:jspecify` (`@Nullable`, `@NonNull`), NOT `org.jetbrains:annotations`.
- **Testcontainers**: Use `ComposeContainer` (NOT deprecated `DockerComposeContainer`) for Docker Compose v2 support.

## Liquibase Changelog Notes

When adding new JPA entity fields that map to DB columns, **always add the corresponding Liquibase changelog AND include it in `db.changelog-master.xml`**. Missing includes cause "column does not exist" errors at runtime but compile fine.

Master changelog: `src/main/resources/db/changelog/db.changelog-master.xml`

## Dependency Notes

Test dependencies for E2E are managed in this module's POM. If adding new testcontainers modules, verify they are available in the corporate Nexus mirror (`mirrorOf=*` in Maven settings). Older artifact naming conventions may be required (e.g., `testcontainers-kafka` vs `kafka`).

## E2E Test Data (Reference)

Realistic test data values (from Postman collection):
- Economic sector code: `"2-1"`
- Party type: `REAL`
- Gateway type: `DIGITAL_BANK`
- Applicant channel: `DIGITAL_BANK`
- Disbursement method: `IRREGULAR_PROGRESSIVE`
- Branch code: `"1"`
- Currency: `IRR`
- Confirm types: `["1", "2", "3"]`
