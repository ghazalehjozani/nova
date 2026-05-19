# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this module is

`adapters/driven/fcb-messaging` is the **Nova side** of the Nova ↔ FCB Kafka integration.
Nova is the new Java 25 / Spring Boot 4 service rebuilding the loan domain; FCB is the
frozen legacy core (Java 8, old Hibernate, old Oracle). All Nova ↔ FCB traffic goes over
Kafka via this module — never REST.

This module is a **driven adapter** in Nova's hexagonal architecture. It implements
outbound ports defined in `core/application/ports/outbound/.../client/*`:

- `accountservice.AccountServicePort`, `accountservice.TransactionPostingPort`
- `customerservice.CustomerServicePort`
- `depositservice.DepositServicePort`
- `loanservice.LoanServicePort`, `loanservice.CollateralServicePort`
- `samat.ValidateSamatPort`
- `FetchSanctionDetailsPort`, `FindOrCreateAccountPort`, `FindAccountByIdPort`

All beans are gated on Spring profile **`kafka-fcb`** (`@Profile("kafka-fcb")` on every
config, client, and adapter). Without that profile, the adapter is dormant and no port
implementations exist.

**Scope today: request/reply outbound + active health probe only.** No event publishing,
no event consumption, no outbox publisher live in this module. A Nova outbox table is
created by `container/.../db/changelog/.../008-create-outbox-events.xml`, but no
publisher has been wired here. Do **not** add `@KafkaListener` event consumers or outbox
pollers here without first confirming whether a separate adapter module is being created
for them — the FCB-side CLAUDE.md describes outbox/event flows that on the Nova side do
**not** yet have a home.

Counterpart on the FCB side:
`/home/m.amirabdollahi/workspaces/core-release-customer-based/subsystem/loan/src/main/java/ir/dotin/loan/business/eventbus/nova`.
Read its `CLAUDE.md` before touching any wire contract.

## Counterpart paths

| Concern                                | Nova side (this module)                                                                 | FCB side                                                                              |
|----------------------------------------|-----------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------|
| Operation-name registry                | `dto/FcbKafkaBaseRequest.java` `@JsonSubTypes`                                          | `enums/NovaIntegrationOperationType`                                                  |
| Request/reply send                     | `service/FcbKafkaClient`                                                                | `consumer/integration/*` → `dispatcher/DefaultNovaOperationDispatcher` → handlers     |
| Reply consumer wiring                  | `config/FcbKafkaConfig` (`manualAssignContainer`)                                       | `publisher/NovaIntegrationReplyPublisher`                                             |
| Heartbeat send                         | `health/FcbHealthProbe`                                                                 | `consumer/healthcheck` → `publisher/NovaHealthCheckReplyPublisher`                    |
| Envelope sign + headers                | `platform-envelope-api` (`ActorEnvelopeFactory/Signer/Codec`) used in client + probe    | `security/envelope` (verifies inbound)                                                |
| Client-credentials token               | `platform-security-api` `ServiceTokenProvider`                                          | `security/api/NovaClientCredentialsTokenService`                                      |
| JWKS                                   | platform-security layer (NOT in this module)                                            | `security/jwks` (both fetch + publish)                                                |
| Topic names (string-for-string match)  | Consul keys `nova.fcb.kafka.*`                                                          | ZooKeeper `OperationCategory.NOVA_*`                                                  |
| Nova-owned event publish (outbox)      | NOT here — table only, in container Liquibase                                           | `outbox/NovaOutboxPoller`                                                             |

## Package layout

Base package `ir.dotin.loan.trade.adapters.driven.fcbmessaging`:

- `service/` — port implementations + the Kafka client. Entry points:
  `FcbKafkaClient` (request/reply), `FcbAccountKafkaAdapter`,
  `FcbTransactionKafkaAdapter`, `FcbValidationKafkaAdapter` (each `implements`
  an outbound port).
- `config/` — Spring config + `@ConfigurationProperties`: `FcbKafkaConfig`
  (templates + manual-assign reply container), `FcbKafkaProperties`,
  `FcbKafkaTopicBindings`, `FcbReplyPartitionResolver`, `FcbResilienceConfig`.
- `health/` — active probe stack: `FcbHealthProbe`, `FcbHealthGate`,
  `FcbPartitionHealthRegistry`, `FcbKafkaReadinessIndicator`,
  `FcbHealthAutoConfig`, `FcbHealthMetrics`, `FcbHealthProperties`,
  `HealthActorProperties`.
