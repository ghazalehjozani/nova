# AsyncAPI Documentation

This directory contains AsyncAPI 3.0 specifications for the trade-loan microservice asynchronous messaging and event-driven architecture.

## AsyncAPI Standards

This project follows these AsyncAPI standards:

- **Version**: AsyncAPI 3.0.x
- **Format**: YAML for human readability
- **Language**: Persian for descriptions and documentation
- **Protocol**: Apache Kafka for event streaming
- **Serialization**: JSON with Avro schema evolution

## Files Structure

```
asyncapi/
├── asyncapi.yaml                 # Main AsyncAPI specification
├── channels/                     # Channel definitions
│   ├── loan-events.yaml          # Loan lifecycle events
│   ├── payment-events.yaml       # Payment processing events
│   ├── audit-events.yaml         # Audit and compliance events
│   └── notification-events.yaml  # User notification events
├── messages/                     # Message schemas
│   ├── commands.yaml             # Command messages
│   ├── events.yaml               # Event messages
│   ├── queries.yaml              # Query messages
│   └── responses.yaml            # Response messages
├── schemas/                      # JSON/Avro schemas
│   ├── loan-schemas.json         # Loan-related schemas
│   ├── payment-schemas.json      # Payment-related schemas
│   └── common-schemas.json       # Shared schemas
├── examples/                     # Message examples
│   ├── commands/                 # Command examples
│   ├── events/                   # Event examples
│   └── integration/              # Integration test examples
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