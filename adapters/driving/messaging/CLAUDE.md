# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Messaging Adapter Implementation Guide

### 1. Overview

The messaging adapter is a **driving adapter** in hexagonal architecture that consumes Kafka messages and translates them into Commands for processing. It also publishes response messages for request/reply patterns and domain events.

### Architecture Flow
```
Kafka Topic → Consumer → Dispatcher → Command Handler → Domain → Response Publisher → Kafka Topic
```

### Key Components
- **Consumers**: Listen to Kafka topics and consume messages
- **Publishers**: Publish messages to Kafka topics (responses, events)
- **DTOs**: Message payload structures
- **Mappers**: Convert messages to/from commands
- **CommandProcessor**: Routes messages to command handlers

## 2. Package Structure

```
adapters/driving/messaging/
├── consumer/                       # Kafka message consumers
│   ├── TradeLoanCommandConsumer.java
│   └── InstallmentOperationCommandConsumer.java
├── publisher/                      # Kafka message publishers
│   └── InstallmentOperationResponsePublisher.java
├── dto/                           # Message DTOs (wire format)
│   ├── FullLoanFacilityLifecycleMessage.java
│   ├── InstallmentPaymentMessage.java
│   └── InstallmentOperationResponse.java
├── mapper/                        # Message ↔ Command mappers
│   ├── FullLoanFacilityLifecycleMessageMapper.java
│   └── InstallmentPaymentMessageMapper.java
└── pom.xml
```

## 3. Inter-Service Communication Standards Compliance

**CRITICAL**: All messaging implementations **MUST** comply with the Inter-Service Communication Standards document (SWA.101 v1.1).

### 3.1 Message Schema Structure

All messages MUST follow this structure:

```
Message
├── Headers (Kafka headers)        # Mandatory: routing, correlation, tracing
└── Body (JSON payload)            # Optional metadata + business fields
```

### 3.2 Message Headers (Mandatory)

**For all messages**, the following Kafka headers are **REQUIRED**:

```
Idempotency-Key: UUID v4 (e.g., "123e4567-e89b-42d3-a456-556642440000")
X-Request-DateTime: ISO 8601 UTC timestamp (e.g., "2025-08-22T14:30:00.123Z")
Accept-Language: Language code (e.g., "fa" or "en-US")
```

**For command messages with type discrimination**, additional headers:

```
operationType: Operation discriminator (e.g., "INSTALLMENT_COLLECTION")
eventUid: Unique event identifier (UUID/GUID)
```

**For distributed tracing** (optional but recommended):

```
traceparent: W3C trace context parent ID
tracestate: W3C trace state
```

### 3.3 Message Body Structure

All message bodies MUST use this JSON structure:

```json
{
  "metadata": {
    // Optional: framework metadata for internal processing
    // Examples: tags, routing info, audit context
  },
  "businessField1": "value",
  "businessField2": "value"
  // ... actual business fields
}
```

**Rules:**
- `metadata` field is **optional** (use for framework-level data only)
- Business fields are **required** and domain-specific
- Use `camelCase` for all field names
- Dates: ISO 8601 format (`YYYY-MM-DDTHH:mm:ss.sssZ`)
- Money: Separate fields for amount (BigDecimal) and currency (String)
- Enums: Use string values (e.g., `"ACTIVE"`, not numeric codes)

### 3.4 Message Channel Naming Convention

Kafka topic names MUST follow this pattern:

```
corridor.{domain}.{service}.{operation}.{type}.queue.v{version}
```

**Examples:**
```
corridor.core.loan.nova.full-lifecycle.request.queue.v1
corridor.core.loan.nova.installment-operation.request.queue.v1
corridor.core.loan.nova.installment-operation.response.queue.v1
```

**Components:**
- `corridor`: Fixed prefix for message broker integration
- `{domain}`: Business domain (e.g., `core`, `esb-centric`)
- `{service}`: Service name (e.g., `loan`, `payment`)
- `{operation}`: Operation/feature name (kebab-case)
- `{type}`: `request`, `response`, or `event`
- `queue`: Fixed (Kafka topic = queue in this context)
- `v{version}`: Version number (e.g., `v1`, `v2`)

### 3.5 Request/Reply Pattern

For synchronous-like request/reply over async messaging:

1. **Request message** includes `responseTopic` field in body
2. **Consumer** processes request and publishes response to `responseTopic`
3. **Response message** includes:
   - Kafka key: `eventUid` (for correlation)
   - Headers: `eventUid`, `operationType`, `status`
   - Body: `status`, `fileNumber`, `errorMessage` (if failed)

**Example flow:**
```
Old System → [request topic] → Trade Loan Consumer
                                      ↓ (process)
                                   Success/Failure
                                      ↓
Old System ← [response topic] ← Trade Loan Publisher
```

