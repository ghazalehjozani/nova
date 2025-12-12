# Domain Context Mapping (CMap)

This directory contains Domain-Driven Design (DDD) context maps for the trade-loan microservice and its integration with other bounded contexts.

## Context Mapping Types

This project uses the following DDD context mapping patterns:

- **Customer/Supplier (مشتری/تأمین‌کننده)**: One-way dependency relationships
- **Conformist (همسو)**: Follower conforms to leader's model
- **Anti-Corruption Layer (لایه ضد فساد)**: Protects domain from external model
- **Open Host Service (سervices میزبان باز)**: Provides protocol for integration
- **Published Language (زبان منتشر شده)**: Shared canonical model
- **Partnership (شراکت)**: Two-way relationship between contexts
- **Separate Ways (مسیرهای مجزا)**: No integration between contexts

## Files Structure

```
cmap/
├── overall-context-map.puml      # High-level context map
├── trade-loan-context.puml       # Trade loan bounded context details
├── integration-patterns.puml     # Integration patterns between contexts
├── relationships.md              # Detailed relationship documentation
└── README.md                     # This file
```

## Key Bounded Contexts

### Core Contexts

1. **Trade Loan Context (بخش وام تجاری)**
   - Manages Morabehe (trade) loans
   - Extends base loan functionality
   - Contains trade-specific business rules

2. **Base Loan Context (بخش وام پایه)**
   - Shared kernel for common loan functionality
   - Core domain models and business logic
   - Reused across all loan types

3. **Platform Framework Context (بخش چارچوب پلتفرم)**
   - Infrastructure and cross-cutting concerns
   - CQRS implementation
   - Common utilities and patterns

### External Contexts

4. **Customer Management Context (بخش مدیریت مشتریان)**
   - Customer information and verification
   - Credit scoring and risk assessment

5. **Payment Processing Context (بخش پردازش پرداخت)**
   - Payment transactions and settlements
   - Integration with banking systems

6. **Regulatory Compliance Context (بخش انطباق با مقررات)**
   - Legal and regulatory requirements
   - Audit and reporting

## Context Map Guidelines

1. **Use Persian** for all context names and descriptions
2. **Show integration types** clearly with appropriate symbols
3. **Include data flows** between contexts
4. **Highlight anti-corruption layers** where external systems integrate
5. **Document team relationships** and ownership

## Integration Patterns

### Synchronous Integration
- REST APIs with OpenAPI contracts
- gRPC for internal microservice communication
- GraphQL for flexible data fetching

### Asynchronous Integration
- Kafka events for eventual consistency
- Message queues for reliable delivery
- Event sourcing for audit trails

## Tools

- **PlantUML**: For context map diagrams
- **Mermaid**: For web-based visualization
- **draw.io**: For final diagram editing
- **Confluence**: For documentation and sharing

## Maintenance

Context maps should be updated when:
- New bounded contexts are identified
- Integration patterns change
- Team responsibilities shift
- Architecture evolves

Review context maps quarterly to ensure they reflect the current state of the system.