- `metrics/` — `FcbRequestReplyMetrics` (Micrometer).
- `dto/` — wire DTOs. `FcbKafkaBaseRequest` (the `@JsonSubTypes` registry) and
  `FcbKafkaBaseResponse` sit at the root; concrete payloads under
  `dto/request/` and `dto/reply/`.
- `mapper/` — translate domain ⇄ wire DTO: `KafkaTransactionMapper`,
  `KafkaAccountMapper`, `KafkaValidationMapper`, `KafkaErrorCodeMapper`,
  `ArticleMetadataMapper`, `LoanTransactionMerger`, `DocumentMerger`.
- `exception/` — `FcbKafkaException`, `FcbClientException`,
  `FcbServerException`, `FcbSerializationException`.
- `util/` — `HostResolver` (stamps `X-Host`).

## Build, test, run

- Maven multi-module. This module's POM only depends on `platform-spring-boot-starter-messaging-kafka`,
  `platform-security-api`, `platform-envelope-api`, `jackson-databind` (Jackson 3 —
  `tools.jackson.*`), Boot actuator, OTel API, Micrometer tracing. **The FCB side is
  Gradle; this side is Maven — do not run `./gradlew` here.**
- Do not run builds yourself; verify changes by reading the code.
- Single test (from repo root or the module):
  ```bash
  mvn -pl adapters/driven/fcb-messaging -am test -Dtest=FcbKafkaClientTest#shouldFoo
  ```
  Note: `src/test` is currently empty in this module — the command above is the
  form to use once tests are added; new tests belong under
  `src/test/java/ir/dotin/loan/trade/adapters/driven/fcbmessaging/...`.
- The Spring profile `kafka-fcb` must be active for any of this module's beans to
  materialise.

## Request/reply contract

Entry point: `FcbKafkaClient.sendAndReceive(FcbKafkaBaseRequest, Duration)`.

Pipeline (in order):

1. `FcbHealthGate.checkPermitted(operationName)` — fail-closed when all known partitions
   are unhealthy; fail-open while warming up.
2. `RetryTemplate` qualified `FcbResilienceConfig.FCB_KAFKA_RETRY_TEMPLATE` — **bounded**
   to 2 retries with 250ms→2s backoff. Retryable causes: `FcbServerException`,
   `KafkaException`, `TimeoutException`. Anything else falls through.
3. Fetch token via `ServiceTokenProvider.getServiceToken()` → `Authorization: Bearer …`.
4. Build `ActorEnvelope` — `envelopeFactory.fromExplicit(...)` when
   `AuthenticationContextHolder` has a user-id, else
   `fromConfigDefault(SYSTEM_RECOVERY, "system", null, RECOVERY, SYNC)`. Sign with
   `ActorEnvelopeSigner`; emit onto headers via `ActorEnvelopeCodec.write(...)`.
5. Serialize body with `tools.jackson.databind.ObjectMapper` (Jackson 3).
6. Publish through the `ReplyingKafkaTemplate` qualified
   `FcbKafkaConfig.FCB_INTEGRATION_REPLYING_TEMPLATE`
   (`sharedReplyTopic=true`, `binaryCorrelation=false`).
7. Block on `future.get(timeoutMs, MILLISECONDS)`; on any exception, `future.cancel(true)`.
8. Deserialize the `byte[]` reply into `FcbKafkaBaseResponse`. Error routing via
   `KafkaErrorCodeMapper`: HTTP-style `errorCode` in 500–599 → `FcbServerException`
   (retryable, climbs back into step 2); 400–499 → terminal
   `CoreBankingErrors.KAFKA_FCB_CLIENT_ERROR`; otherwise →
   `KAFKA_FCB_BUSINESS_ERROR`. Domain failures travel as
   `ir.dotin.platform.commons.core.Result<>` / `Notification`, not Java exceptions.

### Correlation + reply routing

- `eventUid` (UUID) is the request key, the `eventUid` header, and the `Idempotency-Key`
  header.
- **Partition-per-instance reply routing.** Each Nova pod claims exactly one partition of
  the reply topic (`FcbReplyPartitionResolver.resolve(instanceId, replyTopicPartitions)`),
  derived from `MessagingProperties.kafka.instanceId`. The reply container is a
  manually-assigned `ConcurrentMessageListenerContainer` with `concurrency=1` pinned to
  that partition (`FcbKafkaConfig#manualAssignContainer`). Every outbound record stamps
  `KafkaHeaders.REPLY_TOPIC` + `KafkaHeaders.REPLY_PARTITION` so FCB delivers the reply
  back to the correct pod. **Broker provisioning of `reply-topic-partitions` (default 12)
  must match what FCB's `OperationCategory.NOVA_*` config expects.**