### 3.6 Error Code Standards

Error codes in messages MUST follow this format:

```
{PREFIX}-{SEQUENCE}
```

**Format rules:**
- PREFIX: 2-6 characters (e.g., `LOAN`, `PAY`, `USR`)
- SEQUENCE: 4 digits (0001-9999)
- Codes 0001-0100: Reserved for cross-service standard errors

**Reserved codes:**

| Code   | Meaning                     |
|--------|-----------------------------|
| *-0001 | Invalid credentials         |
| *-0002 | Invalid/missing token       |
| *-0003 | Insufficient permissions    |
| *-0004 | Duplicate in progress       |
| *-0005 | Duplicate already processed |
| *-0006 | Rate limit exceeded         |

**Custom codes (0101+):**
Service-specific business errors (e.g., `LOAN-0101`, `LOAN-0102`)

### 3.7 Metadata Field Usage

The `metadata` field is **optional** and should only contain:
- Framework-level processing hints
- Tags for categorization (e.g., `["deposit", "fraud"]`)
- Audit context (NOT business data)
- Internal routing information

**Do NOT put business data in metadata.** All business data belongs in top-level fields.

## 4. Consumer Implementation

### 4.1 Simple Command Consumer Pattern

For single command type per topic:

```java
@Component
@RequiredArgsConstructor
public class TradeLoanCommandConsumer {

    private final CommandProcessor processor;

    @KafkaListener(
            topics = "corridor.core.loan.nova.full-lifecycle.request.queue.v1",
            groupId = "core.loan.facility.*",
            containerFactory = "byteArrayKafkaListenerContainerFactory")
    @AsyncListener(
            operation =
                    @AsyncOperation(
                            channelName = "corridor.core.loan.nova.full-lifecycle.request.queue.v1",
                            description = "Process nova loan full lifecycle commands",
                            headers =
                                    @AsyncOperation.Headers(
                                            schemaName = "CommandHeaders",
                                            values = {
                                                @AsyncOperation.Headers.Header(
                                                        name = "Idempotency-Key",
                                                        description = "Unique identifier for idempotency",
                                                        value = "UUID string"),
                                                @AsyncOperation.Headers.Header(
                                                        name = "X-Request-DateTime",
                                                        description = "Request timestamp",
                                                        value = "ISO-8601 format"),
                                                @AsyncOperation.Headers.Header(
                                                        name = "Accept-Language",
                                                        description = "Preferred language",
                                                        value = "Language code"),
                                                @AsyncOperation.Headers.Header(
                                                        name = "Authorization",
                                                        description = "Bearer token for authentication",
                                                        value = "Bearer token"),
                                                @AsyncOperation.Headers.Header(
                                                        name = "traceparent",
                                                        description = "W3C trace context",
                                                        value = "Trace parent ID"),
                                                @AsyncOperation.Headers.Header(
                                                        name = "tracestate",
                                                        description = "W3C trace state",
                                                        value = "Trace state")
                                            })))
    public void consume(ConsumerRecord<String, byte[]> consumerRecord) {
        RawCommandMessage rawMessage = RawCommandMessage.from(consumerRecord);
        processor.process(rawMessage);
    }
}
```

**Key points:**
- Use `@KafkaListener` with exact topic name
- Group ID format: `{domain}.{service}.{feature}.*`
- Container factory: `byteArrayKafkaListenerContainerFactory` (byte array payloads)
- Use `@AsyncListener` for AsyncAPI documentation (Springwolf)
- Delegate to `CommandProcessor` for dispatching

### 4.2 Multi-Operation Consumer Pattern (Request/Reply)

For shared topics with multiple operation types:

