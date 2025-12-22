# Trade Loan AsyncAPI Documentation

## Overview

This document contains the AsyncAPI 3.0 specification for the Trade Loan Service's asynchronous messaging and event-driven architecture. The specification was extracted from the running service using SpringWolf.

## Service Details

- **Service Name**: trade-loan-service
- **Version**: 1.0.0
- **AsyncAPI Version**: 3.0.0
- **Message Protocol**: Apache Kafka
- **Default Content Type**: application/json
- **Server**: localhost:9092

## Files

- `asyncapi.yaml` - Complete AsyncAPI 3.0 specification with all channels and messages
- `README.md` - This file

## AsyncAPI Standards

This project follows these AsyncAPI standards:

- **Version**: AsyncAPI 3.0.x
- **Format**: YAML for human readability
- **Language**: Persian for descriptions and documentation
- **Protocol**: Apache Kafka for event streaming
- **Serialization**: JSON with Avro schema evolution

## Accessing the AsyncAPI Documentation

### SpringWolf UI
- **AsyncAPI UI**: `http://localhost:8080/springwolf/docs.html`
- **AsyncAPI YAML**: `http://localhost:8080/springwolf/docs.yaml`
- **AsyncAPI JSON**: `http://localhost:8080/springwolf/docs.json`

### Kafka Server Configuration
- **Bootstrap Servers**: localhost:9092
- **Protocol**: kafka
- **Binding Version**: 0.5.0

### Message Channels

The service defines Kafka channels for:

1. **Full Lifecycle Processing** - `corridor.core.loan.nova.full-lifecycle.request.queue.v1`
2. **Customer Integration** - `corridor.core.loan.customer.integration.request.queue.v1`
3. **Disbursement Operations** - Various disbursement request queues
4. **Response Channels** - Corresponding response queues for each operation

### Message Headers

All messages include standardized headers:
- `Accept-Language`: Preferred language (string)
- `Authorization`: Bearer token authentication
- `X-Correlation-ID`: Request correlation identifier
- `X-Request-DateTime`: Request timestamp (UTC, ISO 8601)
- `Idempotency-Key`: UUID for idempotent processing

## Current Files Structure

```
asyncapi/
├── asyncapi.yaml                 # Main AsyncAPI 3.0 specification (extracted)
└── README.md                     # This file
```

## Event-Driven Architecture

### Event Types

1. **Domain Events (رویدادهای دامنه)**
   - LoanFacilityCreated
   - LoanArrangementApproved
   - PaymentProcessed
   - LoanMatured

2. **Integration Events (رویدادهای یکپارچه‌سازی)**
   - CustomerVerified
   - RiskAssessmentCompleted
   - RegulatoryCheckPassed

3. **System Events (رویدادهای سیستمی)**
   - ServiceHealthStatus
   - PerformanceMetrics
   - ErrorOccurred

### Kafka Topics

#### Command Topics
- `trade-loan.commands.create-facility`
- `trade-loan.commands.approve-arrangement`
- `trade-loan.commands.process-payment`

#### Event Topics
- `trade-loan.events.facility.created`
- `trade-loan.events.arrangement.approved`
- `trade-loan.events.payment.processed`

#### Query Topics
- `trade-loan.queries.facility-details`
- `trade-loan.queries.payment-history`

## Message Design Patterns

### Command Messages
```json
{
  "commandId": "uuid",
  "commandType": "CreateLoanFacility",
  "aggregateId": "facility-uuid",
  "payload": {},
  "metadata": {
    "timestamp": "2025-12-11T10:00:00Z",
    "correlationId": "uuid",
    "causationId": "uuid",
    "userId": "user-uuid"
  }
}
```

### Event Messages
```json
{
  "eventId": "uuid",
  "eventType": "LoanFacilityCreated",
  "aggregateId": "facility-uuid",
  "aggregateType": "LoanFacility",
  "version": 1,
  "payload": {},
  "metadata": {
    "timestamp": "2025-12-11T10:00:00Z",
    "correlationId": "uuid",
    "userId": "user-uuid"
  }
}
```

## Schema Evolution

