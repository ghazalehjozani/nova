# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Repository Overview

This is a multi-module Java microservices repository implementing a loan management system using Domain-Driven Design (DDD) and Hexagonal Architecture. The repository contains two main services:

1. **base-loan**: Shared kernel containing common domain models and business logic for all loan types
2. **trade-loan**: Specialized service implementing Morabehe (Trade) loans that extends base-loan functionality

## Build & Development Commands

### Maven Commands
```bash
# Build and run unit tests (exclude integration tests)
mvn clean verify -P!dev -DskipITs=true

# Build with all tests (including integration tests)
mvn clean verify

# Run architecture tests only (enabled by default in CI)
mvn test -Dtest=**/*ArchitectureTest*

# Run specific module tests
cd trade-loan && mvn clean test
cd base-loan && mvn clean test

# Run with SpotBugs static analysis (optional in CI)
mvn clean verify -Pspotbugs

# Run OWASP security scanning (optional in CI)
mvn clean verify -Psecurity
```

### Development Setup
```bash
# Configure project hooks (commit-msg, pre-commit, pre-push)
./configure-project-hooks.sh

# Build all modules
mvn clean install

# Run tests for trade-loan service
cd trade-loan && mvn clean test
```

## Architecture & Project Structure

### Hexagonal Architecture Pattern
The project follows strict hexagonal (ports & adapters) architecture:

- **Core Layer**: Domain entities, value objects, and business logic
- **Application Layer**: Use cases, application services, and ports (interfaces)
- **Adapter Layer**: Implementation of ports (REST controllers, persistence, messaging)
- **Container Layer**: Spring Boot application entry point

### Base Loan Module Structure
```
base-loan/
├── domain/                      # Domain layer (DDD entities, VOs, aggregates)
│   ├── loanarrangement/         # Loan facility and arrangement domain
│   ├── loantype/                # Loan type definitions and rules
│   └── shared/                  # Shared domain components
├── architecture-tests/          # ArchUnit tests enforcing domain rules
└── pom.xml
```

### Trade Loan Module Structure
```
trade-loan/
├── core/
│   ├── domain/                  # Trade-specific domain models
│   └── application/
│       ├── ports/               # Application interfaces
│       │   ├── driving/         # Inbound interfaces (REST, messaging)
│       │   └── driven/          # Outbound interfaces (persistence, clients)
│       └── service/             # Application services (use cases)
├── adapters/
│   ├── driving/                 # Inbound adapters
│   │   ├── rest/               # REST controllers
│   │   └── messaging/          # Message consumers
│   └── driven/                  # Outbound adapters
│       ├── persistence/         # JPA repositories
│       ├── client/             # External service clients
│       └── messaging/          # Message producers
├── container/                   # Spring Boot main application
└── architecture-tests/          # Architecture compliance tests
```

## Key Domain Concepts

### Core Domain Entities (from base-loan)
- **BaseLoanFacility**: Loan request/facility with customer details, amount, duration
- **BaseLoanType**: Configurable loan product type with parameters and rules
- **BaseLoanRule**: Immutable loan rules covering interest, penalties, repayment policies
- **BaseLoanArrangement**: Specific loan agreement instance

### Value Objects & Policies
- **InterestPolicy**: Base/preferred interest rates and formulas
- **PenaltyPolicy**: Penalty rates and payment types
- **RepaymentPriorityPolicy**: Repayment order (principal, interest, penalty)
- **RegulatoryCompliancePolicy**: Overdue classification rules
- **Money**: Immutable monetary value with currency handling

### Trade Loan Extensions
- **MorabeheLoanType**: Trade-specific loan type with merchandise document flags
- **Trade-specific domain logic**: Extends base-loan with trade business rules

## Important Development Guidelines

### Code Quality & Architecture Compliance
- **Architecture Tests**: Use ArchUnit to enforce hexagonal architecture rules
- **Checkstyle**: Code style enforcement (checkstyle.xml configuration in both modules)
- **No cross-layer dependencies**: Domain layer must not depend on application or adapter layers
- **Immutable Value Objects**: All domain VOs should be immutable
- **Factory Pattern**: Use factories for complex domain object creation (DocumentFactory, ArticleSpecFactory)

### Domain-Driven Design Principles
- **Aggregate Roots**: Protect domain invariants through aggregate roots
- **Domain Events**: Use for communication between bounded contexts
- **Repository Pattern**: Abstract persistence behind domain interfaces
- **Strategy Pattern**: For configurable business rules (DocumentCalculationStrategy)

### Testing Approach
- **Unit Tests**: Test domain logic in isolation
- **Architecture Tests**: Enforce structural rules using ArchUnit
- **Integration Tests**: Test adapter implementations (optional via CI parameter)
- **Test Coverage**: Maintain high coverage for domain logic

### Document & Transaction Flow
The system uses a sophisticated document and transaction creation pattern:
1. **Application Service**: Orchestrates transaction creation
2. **DocumentFactory**: Manages document and transaction lifecycle
3. **DocumentCalculationStrategy**: Implements business rules for article generation
4. **ArticleSpecFactory**: Creates article specifications
5. **Value Objects**: Ensure data integrity (Document, Article, LoanTransaction)

## Configuration Files
- **Jenkinsfile**: CI/CD pipeline with quality gates and deployment options
- **checkstyle.xml**: Code style and quality rules
- **checkstyle-suppressions.xml**: Checkstyle rule exceptions
- **configure-project-hooks.sh**: Git hooks setup for commit validation

## Dependencies & Platform
- **Parent**: ir.dotin.platform:platform-parent (1.0.0-SNAPSHOT)
- **Framework**: Spring Boot with Java
- **Build**: Maven with multi-module structure
- **Testing**: JUnit 5, AssertJ, Mockito
- **Architecture**: ArchUnit for structural testing
- **Documentation**: ADR (Architecture Decision Records) in doc/adr/

## Key External Dependencies
- **ir.dotin.platform**: Platform-specific libraries (commons, dispatcher-api)
- **Spring Ecosystem**: Web, Data JPA, Security, Validation
- **MapStruct**: For object mapping
- **SpringDoc**: OpenAPI documentation
- **Spring Kafka**: For messaging adapters