```java
@Component
@RequiredArgsConstructor
public class InstallmentOperationCommandConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(InstallmentOperationCommandConsumer.class);
    private static final String OPERATION_TYPE_INSTALLMENT_COLLECTION = "INSTALLMENT_COLLECTION";

    private final ObjectMapper objectMapper;
    private final CommandProcessor processor;
    private final InstallmentOperationResponsePublisher responsePublisher;

    @KafkaListener(
            topics = "corridor.core.loan.nova.installment-operation.request.queue.v1",
            groupId = "core.loan.installment-operation.*",
            containerFactory = "byteArrayKafkaListenerContainerFactory")
    @AsyncListener(
            operation =
            @AsyncOperation(
                    channelName = "corridor.core.loan.nova.installment-operation.request.queue.v1",
                    description = "Process nova installment operation commands (request/reply)",
                    headers =
                    @AsyncOperation.Headers(
                            schemaName = "InstallmentOperationHeaders",
                            values = {
                                    @AsyncOperation.Headers.Header(
                                            name = "eventUid",
                                            description = "Unique event identifier (GUID)",
                                            value = "UUID string"),
                                    @AsyncOperation.Headers.Header(
                                            name = "operationType",
                                            description = "Discriminator: INSTALLMENT_COLLECTION, etc.",
                                            value = "Operation type code"),
                                    @AsyncOperation.Headers.Header(
                                            name = "Idempotency-Key",
                                            description = "Unique identifier for idempotency",
                                            value = "UUID string")
                            })))
    public void consume(ConsumerRecord<String, byte[]> consumerRecord) {
        String operationType = resolveOperationType(consumerRecord);
        String eventUid = extractHeader(consumerRecord, "eventUid");

        LOG.info("Received installment operation [operationType={}, eventUid={}, key={}]",
                operationType, eventUid, consumerRecord.key());

        switch (operationType) {
            case OPERATION_TYPE_INSTALLMENT_COLLECTION ->
                    handleInstallmentCollection(consumerRecord, eventUid);

            // Future operations:
            // case "INSTALLMENT_PREPAYMENT" -> handleInstallmentPrepayment(...);
            // case "PENALTY_SETTLEMENT" -> handlePenaltySettlement(...);

            default -> {
                LOG.warn("Unknown operationType [{}], eventUid={}, skipping.", operationType, eventUid);
                sendErrorResponse(consumerRecord, eventUid, operationType, null,
                        "Unknown operationType: " + operationType);
            }
        }
    }

    private void handleInstallmentCollection(ConsumerRecord<String, byte[]> record, String eventUid) {
        InstallmentPaymentMessage message = null;
        try {
            message = objectMapper.readValue(record.value(), InstallmentPaymentMessage.class);

            LOG.info("Processing INSTALLMENT_COLLECTION [eventUid={}, fileNumber={}, payments={}]",
                    eventUid, message.fileNumber(),
                    message.payments() != null ? message.payments().size() : 0);

            RawCommandMessage rawMessage = RawCommandMessage.from(record);
            processor.process(rawMessage);

            // Request/Reply: send success response
            sendSuccessResponse(message);

        } catch (Exception e) {
            LOG.error("Failed to process INSTALLMENT_COLLECTION [eventUid={}]", eventUid, e);

            String fileNumber = message != null ? message.fileNumber() : null;
            sendErrorResponse(record, eventUid, OPERATION_TYPE_INSTALLMENT_COLLECTION,
                    fileNumber, e.getMessage());

            throw new RuntimeException("INSTALLMENT_COLLECTION processing failed: " + eventUid, e);
        }
    }

    private void sendSuccessResponse(InstallmentPaymentMessage message) {
        String responseTopic = message.responseTopic();
        if (responseTopic == null || responseTopic.isBlank()) {
            LOG.debug("No responseTopic in message [eventUid={}], skipping reply.",
                    message.eventUid());
            return;
        }

        InstallmentOperationResponse response = InstallmentOperationResponse.success(
                message.eventUid(),
                message.operationType(),
                message.fileNumber());

        responsePublisher.sendResponse(responseTopic, response);
    }

    private void sendErrorResponse(ConsumerRecord<String, byte[]> record,
                                   String eventUid, String operationType,
                                   String fileNumber, String errorMessage) {
        String responseTopic = resolveResponseTopic(record);
        if (responseTopic == null || responseTopic.isBlank()) {
            LOG.debug("No responseTopic available [eventUid={}], skipping error reply.", eventUid);
            return;
        }

        InstallmentOperationResponse response = InstallmentOperationResponse.failed(
                eventUid != null ? eventUid : "UNKNOWN",
                operationType,
                fileNumber != null ? fileNumber : "UNKNOWN",
                errorMessage);

        try {
            responsePublisher.sendResponse(responseTopic, response);
        } catch (Exception e) {
            LOG.error("Failed to send error response [eventUid={}]", eventUid, e);
        }
    }

    // Helper: Read operationType from header (cheap) or body (fallback)
    private String resolveOperationType(ConsumerRecord<String, byte[]> record) {
        String fromHeader = extractHeader(record, "operationType");
        if (fromHeader != null && !fromHeader.isEmpty()) {
            return fromHeader;
        }
        try {
            JsonNode root = objectMapper.readTree(record.value());
            JsonNode opNode = root.get("operationType");
            if (opNode != null && !opNode.isNull()) {
                return opNode.asText();
            }
        } catch (Exception e) {
            LOG.warn("Failed to parse operationType from message body", e);
        }
        return "UNKNOWN";
    }

    private String resolveResponseTopic(ConsumerRecord<String, byte[]> record) {
        try {
            JsonNode root = objectMapper.readTree(record.value());
            JsonNode node = root.get("responseTopic");
            if (node != null && !node.isNull()) {
                return node.asText();
            }
        } catch (Exception e) {
            LOG.warn("Failed to parse responseTopic from message body", e);
        }
        return null;
    }

    private String extractHeader(ConsumerRecord<String, byte[]> record, String headerName) {
        Header header = record.headers().lastHeader(headerName);
        if (header != null && header.value() != null) {
            return new String(header.value(), StandardCharsets.UTF_8);
        }
        return null;
    }
}
```