### Avro Schema Strategy
- **Backward Compatibility**: New fields must have defaults
- **Forward Compatibility**: Removing fields requires alias
- **Breaking Changes**: Requires new topic version
- **Schema Registry**: Centralized schema management

### Versioning Strategy
- **Topic Versioning**: Use topic suffixes for breaking changes
- **Schema Versioning**: Include version in schema namespace
- **Compatibility**: Maintain backward compatibility for consumers

## Quality Attributes

### Reliability
- **At-least-once Delivery**: Guaranteed message delivery
- **Idempotent Processing**: Safe message reprocessing
- **Dead Letter Queues**: Handle poison messages
- **Message Ordering**: Partition key for ordering

### Performance
- **Batching**: Process messages in batches
- **Compression**: Use LZ4 compression for large messages
- **Partitioning**: Optimize partition strategy
- **Consumer Groups**: Scale consumer processing

### Security
- **TLS Encryption**: Secure communication
- **SASL Authentication**: Kafka security
- **Schema Validation**: Message format validation
- **Access Control**: Topic-level permissions

## Integration Patterns

### Saga Pattern
```yaml
channels:
  trade-loan.saga.create-facility:
    subscribe:
      message:
        $ref: '#/messages/CreateFacilityCommand'
    publish:
      message:
        $ref: '#/messages/FacilityCreatedEvent'
```

### Event Sourcing
```yaml
channels:
  trade-loan.events.loan-snapshot:
    publish:
      message:
        $ref: '#/messages/LoanSnapshotEvent'
```

### CQRS
```yaml
channels:
  trade-loan.queries.read-model:
    subscribe:
      message:
        $ref: '#/messages/ReadModelQuery'
    publish:
      message:
        $ref: '#/messages/ReadModelResponse'
```

## Testing Strategy

### Contract Testing
- **Pact**: Consumer-driven contract testing
- **Schema Registry**: Validate schema compatibility
- **Message Examples**: Test with real message examples

### Integration Testing
- **Testcontainers**: Embedded Kafka for testing
- **Spring Kafka Test**: Test framework for Kafka
- **Mock Consumers**: Simulate consumer behavior

## Monitoring and Observability

### Metrics
- **Message Throughput**: Messages per second
- **Consumer Lag**: Consumer processing delay
- **Error Rates**: Failed message processing
- **Topic Size**: Topic storage metrics

### Logging
- **Structured Logging**: JSON format for logs
- **Correlation IDs**: Trace message flow
- **Message Metadata**: Log important message fields

### Tracing
- **OpenTelemetry**: Distributed tracing
- **Kafka Headers**: Propagate trace context
- **Span Events**: Log important processing steps

## Tools and Frameworks

### Schema Management
- **Confluent Schema Registry**: Central schema management
- **Avro**: Schema serialization format
- **Kafka Connect**: Database integration

### Development Tools
- **AsyncAPI Generator**: Code generation from spec
- **Kafkacat**: Command-line Kafka client
- **Kafka UI**: Web-based Kafka interface

### Monitoring Tools
- **Prometheus**: Metrics collection
- **Grafana**: Metrics visualization
- **Jaeger**: Distributed tracing
- **ELK Stack**: Log aggregation

## Best Practices

1. **Design for Failure**: Assume messages can fail and retry
2. **Keep Messages Small**: Optimize for network efficiency
3. **Use Meaningful Names**: Clear, descriptive message and topic names
4. **Document Everything**: Maintain comprehensive AsyncAPI documentation
5. **Version Carefully**: Plan for schema evolution from the start

## Regeneration

To regenerate this specification:

```bash
# Download the AsyncAPI specification
curl -s http://localhost:8080/springwolf/docs.yaml \
  -o documents/asyncapi/asyncapi.yaml

# Or in JSON format
curl -s http://localhost:8080/springwolf/docs.json \
  -o documents/asyncapi/asyncapi.json
```

## Validation

```bash
# Validate AsyncAPI specification
npx -y @asyncapi/cli validate documents/asyncapi/asyncapi.yaml

# Generate HTML documentation
npx -y @asyncapi/cli generate from-file documents/asyncapi/asyncapi.yaml \
  --param htmlTemplate=single-page \
  -o documents/asyncapi/index.html
```