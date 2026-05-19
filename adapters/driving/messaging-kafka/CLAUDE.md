# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Module: trade-loan-adapters-driving-messaging-kafka

Asynchronous **Kafka driving adapter** for the trade-loan service. Owns Spring Kafka `@KafkaListener` consumers and the Inbox-pattern message handlers that translate inbound Kafka records into `*Command` objects and hand them to `CommandDispatcher`. AsyncAPI documentation is generated via Springwolf. No business logic lives here — consumers are thin translation layers, identical in spirit to the REST controllers in the sibling `rest` module but driven by Kafka topics.

Maven artifact: `ir.dotin.loan:trade-loan-adapters-driving-messaging-kafka`. Packaged as a plain `jar` — the runnable Spring Boot app lives in `container/`.

## Build / Test

See root [CLAUDE.md → Build & Test](../../../CLAUDE.md#build--test-single-source-of-truth). Maven path: `adapters/driving/messaging-kafka`. Single test example: `mvn -pl adapters/driving/messaging-kafka -am test -Dtest=FcbEventConsumerTest`.

No `src/main/resources` and no `src/test/java` today — Kafka container factories, topic/group config, and platform messaging properties live in the `container/` module. Architectural compliance is enforced by ArchUnit from the root `architecture-tests` module.

## Package Layout

```
ir.dotin.loan.trade.adapters.driving.messaging.kafka
└── consumer/
    ├── FcbEventConsumer.java                 # FCB envelope topic → InboundEventIngestor (Inbox)
    ├── FullLifecycleKafkaCommandConsumer.java # request/reply flow command, no Inbox
    └── handler/                              # one InboxMessageHandler per FCB operationType
        ├── CloseFacilityPaidOffHandler.java
        ├── CloseFacilityPaidOffCompensateHandler.java
        ├── CancelLoanFacilityHandler.java
        ├── InstallmentCollectionHandler.java
        ├── InstallmentCollectionCompensateHandler.java
        ├── LoanFacilityRestructuringHandler.java
        └── CollateralUpdateHandler.java
```

Consumers are `public @Component` (Spring discovers them by classpath scan). Handlers implement `InboxMessageHandler` from `platform-inbox-api` and are routed by `supportedMessageType()` matching `FcbEventOperationType.getCode()`.

## Two Distinct Consumer Patterns

This module hosts **two** flow styles. Don't conflate them.

### 1. FCB Inbox-driven (envelope ingest → handler dispatch)

`FcbEventConsumer` listens on `corridor.core.loan.nova.installment-operation.request.queue.v1` and treats it as an envelope topic carrying many `operationType`s (CLOSE_PAID_OFF, INSTALLMENT_COLLECTION, CANCELLATION_LOAN_FACILITY, …). It resolves `operationType` (header first, body fallback) and `eventUid`, then hands the `InboundMessage` to `InboundEventIngestor` — the platform's **Inbox** machinery, which persists the event and later replays it into the matching `InboxMessageHandler` (the `handler/` subpackage).

Why this matters: the Kafka offset advances on **ingest**, not on business processing. Business processing happens later, idempotently, replayed by the Inbox. The handler returns `HandlerResult.success()` / `.retryable(e)` / `.permanent(e)` to drive Inbox retry behavior — NOT to control Kafka commit.

### 2. Direct request/reply flow command (no Inbox)

`FullLifecycleKafkaCommandConsumer` listens on `corridor.core.loan.nova.full-lifecycle.request.queue.v1` and processes synchronously through `InboundCommandProcessor` (platform flow-command machinery, not the Inbox). The response is published back to the message's `responseDestination` via the injected `ResponsePublisher kafkaResponsePublisher`. Required headers are validated up front (`idempotencyKey`, `requestDateTime`, `acceptLanguage`, `authorizationToken`, `traceparent`, `correlationId`, `flowFailureMode`) — missing any throws and skips the record.

The two patterns share the topic prefix (`corridor.core.loan.nova.…`) but differ in *who owns retries*: Inbox owns retries for pattern 1, the upstream caller owns them (via request/reply) for pattern 2.

## Canonical Handler Shape (Inbox path)

```java
@Component
@RequiredArgsConstructor
public class InstallmentCollectionHandler implements InboxMessageHandler {
    private final ObjectMapper objectMapper;
    private final InstallmentCollectionMessageMapper messageMapper;   // from -contract
    private final CommandDispatcher dispatcher;

    @Override public @NonNull String supportedMessageType() {
        return FcbEventOperationType.INSTALLMENT_COLLECTION.getCode();
    }

    @Override public @NonNull HandlerResult handle(@NonNull InboundMessage message) {
        InstallmentPaymentMessage paymentMessage;
        String eventUid = "unknown";
        try {
            JsonNode rootNode = objectMapper.readTree(message.payload());
            eventUid = rootNode.path("eventUid").asString(eventUid);
            paymentMessage = objectMapper.treeToValue(rootNode, InstallmentPaymentMessage.class);
        } catch (Exception e) {
            return HandlerResult.permanent(e);              // parse errors are NOT retryable
        }
        try {
            CollectInstallmentCommand command = messageMapper.toCommand(paymentMessage);
            ExecutionResult<?> result = dispatcher.dispatch(command);
            return switch (result) {
                case ExecutionResult.Fresh<?> ignored        -> HandlerResult.success();
                case ExecutionResult.Replayed<?> ignored     -> HandlerResult.success();
                case ExecutionResult.BusinessFailure<?> f    -> HandlerResult.permanent(
                        new IllegalStateException(f.notification().toString()));
            };
        } catch (Exception e) {
            return HandlerResult.retryable(e);              // infra failure → retry
        }
    }
}
```

Rules:

- **Parse failure → `HandlerResult.permanent`.** Malformed payload will never succeed on replay.
- **`BusinessFailure` → `HandlerResult.permanent`.** Domain rule rejected it; retrying won't change the answer. Wrap `failure.notification().toString()` for ops visibility.
- **Infra/`Exception` → `HandlerResult.retryable`.** Lets Inbox back off and re-deliver.
- **`Fresh` and `Replayed` both → success.** The dispatcher's idempotency handling means a replay is success, not a duplicate to be flagged.
- **Use the contract mapper** (`*MessageMapper` from `adapters/driving/contract`) for DTO→Command. Don't build commands inline.
- **One handler per `operationType`.** `supportedMessageType()` is the only routing key the Inbox uses; collisions will mean the wrong handler fires silently.

## AsyncAPI / Springwolf

- Every listener method carries an `@AsyncListener(operation = @AsyncOperation(channelName = …, description = …, servers = "kafka", headers = @AsyncOperation.Headers(schemaName = …)))` annotation alongside `@KafkaListener`. Keep the `channelName` literally equal to the `topics` value — Springwolf builds the AsyncAPI doc from `@AsyncListener`, not from `@KafkaListener`, so a divergence will publish docs that don't match runtime topics.
- `schemaName` selects the header schema in the generated AsyncAPI: `SCHEMA_EVENT_HANDLER_HEADERS` for FCB inbox topics, `SCHEMA_FLOW_COMMAND_HEADERS` for flow/request-reply topics. Pick the one matching the consumer pattern.
- `springwolf-ui` is on the classpath and is served by `container/`. There is no Springwolf config in this module — only the annotations.

## Anti-Corruption: Legacy Terms

FCB payloads use legacy terms (e.g. `fileNumber`). Per the root architecture rules, **legacy terms stop at the adapter boundary.** They appear in:

- The contract DTOs (`InstallmentPaymentMessage.fileNumber()` etc.) — fine, those are adapter-layer.
- The `LOG.info("…fileNumber={}…")` lines in handlers — fine, logging the raw inbound value.

They must NOT appear in commands, application services, outbound ports, or domain code. The `*MessageMapper` in `adapters/driving/contract` is the translation seam (`fileNumber` → `applicationNumber`/`facilityId`/etc.). If a new handler needs a legacy field the mapper doesn't expose, extend the mapper — do not leak the legacy term past it.

## Topic & Header Conventions

- Topic naming: `corridor.<domain>.loan.nova.<flow>.request.queue.v<n>`. The `v<n>` is *channel* versioning, separate from `core/application/ports/inbound` command versioning. Don't bump one expecting the other to follow.
- `groupId = "${platform.messaging.kafka.consumer-group-id}"` — a single shared group across all listeners in the service. Don't hard-code group IDs; they're environment-resolved in `container/`.
- `containerFactory = "byteArrayKafkaListenerContainerFactory"` — payload is raw `byte[]`, deserialized inside `consume()`. Don't switch to a JSON deserializer at the listener level; the Inbox needs the raw payload byte stream.
- Idempotency: for direct flow commands, the inbound `Idempotency-Key` header drives `command.uid` (see `FullLifecycleKafkaCommandConsumer` — note that file currently uses `UUID.randomUUID()` as a fallback, which is documented as a FIXME for upstream propagation). For Inbox-driven handlers, the dispatcher derives idempotency from the command payload + Inbox `eventUid`.

## Dependency Rules (this module)

- **May depend on:**
  - `adapters/driving/contract` — DTOs (`*Message`, `*Request`, `FcbEventOperationType`) and their MapStruct mappers
  - `core/application/ports/inbound` — `*Command` types
  - `platform-messaging-api`, `platform-messaging-core`, `platform-spring-boot-starter-messaging-kafka` — `InboundMessage`, `KafkaInboundMessageConverter`, `InboundCommandProcessor`, `CommandSerializer`, `ResponsePublisher`
  - `platform-inbox-core` — `InboundEventIngestor`, `InboxMessageHandler`, `HandlerResult`
  - `platform-dispatcher-api` — `CommandDispatcher`, `ExecutionResult`
  - `platform-spring-boot-starter-asyncapi`, `springwolf-kafka`, `springwolf-ui` — AsyncAPI generation
  - `platform-security-api` — `AuthenticationContextHolder` (used where the handler needs to attribute an action to a user, e.g. `LoanFacilityRestructuringHandler`)
  - `spring-kafka`, Jackson (`tools.jackson.databind`), Lombok, time4j (currency/calendar helpers)
- **Must NOT depend on:**
  - `core/application/ports/outbound` (forbidden — driving adapters are read-only against the inbound side)
  - `core/application/service` (services run behind the `CommandDispatcher`, never injected directly)
  - `core/domain` (domain types never cross the messaging boundary)
  - any driven adapter (`adapters/driven/persistence`, `adapters/driven/fcb-messaging`)
  - sibling driving adapters (`rest`, `messaging-activemq`)
- ArchUnit in the root `architecture-tests` module enforces these. If a build fails on a "may not depend on" violation here, fix the design — don't suppress.

## Pitfalls

- **Don't commit Kafka offsets manually.** The platform's container factory handles ack mode. Manual ack inside a listener is a foot-gun: an Inbox-ingested record must ack on ingest, not on handler success.
- **Don't make consumers `@Transactional`.** Kafka consumption is not part of a JDBC transaction; the Inbox owns its own transactional boundary on ingest.
- **Don't share an `ObjectMapper` instance you reconfigure locally.** The injected mapper is the platform-configured one (Jackson 3 / `tools.jackson.databind`); mutating it leaks to every other consumer.
- **`@AsyncListener` and `@KafkaListener` `channelName`/`topics` must stay in lockstep.** A copy-paste new consumer with a stale channel name will publish wrong AsyncAPI docs and pass tests.
- **Header validation belongs at the listener edge, not the handler.** See `FullLifecycleKafkaCommandConsumer.validateRequiredHeaders` — fail fast before invoking the command processor, so missing-header records don't end up in retry loops.
- **Don't add response publishing to Inbox handlers.** Handlers return `HandlerResult`; the Inbox machinery handles the rest. Pattern-2 (`ResponsePublisher`) is for the synchronous flow consumer only.
- **`CancelLoanFacilityHandler` log message mistakenly says "Failed to process INSTALLMENT_COLLECTION" inside the cancellation handler** — known copy-paste; fix opportunistically when touching that file.