**Pattern highlights:**
- Switch on `operationType` for type discrimination
- Extract `operationType` from header (preferred) or body (fallback)
- Handle each operation type in dedicated method
- Always send response (success or error) for request/reply
- Extract `responseTopic` from message body
- Use `eventUid` as correlation key

## 5. Publisher Implementation

### 5.1 Request/Reply Response Publisher

```java
@Component
@RequiredArgsConstructor
public class InstallmentOperationResponsePublisher {

    private static final Logger LOG = LoggerFactory.getLogger(InstallmentOperationResponsePublisher.class);

    private final KafkaTemplate<String, byte[]> kafkaTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Sends a response to the given topic. Non-blocking — logs on success/failure.
     *
     * @param responseTopic  target topic (from inbound message's responseTopic field)
     * @param response       the response payload
     */
    public void sendResponse(String responseTopic, InstallmentOperationResponse response) {
        byte[] payload;
        try {
            payload = objectMapper.writeValueAsBytes(response);
        } catch (JsonProcessingException e) {
            LOG.error("Failed to serialize response [eventUid={}, topic={}]",
                    response.eventUid(), responseTopic, e);
            return;
        }

        ProducerRecord<String, byte[]> record =
                new ProducerRecord<>(responseTopic, response.eventUid(), payload);

        // Add mandatory headers
        record.headers()
                .add(new RecordHeader("eventUid",
                        response.eventUid().getBytes(StandardCharsets.UTF_8)))
                .add(new RecordHeader("operationType",
                        response.operationType().getBytes(StandardCharsets.UTF_8)))
                .add(new RecordHeader("status",
                        response.status().name().getBytes(StandardCharsets.UTF_8)));

        CompletableFuture<SendResult<String, byte[]>> future = kafkaTemplate.send(record);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                LOG.error("Failed to publish response [eventUid={}, topic={}]",
                        response.eventUid(), responseTopic, ex);
            } else {
                LOG.info("Response published [eventUid={}, topic={}, partition={}, offset={}]",
                        response.eventUid(), responseTopic,
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });
    }
}
```

**Key points:**
- Use `eventUid` as Kafka message key (for correlation and partition routing)
- Serialize response to JSON bytes using ObjectMapper
- Add headers: `eventUid`, `operationType`, `status`
- Send asynchronously with `CompletableFuture`
- Log success/failure for observability

## 6. Message DTOs

### 6.1 Command Message Structure

All command messages MUST implement `CommandPayload`:

```java
public record InstallmentPaymentMessage(
        // --- Corridor envelope (from AbstractMessage) ---
        String producerCode,
        String eventUid,
        @Nullable Date dateTime,
        int version,
        @Nullable String responseTopic,
        @Nullable String[] tags,

        // --- Nova envelope (operation discriminator) ---
        String operationType,
        String fileNumber,

        // --- Business fields ---
        String transactionNumber,
        @Nullable String documentNumber,
        String currency,
        BigDecimal totalPrincipalAmount,
        BigDecimal totalInterestAmount,
        List<PaymentDetailDto> payments,
        Map<String, Object> metadata)
        implements CommandPayload {

    public record PaymentDetailDto(
            int installmentSequenceNumber,
            BigDecimal principalAmount,
            BigDecimal interestAmount,
            @Nullable BigDecimal penaltyAmount,
            BigDecimal totalAmount,
            @Nullable String valueDate,
            boolean fullySettled) {
    }
}
```

**Design rules:**
- Use `record` for immutability
- Implement `CommandPayload` marker interface
- Envelope fields: `producerCode`, `eventUid`, `dateTime`, `version`, `responseTopic`, `tags`
- Discriminator field: `operationType` (for shared topics)
- Business fields: Domain-specific (amounts, dates, identifiers)
- `metadata` field: Optional framework-level data (Map<String, Object>)
- Use `@Nullable` for optional fields
- Nested DTOs for complex structures (lists of detail records)

### 6.2 Response Message Structure

