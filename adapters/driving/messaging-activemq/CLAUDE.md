# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Module: trade-loan-adapters-driving-messaging-activemq

Synchronous **JMS/ActiveMQ driving adapter** for the trade-loan service. Hosts a single `@JmsListener` consumer that translates inbound ActiveMQ text messages into `*Command` objects, runs them through the platform's `InboundCommandProcessor` (request/reply flow-command machinery), and publishes the response back via `ResponsePublisher`. AsyncAPI documentation is generated via Springwolf. No business logic lives here — the consumer is a thin translation layer.

Sibling of `adapters/driving/messaging-kafka`. Unlike that module, **there is no Inbox path here** — ActiveMQ is used strictly for the request/reply full-lifecycle flow. If a new flow needs Inbox semantics, route it through Kafka instead, not by retrofitting Inbox onto JMS.

Maven artifact: `ir.dotin.loan:trade-loan-adapters-driving-messaging-activemq`. Packaged as a plain `jar` — the runnable Spring Boot app lives in `container/`.

## Build / Test

See root [CLAUDE.md → Build & Test](../../../CLAUDE.md#build--test-single-source-of-truth). Maven path: `adapters/driving/messaging-activemq`.

No `src/main/resources`, no `src/test/java` today. ActiveMQ broker URL, listener container factory, connection pool, and platform messaging properties live in `container/`. Architectural compliance is enforced by ArchUnit from the root `architecture-tests` module.

## Package Layout

```
ir.dotin.loan.trade.adapters.driving.messaging.activemq
├── consumer/
│   └── FullLifecycleJmsCommandConsumer.java   # full-lifecycle request queue → InboundCommandProcessor
└── config/
    └── ActiveMqJmsConfig.java                 # queue-name constants only; infra comes from platform starter
```

`ActiveMqJmsConfig` is intentionally minimal — base JMS infrastructure (`jmsListenerContainerFactory`, `JmsTemplate`, pooled connection factory) is provided by `platform-spring-boot-starter-messaging-activemq`. Do not redefine those beans here; if you need overrides, add them to `container/` or push the change into the platform starter.

## Consumer Pattern: Request/Reply Flow Command

`FullLifecycleJmsCommandConsumer` listens on `corridor.core.loan.nova.full-lifecycle.request.queue.v1` and processes synchronously:

1. Reject non-`TextMessage` (throws — broker DLQs the message).
2. Convert to platform `InboundMessage` via `JmsInboundMessageConverter`.
3. `validateRequiredHeaders(...)` — fail fast on missing `idempotencyKey`, `requestDateTime`, `acceptLanguage`, `authorizationToken`, `traceparent`, `correlationId`, `flowFailureMode`, `flowCorrelationId`.
4. Deserialize JSON payload into `FullLoanFacilityLifecycleMessage` (contract DTO).
5. Map DTO → `FullLoanFacilityLifecycleCommand` via `FullLoanFacilityLifecycleMessageMapper`, stamp `uid = UUID.randomUUID()` and a default `TransactionMetadataDto` (channel=`ACTIVEMQ`, terminalType=`MESSAGING`, etc.).
6. Re-serialize the command and hand the `InboundMessage` (with command bytes as payload) to `InboundCommandProcessor.process(...)`.
7. If `responseDestination` is set, publish the `CommandResponse` back via `jmsResponsePublisher.publish(destination, correlationKey, response)`.

Header validation order matches the Kafka sibling so the two consumers behave identically at the edge. Keep them in lockstep — divergence creates per-transport bugs.

## AsyncAPI / Springwolf

- `@AsyncListener(operation = @AsyncOperation(channelName = "<queue>", servers = "activemq", headers = @AsyncOperation.Headers(schemaName = MessagingHeaderNames.SCHEMA_FLOW_COMMAND_HEADERS)))` sits alongside `@JmsListener`. Keep `channelName` literally equal to the `destination` value — Springwolf generates docs from `@AsyncListener`, not `@JmsListener`.
- `servers = "activemq"` (not `"kafka"`) — the server name is what the AsyncAPI doc renders as the binding. A copy-paste from the Kafka sibling that leaves `servers = "kafka"` will silently misroute the doc.
- `schemaName = SCHEMA_FLOW_COMMAND_HEADERS` for flow/request-reply (this module only has that flavor today).
- `springwolf-jms` + `springwolf-ui` are on the classpath; serving comes from `container/`. No Springwolf config in this module.

## Anti-Corruption: Legacy Terms

Inbound JMS payloads may carry legacy terms. The boundary stops at the contract DTO and its MapStruct mapper in `adapters/driving/contract`. Commands, application services, outbound ports, and domain code use domain terms only. If a new field arrives that the mapper doesn't expose, extend the mapper — don't leak the legacy name past it.

## Queue & Header Conventions

- Queue naming: `corridor.<domain>.loan.nova.<flow>.request.queue.v<n>`. `v<n>` is *channel* versioning, separate from `core/application/ports/inbound` command versioning.
- Idempotency: `Idempotency-Key` header is validated as present but the current code uses `UUID.randomUUID()` for `command.uid`. If/when the platform JMS converter exposes the inbound idempotency key, propagate it into `command.uid` for cross-transport replay equivalence with Kafka.
- Default `TransactionMetadata` is hard-coded for ActiveMQ traffic (branchCode=`1`, userId=`SYSTEM`, terminalId=`ACTIVEMQ`, channel=`ACTIVEMQ`, …). If you need per-message metadata, derive it from JMS headers in the consumer — don't push the responsibility into the application service.
- `responseDestination` and `correlationKey` come from the platform's `InboundMessage` (typically populated from `JMSReplyTo` / `JMSCorrelationID`). Skip publishing if `responseDestination` is null or blank — that's a fire-and-forget pattern, valid for some callers.

## Dependency Rules (this module)

- **May depend on:**
  - `adapters/driving/contract` — DTOs (`FullLoanFacilityLifecycleMessage`) and their MapStruct mappers
  - `core/application/ports/inbound` — `*Command` types
  - `platform-messaging-api`, `platform-messaging-core`, `platform-spring-boot-starter-messaging-activemq` — `InboundMessage`, `JmsInboundMessageConverter`, `InboundCommandProcessor`, `CommandSerializer`, `ResponsePublisher`, `MessagingHeaderNames`
  - `platform-spring-boot-starter-asyncapi`, `springwolf-jms`, `springwolf-ui` — AsyncAPI generation
  - `jakarta.jms-api`, Spring JMS, Jackson (`tools.jackson.databind`), Lombok
- **Must NOT depend on:**
  - `core/application/ports/outbound` (forbidden — driving adapters never see outbound ports)
  - `core/application/service` (services run behind `InboundCommandProcessor` / `CommandDispatcher`, never injected directly)
  - `core/domain` (domain types never cross the messaging boundary)
  - any driven adapter (`adapters/driven/persistence`, `adapters/driven/fcb-messaging`)
  - sibling driving adapters (`rest`, `messaging-kafka`)
- ArchUnit in the root `architecture-tests` module enforces these. If a build fails on a "may not depend on" violation here, fix the design — don't suppress.

## Pitfalls

- **`servers = "activemq"` in `@AsyncOperation`.** Easy to miss when copy-pasting from the Kafka sibling.
- **Don't make the consumer `@Transactional`.** JMS session ack mode is configured by the platform container factory; wrapping the listener in a JDBC transaction conflates two unrelated commit boundaries.
- **Don't catch and swallow exceptions in `consume(...)`.** Header-validation and conversion failures must throw so the broker's redelivery / DLQ policy can engage. The pattern is "fail fast at the edge" — handling belongs to the broker, not the listener.
- **Don't add an Inbox path here.** This module is request/reply only. Inbox-style ingestion goes through `messaging-kafka` so retry semantics stay consistent with the rest of the platform's Inbox infrastructure.
- **`TextMessage` only.** `BytesMessage` and others throw `IllegalArgumentException` by design. If a producer starts sending `BytesMessage`, fix the producer or extend the converter — don't branch inside `consume(...)`.
- **Header validation belongs at the listener edge, not in `InboundCommandProcessor`.** Missing headers are a transport-contract violation; surfacing them as command-handler errors loses the diagnostic.
- **Keep header validation in lockstep with the Kafka sibling.** Both consumers list the same required headers (one extra here: `flowCorrelationId`). When the platform header set changes, update both files in the same commit.
