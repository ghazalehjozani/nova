# CLAUDE.md — ActiveMQ Driving Adapter

## Overview

Driving adapter that consumes **FullLoanFacilityLifecycle** commands over
ActiveMQ/JMS using the request–reply pattern.  Sits alongside the existing
Kafka adapter (`trade-loan-adapters-driving-messaging`) and reuses its DTOs
and MapStruct mapper so the domain layer stays transport-agnostic.

### Architecture Flow

```
ActiveMQ Queue → JmsListener → Consumer → CommandProcessor → Domain
                                  ↓
                 JMSReplyTo / responseQueue ← JmsReplyPublisher ← Reply
```

## Package Structure

```
adapters/driving/activemq/
├── config/        ActiveMqJmsConfig          – connection factory, listener factory, JmsTemplate
├── consumer/      TradeLoanJmsCommandConsumer – @JmsListener, deser, map, process, reply
├── publisher/     JmsReplyPublisher           – send reply to JMSReplyTo or named queue
├── dto/           JmsCommandReply             – reply wire format (status, error)
└── support/       JmsRawCommandMessageFactory – bridges JMS Message → RawCommandMessage
```

## Request–Reply Destination Resolution

The adapter supports **both** standard JMS and the Kafka-compatible convention:

| Priority | Source                | Mechanism                                   |
|----------|-----------------------|---------------------------------------------|
| 1        | `JMSReplyTo` header   | Temporary queue created by caller           |
| 2        | `responseTopic` field | Named queue in JSON body (Kafka convention) |
| —        | Neither               | Fire-and-forget; no reply sent              |

## Correlation

- `JMSCorrelationID` from the inbound message is copied to the reply.
- If absent, falls back to `JMSMessageID`, then generates a UUID.
- Callers waiting on a shared reply queue can use selector:
  `JMSCorrelationID = '<id>'`.

## Key Design Decisions

1. **Reuses DTOs/mapper** from the Kafka messaging module to avoid
   duplication.  If the two modules diverge, extract a
   `trade-loan-adapters-driving-messaging-common` module.

2. **JmsRawCommandMessageFactory** builds a synthetic `ConsumerRecord`
   so the platform's `CommandProcessor` pipeline works unchanged.
   JMS string properties are mapped 1-to-1 to Kafka record headers.

3. **Connection pooling** via `pooled-jms` — a single shared pool avoids
   the cost of creating a connection per listener thread.

4. **CLIENT_ACKNOWLEDGE** — acknowledgement is deferred until the
   `CommandProcessor` completes, keeping idempotency semantics intact.

## Configuration

```properties
spring.activemq.broker-url=tcp://localhost:61616
spring.activemq.user=admin
spring.activemq.password=admin
spring.activemq.pool.enabled=true
spring.activemq.pool.max-connections=10

trade-loan.activemq.reply.timeout-ms=30000
trade-loan.activemq.listener.concurrency=3-10
```

## Standards Compliance

Same rules as the Kafka adapter (SWA.101 v1.1) apply:

- Mandatory headers: `Idempotency-Key`, `X-Request-DateTime`, `Accept-Language`
  → set as **JMS string properties** instead of Kafka headers.
- Message body: JSON with optional `metadata` + business fields.
- Queue naming: `corridor.{domain}.{service}.{operation}.{type}.queue.v{version}`
- Error codes: `{PREFIX}-{SEQUENCE}` (e.g. `LOAN-0500`).

## Testing

Use embedded ActiveMQ broker (`activemq-broker` dependency, test scope):

```java
@SpringBootTest
@EmbeddedActiveMQ
class TradeLoanJmsCommandConsumerTest {
    @Autowired JmsTemplate jmsTemplate;
    @MockBean  CommandProcessor processor;

    @Test
    void shouldProcessAndReply() {
        // send TextMessage to request queue, verify processor.process() called,
        // receive reply from temp queue, assert status == SUCCESS
    }
}
```