```java
public record InstallmentOperationResponse(
        String eventUid,
        String operationType,
        String fileNumber,
        ResponseStatus status,
        @Nullable String errorMessage) {

    public enum ResponseStatus {
        SUCCESS,
        FAILED
    }

    public static InstallmentOperationResponse success(
            String eventUid,
            String operationType,
            String fileNumber) {
        return new InstallmentOperationResponse(
                eventUid,
                operationType,
                fileNumber,
                ResponseStatus.SUCCESS,
                null);
    }

    public static InstallmentOperationResponse failed(
            String eventUid,
            String operationType,
            String fileNumber,
            String errorMessage) {
        return new InstallmentOperationResponse(
                eventUid,
                operationType,
                fileNumber,
                ResponseStatus.FAILED,
                errorMessage);
    }
}
```

**Response rules:**
- Include correlation fields: `eventUid`, `operationType`, `fileNumber`
- Include `status` enum (SUCCESS/FAILED)
- Include `errorMessage` for failures (null for success)
- Provide static factory methods for clarity

## 7. Message Mapping

### 7.1 Message → Command Mapper

Use MapStruct to map message DTOs to command objects:

```java
@Mapper(componentModel = "spring")
public interface InstallmentPaymentMessageMapper {

    @Mapping(target = "loanFacilityId", source = "fileNumber", qualifiedByName = "resolveLoanFacilityId")
    @Mapping(target = "installmentScheduleId", source = "fileNumber", qualifiedByName = "resolveInstallmentScheduleId")
    @Mapping(target = "transactionNumber", source = "transactionNumber")
    @Mapping(target = "currency", source = "currency")
    @Mapping(target = "totalPrincipalAmount", source = "totalPrincipalAmount")
    @Mapping(target = "totalInterestAmount", source = "totalInterestAmount")
    @Mapping(target = "payments", source = "payments")
    RecordInstallmentPaymentCommand toCommand(InstallmentPaymentMessage message);

    @Named("resolveLoanFacilityId")
    default UUID resolveLoanFacilityId(String fileNumber) {
        // Repository lookup to resolve fileNumber → loanFacilityId
        // Implementation injected via @Mapper(uses = {...})
        throw new UnsupportedOperationException("Injected by MapStruct");
    }

    @Named("resolveInstallmentScheduleId")
    default UUID resolveInstallmentScheduleId(String fileNumber) {
        // Repository lookup to resolve fileNumber → installmentScheduleId
        throw new UnsupportedOperationException("Injected by MapStruct");
    }
}
```

**Mapping rules:**
- Use `@Mapper(componentModel = "spring")` for Spring injection
- Use `@Mapping` to map fields explicitly
- Use `@Named` methods for complex transformations
- Inject repositories via `@Mapper(uses = {MyRepository.class})`
- Handle legacy ID resolution (e.g., fileNumber → UUID)

## 8. Standards Compliance Checklist

### 8.1 Message Headers (Mandatory)

- [ ] `Idempotency-Key` (UUID v4)
- [ ] `X-Request-DateTime` (ISO 8601 UTC)
- [ ] `Accept-Language` (language code)
- [ ] `operationType` (for shared topics)
- [ ] `eventUid` (correlation ID)
- [ ] `traceparent` (W3C trace context, recommended)
- [ ] `tracestate` (W3C trace state, recommended)

### 8.2 Message Body

- [ ] `metadata` field is optional (use sparingly)
- [ ] Business fields use `camelCase`
- [ ] Dates in ISO 8601 format
- [ ] Money: separate amount + currency fields
- [ ] Enums use string values (not numeric codes)
- [ ] No sensitive data in metadata

### 8.3 Topic Naming

- [ ] Follows pattern: `corridor.{domain}.{service}.{operation}.{type}.queue.v{version}`
- [ ] Uses kebab-case for operation names
- [ ] Includes version suffix (e.g., `v1`)
- [ ] Type is `request`, `response`, or `event`

### 8.4 Request/Reply Pattern

- [ ] Request includes `responseTopic` field
- [ ] Response uses `eventUid` as Kafka key
- [ ] Response includes `status` field (SUCCESS/FAILED)
- [ ] Response includes correlation fields (eventUid, operationType)
- [ ] Error responses include `errorMessage`

### 8.5 Error Handling

- [ ] Error codes follow format: `{PREFIX}-{SEQUENCE}`
- [ ] Reserved codes (0001-0100) used correctly
- [ ] Custom codes (0101+) documented in service registry
- [ ] Error responses sent to `responseTopic`

## 9. Testing

### 9.1 Consumer Unit Test