### Mandatory request headers (stamped by `FcbKafkaClient` / `FcbHealthProbe`)

`X-Operation-Type`, `eventUid`, `Idempotency-Key`, `X-Request-DateTime`,
`Authorization`, `Accept-Language: fa`, `X-Request-Timestamp-Epoch-Ms`,
`X-Request-Deadline-Epoch-Ms`, `X-Host`, `traceparent` (W3C, only when a span is
active), plus the envelope codec's headers. Renaming or removing any of these is a
contract change on **both** sides.

### Operation-name registry

Every operation name is declared in `FcbKafkaBaseRequest`'s `@JsonSubTypes`. The string
values (e.g. `load-economicalSection-by-code`, `get-loan-file-number`,
`nova-open-account`, `nova-delete-account`, `electronic-bill-create-account`,
`issue-general-document`, `cancel-transfer-money-loan`, `load-assurance-service`,
`fetch-sanction-details`, `validate-samat`, `heartbeat`, …) must match FCB's
`NovaIntegrationOperationType`. Adding a new operation = new `*Request` DTO + new
`@JsonSubTypes.Type` entry + new `name=` value matching the FCB enum, in the same change.

### `ProducerCode`

`FcbKafkaClient` always stamps `producerCode = "NOVA"` on the request body. Do not
parameterise.

## Health probe

`FcbHealthProbe`:

- Started on `ApplicationReadyEvent`, gated on `FcbHealthProperties.enabled` (default
  true).
- Owns its own threads — a virtual-thread loop `fcb-kafka-health-probe` and a
  `Executors.newThreadPerTaskExecutor(Thread.ofVirtual()…)` per-partition fanout. **This
  is the only sanctioned thread pool in the module.**
- Per cycle: call `partitionsFor(healthRequestTopic)`, submit one `HeartbeatRequest` per
  partition with `X-Health-Probe: true`, wait up to `2 × probeTimeout`. Replies arrive
  on the dedicated `FCB_HEALTH_REPLYING_TEMPLATE` pinned to `FCB_HEALTH_REPLY_PARTITION`
  (the topic is provisioned with `health-reply-topic-partitions=1` by default and the
  container is `concurrency=1` — health traffic is *isolated* from integration traffic so
  an integration rebalance cannot block liveness signals; see
  `FcbKafkaTopicBindings.healthReply` comment).
- Probe actor envelope uses `InitiatorType.SYSTEM_HEALTH_PROBE`,
  `ExecutionTrigger.HEALTH_PROBE`, `ExecutionMode.ASYNC`.
- Watchdog: if no successful cycle within
  `2 × probeInterval + probeTimeout`, all partitions are marked unhealthy.
- Gauges (`FcbHealthAutoConfig`): `fcb.kafka.partitions.healthy`,
  `fcb.kafka.partitions.known`.
- `FcbHealthGate.checkPermitted` is the only sync coupling between probe and request
  path. When closed it short-circuits the client with
  `CoreBankingErrors.KAFKA_BROKER_UNAVAILABLE` — **before** retries — so sustained
  outages do not burn the retry budget.

## Security

- OAuth2 client-credentials token via `ServiceTokenProvider` (platform-security-api).
  The platform layer caches; do **not** add a local cache.
- Envelope: every outbound record carries a signed `ActorEnvelope`. This module does
  **not** verify inbound envelopes — there are no inbound (business) consumers here.
  When Nova grows inbound event consumers for FCB-emitted events, envelope verification
  is mandatory and belongs in that adapter, mirroring FCB's `security/envelope` rules
  (drop/DLT on missing/invalid envelope; never silently process).
- JWKS is **not** served or fetched here; that lives at the platform-security layer.

## Resilience: what NOT to do

These are real prior-incident learnings (some mirrored from the FCB-side CLAUDE.md):

1. **Do not add a thread pool on the request/reply hot path.** Stay on the caller's
   thread; `ReplyingKafkaTemplate` already manages async/correlation. The FCB side has a
   sister rule — a previous dispatcher thread pool slowed things down — and the symmetric
   mistake on Nova would be wrapping `sendAndReceive` in an executor.
2. **Do not reintroduce a circuit breaker.** The legacy Resilience4j circuit breaker was
   removed and replaced by `FcbHealthGate` (active heartbeat instead of consuming user
   requests as probes). See `FcbResilienceConfig`'s class Javadoc.