```java
@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {"test.installment-operation.request.queue.v1"})
class InstallmentOperationCommandConsumerTest {

    @Autowired
    private KafkaTemplate<String, byte[]> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CommandProcessor processor;

    @Test
    void shouldConsumeInstallmentCollectionMessage() throws Exception {
        // Given
        InstallmentPaymentMessage message = InstallmentPaymentMessage.builder()
                .eventUid(UUID.randomUUID().toString())
                .operationType("INSTALLMENT_COLLECTION")
                .fileNumber("FILE-001")
                .transactionNumber("TXN-001")
                .currency("IRR")
                .totalPrincipalAmount(new BigDecimal("1000000"))
                .totalInterestAmount(new BigDecimal("50000"))
                .payments(List.of())
                .build();

        byte[] payload = objectMapper.writeValueAsBytes(message);

        ProducerRecord<String, byte[]> record = new ProducerRecord<>(
                "test.installment-operation.request.queue.v1",
                message.eventUid(),
                payload);

        record.headers()
                .add("eventUid", message.eventUid().getBytes(StandardCharsets.UTF_8))
                .add("operationType", "INSTALLMENT_COLLECTION".getBytes(StandardCharsets.UTF_8))
                .add("Idempotency-Key", UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8));

        // When
        kafkaTemplate.send(record).get();

        // Then
        await().atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> verify(processor, times(1)).process(any(RawCommandMessage.class)));
    }
}
```

### 9.2 Publisher Unit Test

```java
@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {"test.installment-operation.response.queue.v1"})
class InstallmentOperationResponsePublisherTest {

    @Autowired
    private InstallmentOperationResponsePublisher publisher;

    @Autowired
    private KafkaListenerEndpointRegistry registry;

    @KafkaListener(
            topics = "test.installment-operation.response.queue.v1",
            groupId = "test-group",
            containerFactory = "byteArrayKafkaListenerContainerFactory")
    public void listen(ConsumerRecord<String, byte[]> record) {
        receivedRecords.add(record);
    }

    private final List<ConsumerRecord<String, byte[]>> receivedRecords = new CopyOnWriteArrayList<>();

    @Test
    void shouldPublishSuccessResponse() throws Exception {
        // Given
        String eventUid = UUID.randomUUID().toString();
        InstallmentOperationResponse response = InstallmentOperationResponse.success(
                eventUid,
                "INSTALLMENT_COLLECTION",
                "FILE-001");

        // When
        publisher.sendResponse("test.installment-operation.response.queue.v1", response);

        // Then
        await().atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    assertThat(receivedRecords).hasSize(1);
                    ConsumerRecord<String, byte[]> record = receivedRecords.get(0);
                    assertThat(record.key()).isEqualTo(eventUid);

                    String eventUidHeader = new String(
                            record.headers().lastHeader("eventUid").value(),
                            StandardCharsets.UTF_8);
                    assertThat(eventUidHeader).isEqualTo(eventUid);

                    String statusHeader = new String(
                            record.headers().lastHeader("status").value(),
                            StandardCharsets.UTF_8);
                    assertThat(statusHeader).isEqualTo("SUCCESS");
                });
    }
}
```

## 10. Common Patterns

### 10.1 Idempotency Handling

**CRITICAL**: The platform `CommandProcessor` handles idempotency automatically using the `Idempotency-Key` header. Do NOT implement custom idempotency logic in consumers.

**How it works:**
1. Consumer extracts `Idempotency-Key` from Kafka headers
2. Wraps message in `RawCommandMessage` (includes headers)
3. Delegates to `CommandProcessor.process(rawMessage)`
4. Dispatcher checks idempotency cache using `Idempotency-Key`
5. If duplicate (key exists in cache), returns cached result
6. If new, executes command and caches result

**Rules:**
- Always include `Idempotency-Key` header in requests
- Use UUID v4 format
- Cache TTL specified by `Idempotency-Key-TTL` header (default: 60 seconds)
- Consumers MUST NOT validate/check idempotency themselves

### 10.2 Error Handling in Consumers

```java
private void handleOperation(ConsumerRecord<String, byte[]> record, String eventUid) {
    MyMessage message = null;
    try {
        // Step 1: Deserialize
        message = objectMapper.readValue(record.value(), MyMessage.class);

        // Step 2: Process via CommandProcessor
        RawCommandMessage rawMessage = RawCommandMessage.from(record);
        processor.process(rawMessage);

        // Step 3: Send success response (if request/reply)
        sendSuccessResponse(message);

    } catch (JsonProcessingException e) {
        // Deserialization error - malformed message
        LOG.error("Malformed message [eventUid={}]", eventUid, e);
        sendErrorResponse(record, eventUid, "PARSE_ERROR", null, "Malformed message: " + e.getMessage());
        // Do NOT rethrow - message is poison, skip it

    } catch (DomainException e) {
        // Business rule violation
        LOG.error("Business rule violation [eventUid={}]", eventUid, e);
        sendErrorResponse(record, eventUid, message.operationType(), message.fileNumber(), e.getMessage());
        // Do NOT rethrow - business error, send error response and acknowledge

    } catch (Exception e) {
        // Unexpected system error - rethrow to trigger retry
        LOG.error("System error [eventUid={}]", eventUid, e);
        sendErrorResponse(record, eventUid, message.operationType(), message.fileNumber(), "System error");
        throw new RuntimeException("Processing failed: " + eventUid, e);
    }
}
```