3. **Do not roll your own retries.** Retry is centrally bounded in `FcbResilienceConfig`
   and intentionally short — in a financial system, retrying a known-broken downstream
   eats the SLO. `FcbHealthGate` catches sustained outages.
4. **Do not leak Kafka types into application or domain.** `ProducerRecord`,
   `ConsumerRecord`, `RecordHeader`, and `KafkaHeaders` belong in `service/` and
   `health/`; mappers stop at `FcbKafkaBaseRequest`/`Response`. Domain ports speak
   `Result<DomainVO>`.
5. **Do not customise the global `ObjectMapper`.** Use the injected platform bean; the
   one bit of locally-scoped Jackson config that *is* OK is the `@JsonSubTypes` registry
   on `FcbKafkaBaseRequest`.
6. **Both Jackson namespaces are intentional.** Runtime serialization uses Jackson 3
   (`tools.jackson.databind.ObjectMapper`); `FcbKafkaBaseRequest`'s `@JsonTypeInfo` +
   `@JsonSubTypes` annotations are still in `com.fasterxml.jackson.annotation` (Jackson
   2 namespace, kept for compatibility with the legacy FCB side). Do not "unify" by
   deleting one.
7. **Clock is wrong on prod servers** (UTC stamped as `+03:30`). Fix will be central —
   inject `java.time.Clock` everywhere new (the probe already does;
   `FcbHealthAutoConfig#fcbHealthClock = Clock.systemUTC()`). Do not bake workarounds.
   `FcbKafkaClient.sendAndReceive` still uses `ZonedDateTime.now()` for the body
   timestamp — that is the next migration point; do not copy that pattern in new code.
8. **Multi-instance.** Every Nova pod owns exactly one reply partition. Reply-topic
   partition count must match the number of pods you expect to support; provisioning is
   a deploy-time concern, not a per-call concern.

## Configuration

There is exactly **one** local config file in this whole service:
`container/src/main/resources/bootstrap.yml`. Everything else (datasource, kafka,
security, fcb topic names, dispatcher settings) lives in **Consul** as KV under prefixes
`core/loan/nova` (overrides) and `core/loan` (shared defaults), first-prefix-wins. The
`nova-config` repository owns Consul content; CI syncs it (prod) or the
`git2consul-sync` sidecar syncs it (dev/test). Secrets are env-vars only — never in
Consul.

This module's `@ConfigurationProperties`:

- `FcbKafkaProperties` (`nova.fcb.kafka.*`): `request-topic`, `reply-topic`,
  `health-request-topic`, `health-reply-topic`, `default-timeout` (10s),
  `transaction-timeout` (60s), `reply-topic-partitions` (12),
  `health-reply-topic-partitions` (1). All four topic names are `@NotBlank`.
- `FcbHealthProperties` (probe enabled, interval, timeout, fail-open policy) and
  `HealthActorProperties` (probe `sub`, `branchCode`) — both bound by
  `FcbHealthAutoConfig`.

Topic-name strings in Consul **must match** FCB's ZooKeeper `OperationCategory.NOVA_*`
entries byte-for-byte. If you rename a topic, change both repos in the same MR.

## Contract sync rule

A change to any of the following requires a paired change on FCB **in the same MR**,
grep-verified across both repos:

- `*Request` / `*Response` DTO field shape;
- a header constant in `FcbKafkaClient` / `FcbHealthProbe`;
- an operation-name string in `FcbKafkaBaseRequest @JsonSubTypes`;
- a topic name in Consul `nova.fcb.kafka.*` (matched against FCB
  `OperationCategory.NOVA_*`);
- partition count for the reply topic (matched against FCB-side topic provisioning).

Grep both repos for the symbol before editing. When working across both repos, start
from `/home/m.amirabdollahi/workspaces` so relative paths line up.

## Commit message convention

Format observed on this branch (`git log --oneline -30`):

```
<type>(<TICKET-ID>:<SUB-TICKET-ID>[:<LEAF-TICKET-ID>]): <English description>
```

Examples:

```
feat(CBS-280274:LN-59163:LN-59355): wire spring-config-schema plugin in container
refactor(CBS-280274:LN-59163:LN-59295): Kafka consumer group storm fix — FCB containers via SPI
fix(CBS-280274:LN-59163:LN-59342): drop unsupported watch.timeout from consul config
```

Conventional Commits prefix (`feat`/`fix`/`chore`/`refactor`), colon-chained ticket IDs,
English description. **This deliberately differs from the FCB repo's Persian,
` : ` / ` | `-separated style — do not copy the FCB convention here.**