**Error handling rules:**
- **Deserialization errors**: Log and skip (poison message)
- **Business errors (DomainException)**: Send error response, acknowledge
- **System errors**: Send error response, rethrow for retry
- Always try to send error response before rethrowing

### 10.3 Legacy System Integration

When integrating with old systems (Java 8, no UUID support):

**Challenge:** Old system sends `fileNumber` (string), not UUIDs

**Solution:**
```java
@Mapper(componentModel = "spring", uses = {LoanFacilityRepository.class})
public interface LegacyMessageMapper {

    @Mapping(target = "loanFacilityId", source = "fileNumber", qualifiedByName = "resolveByFileNumber")
    MyCommand toCommand(LegacyMessage message);

    @Named("resolveByFileNumber")
    default UUID resolveByFileNumber(String fileNumber) {
        // Injected repository looks up loanFacilityId by fileNumber
        // MapStruct will inject the repository automatically
        return null; // Placeholder - implementation injected
    }
}

// Usage in mapper (MapStruct generates this):
public class LegacyMessageMapperImpl implements LegacyMessageMapper {
    @Autowired
    private LoanFacilityRepository repository;

    @Override
    public UUID resolveByFileNumber(String fileNumber) {
        return repository.findByFileNumber(fileNumber)
                .map(LoanFacility::getId)
                .orElseThrow(() -> new NotFoundException("Loan facility not found: " + fileNumber));
    }
}
```

### 10.4 Operation Type Discrimination

For shared topics with multiple operations:

```java
// 1. Extract operationType from header (preferred - cheap)
private String resolveOperationType(ConsumerRecord<String, byte[]> record) {
    String fromHeader = extractHeader(record, "operationType");
    if (fromHeader != null && !fromHeader.isEmpty()) {
        return fromHeader;
    }

    // 2. Fallback: parse from body (expensive - requires JSON parsing)
    try {
        JsonNode root = objectMapper.readTree(record.value());
        JsonNode opNode = root.get("operationType");
        if (opNode != null && !opNode.isNull()) {
            return opNode.asText();
        }
    } catch (Exception e) {
        LOG.warn("Failed to parse operationType from message body", e);
    }

    return "UNKNOWN";
}

// 3. Dispatch based on operationType
switch (operationType) {
    case "OPERATION_A" -> handleOperationA(...);
    case "OPERATION_B" -> handleOperationB(...);
    default -> {
        LOG.warn("Unknown operationType [{}]", operationType);
        sendErrorResponse(..., "Unknown operationType: " + operationType);
    }
}
```

## 11. Configuration

### 11.1 Kafka Consumer Configuration

```yaml
spring:
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
    consumer:
      group-id: core.loan.${spring.application.name}
      auto-offset-reset: earliest
      enable-auto-commit: false
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.apache.kafka.common.serialization.ByteArrayDeserializer
      properties:
        spring.json.trusted.packages: ir.dotin.loan.trade.adapters.driving.messaging.dto
        max.poll.records: 10
        max.poll.interval.ms: 300000

    listener:
      ack-mode: record
      concurrency: 3
```

### 11.2 Kafka Producer Configuration

```yaml
spring:
  kafka:
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.ByteArraySerializer
      acks: all
      retries: 3
      properties:
        enable.idempotence: true
        max.in.flight.requests.per.connection: 5
```

## 12. AsyncAPI Documentation (Springwolf)

All consumers MUST be documented with `@AsyncListener` annotations:

```java
@AsyncListener(
        operation =
                @AsyncOperation(
                        channelName = "corridor.core.loan.nova.full-lifecycle.request.queue.v1",
                        description = "Process nova loan full lifecycle commands",
                        headers =
                                @AsyncOperation.Headers(
                                        schemaName = "CommandHeaders",
                                        values = {
                                            @AsyncOperation.Headers.Header(
                                                    name = "Idempotency-Key",
                                                    description = "Unique identifier for idempotency (UUID v4)",
                                                    value = "UUID string"),
                                            @AsyncOperation.Headers.Header(
                                                    name = "X-Request-DateTime",
                                                    description = "Request timestamp (ISO 8601 UTC)",
                                                    value = "2025-08-22T14:30:00.123Z"),
                                            @AsyncOperation.Headers.Header(
                                                    name = "Accept-Language",
                                                    description = "Preferred language for error messages",
                                                    value = "fa | en-US"),
                                            @AsyncOperation.Headers.Header(
                                                    name = "traceparent",
                                                    description = "W3C trace context (distributed tracing)",
                                                    value = "00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01"),
                                            @AsyncOperation.Headers.Header(
                                                    name = "tracestate",
                                                    description = "W3C trace state (vendor-specific)",
                                                    value = "congo=t61rcWkgMzE")
                                        })))
public void consume(ConsumerRecord<String, byte[]> consumerRecord) {
    // Implementation
}
```

**Springwolf UI:** Accessible at `http://localhost:8080/springwolf/asyncapi-ui.html`

## 13. Key Implementation Rules

### 13.1 Mandatory Rules

1. **Standards Compliance**
   - Follow Inter-Service Communication Standards (SWA.101 v1.1)
   - Include all mandatory headers (Idempotency-Key, X-Request-DateTime, Accept-Language)
   - Use correct topic naming convention
   - Follow message body structure (metadata + business fields)

2. **Idempotency**
   - Always include `Idempotency-Key` header
   - Use UUID v4 format
   - Let platform handle idempotency (do NOT implement custom logic)

3. **Error Handling**
   - Send error responses for request/reply patterns
   - Use standard error code format (PREFIX-SEQUENCE)
   - Log all errors with context (eventUid, operationType)

4. **Request/Reply**
   - Extract `responseTopic` from message body
   - Use `eventUid` as Kafka message key for correlation
   - Include `status` field in response (SUCCESS/FAILED)
   - Send response for both success and failure cases

5. **Documentation**
   - Use `@AsyncListener` for all consumers
   - Document all headers in AsyncOperation
   - Provide meaningful descriptions

### 13.2 Best Practices

1. **Message Design**
   - Use records for immutability
   - Implement `CommandPayload` marker interface
   - Use `@Nullable` for optional fields
   - Avoid primitive types (use wrapper classes)

2. **Consumer Implementation**
   - Delegate to `CommandProcessor` for dispatching
   - Extract headers using helper methods
   - Log all operations with correlation IDs
   - Handle errors gracefully (send error responses)

3. **Publisher Implementation**
   - Use `eventUid` as Kafka key
   - Add all relevant headers
   - Send asynchronously with CompletableFuture
   - Log success/failure for observability

4. **Testing**
   - Use `@EmbeddedKafka` for integration tests
   - Test both success and error paths
   - Verify headers are correctly set
   - Use Awaitility for async assertions

5. **Legacy Integration**
   - Use MapStruct for message mapping
   - Resolve legacy IDs (fileNumber → UUID) in mappers
   - Inject repositories via `@Mapper(uses = {...})`

## 14. Migration from Old System

### 14.1 Message Format Compatibility

**Old system limitations:**
- Java 8 (no modern Java features)
- No `@JsonTypeInfo` support (can't use polymorphic JSON)
- Uses `fileNumber` instead of UUIDs
- Uses `Date` instead of `Instant`

**Solution:**
- Use `operationType` field for type discrimination
- Support both header and body-based `operationType` resolution
- Map `fileNumber` to UUID via repository lookup
- Convert `Date` to `Instant` in mappers

### 14.2 Backward Compatibility

When adding new fields to messages:
- Always make new fields `@Nullable`
- Use default values in mappers for missing fields
- Document field addition in changelog
- Coordinate with old system team for rollout

## 15. Troubleshooting

### 15.1 Consumer Not Receiving Messages

**Check:**
1. Topic name matches exactly (case-sensitive)
2. Consumer group ID is unique per service instance
3. Kafka bootstrap servers configuration
4. Network connectivity to Kafka cluster
5. Consumer logs for errors

### 15.2 Idempotency Not Working

**Check:**
1. `Idempotency-Key` header is present
2. Header format is UUID v4
3. Cache TTL is reasonable (60+ seconds)
4. Dispatcher is configured correctly

### 15.3 Response Not Received (Request/Reply)

**Check:**
1. `responseTopic` field in request message
2. Publisher is sending to correct topic
3. Response topic consumer is running
4. `eventUid` matches in request/response
5. Publisher logs for send errors

### 15.4 Message Deserialization Errors

**Check:**
1. Message DTO matches wire format exactly
2. Field names use camelCase
3. All required fields are present
4. Date/time formats are ISO 8601
5. ObjectMapper configuration (trusted packages)

## 16. Related Documentation

- **REST Adapter**: `/workspace/trade-loan/adapters/driving/rest/CLAUDE.md`
- **Persistence Adapter**: `/workspace/trade-loan/adapters/driven/persistence/CLAUDE.md`
- **Trade Loan Project**: `/workspace/trade-loan/CLAUDE.md`
- **Workspace Overview**: `/workspace/CLAUDE.md`
- **Inter-Service Communication Standards**: `/workspace/SWA.101-Inter_Service_Communication_Standards-v1.1.pdf`
