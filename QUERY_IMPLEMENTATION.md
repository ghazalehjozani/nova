# Complete Query Implementation for Trade Loan System

## Overview

This implementation provides a comprehensive CQRS-based query system for the trade loan microservice. The system follows clean architecture principles with complete separation between read and write operations.

## Architecture

### CQRS Pattern Implementation

```
Controller → QueryHandler → QueryPort → QueryAdapter → JPA Entity → QueryModel → DTO
↑                           ↓
(app layer)                  (persistence adapter)
Mapper: Entity→QueryModel     Mapper: QueryModel→DTO
```

### Key Architectural Principles

1. **No Domain Entities in Queries**: Port interfaces return QueryModels, not domain entities
2. **Reuse Existing JPA Entities**: Adapter maps existing entities to QueryModels
3. **Clean Layer Separation**:
   - Adapter: Entity → QueryModel
   - Handler: QueryModel → DTO
4. **CQRS Compliance**: Separate read/write models and operations
5. **Performance Optimized**: QueryModels are denormalized and read-optimized

## Implementation Structure

### 1. Query Models (Application Layer)
**Location**: `core/application/query/loanfacility/model/`

- `FacilityQueryModel.java` - Optimized read model with flattened customer data
- `FacilitySearchQueryModel.java` - Paginated search results wrapper
- `OutstandingBalanceQueryModel.java` - Calculated balance information

### 2. Query Repository Port
**Location**: `core/application/ports/driven/query/loanfacility/`

- `FacilityQueryRepository.java` - Port interface returning QueryModels
- `PageRequest.java` - Application layer pagination model
- `FacilitySearchCriteria.java` - Search parameters
- `FacilityStatistics.java` - Statistics model

### 3. Query Handlers (Application Layer)
**Location**: `core/application/query/loanfacility/`

- `GetFacilityByIdQueryHandler.java` - Single facility retrieval
- `SearchFacilitiesByCriteriaQueryHandler.java` - Search with pagination
- `CalculateFacilityOutstandingQueryHandler.java` - Balance calculations

### 4. Query Adapter Implementation (Persistence Layer)
**Location**: `adapters/driven/persistence/loanfacility/query/`

- `JpaFacilityQueryAdapter.java` - Port implementation using existing entities
- `SpringDataFacilityQueryRepository.java` - Extended JPA repository
- `FacilityQueryModelMapper.java` - Entity → QueryModel mapping

### 5. Query Specifications
**Location**: `adapters/driven/persistence/loanfacility/query/specification/`

- `FacilitySpecification.java` - Dynamic query criteria builder

### 6. Query Model to DTO Mappers
**Location**: `core/application/query/loanfacility/mapper/`

- `FacilityDTOMapper.java` - QueryModel → API DTO mapping
- `OutstandingBalanceDTOMapper.java` - Balance model mapping

### 7. DTOs and Query Objects
**Location**: `core/application/query/loanfacility/dto/`

- `TradeFacilityDTO.java` - External API representation
- `OutstandingBalanceDTO.java` - Balance API response
- `PaginatedResult.java` - Generic pagination wrapper

### 8. REST API Layer
**Location**: `adapters/driving/rest/src/main/java/ir/dotin/loan/trade/adapters/driving/web/`

- `FacilityQueryController.java` - REST endpoints for queries
- `FacilitySearchRequestDTO.java` - Advanced search request DTO

## Usage Examples

### 1. Get Facility by ID
```java
// Query
GetFacilityByIdQuery query = new GetFacilityByIdQuery(facilityId);

// Handler execution
Result<TradeFacilityDTO> result = queryHandler.handle(query);

// REST API
GET /api/v1/facilities/{facilityId}
```

### 2. Search Facilities with Criteria
```java
// Build criteria
FacilitySearchCriteria criteria = FacilitySearchCriteria.builder()
    .customerNationalId("1234567890")
    .status(List.of("ACTIVE", "DISBURSED"))
    .branchCode("BR001")
    .build();

// Query
SearchFacilitiesByCriteriaQuery query = new SearchFacilitiesByCriteriaQuery(
    criteria, PageRequest.of(0, 20)
);

// Handler execution
Result<PaginatedResult<TradeFacilityDTO>> result = queryHandler.handle(query);

// REST API
GET /api/v1/facilities/search?customerNationalId=1234567890&status=ACTIVE
```

### 3. Calculate Outstanding Balance
```java
// Query
CalculateFacilityOutstandingQuery query = new CalculateFacilityOutstandingQuery(
    facilityId, LocalDate.now()
);

// Handler execution
Result<OutstandingBalanceDTO> result = queryHandler.handle(query);

// REST API
GET /api/v1/facilities/{facilityId}/outstanding?asOfDate=2024-01-01
```

## Key Features

### 1. Advanced Search Capabilities
- Multi-criteria filtering (customer, status, amounts, dates, etc.)
- Dynamic query building using JPA Specifications
- Full-text search on customer names
- Range queries for amounts and dates

### 2. Pagination Support
- Application-layer pagination (independent of Spring Data)
- Configurable page size and sorting
- Complete pagination metadata in responses

### 3. Performance Optimizations
- Reuses existing JPA entities (no duplication)
- Lazy loading with proper fetch strategies
- Efficient calculations using aggregation queries
- Support for batch operations

### 4. Balance Calculations
- Real-time outstanding balance calculations
- Principal, interest, and penalty breakdowns
- Overdue status and risk level assessment
- Payment progress tracking

### 5. Statistics and Reporting
- Facility statistics by branch and status
- Aggregate calculations (total amounts, counts)
- Support for custom reporting queries

## Integration Points

### 1. Existing Entity Integration
The implementation reuses existing entities:
- `TradeLoanFacilityEntity` - Main facility entity
- `TradeLoanApplicationEntity` - Application data
- `TradeSanctionedLoanEntity` - Approval data
- `InstallmentEntity` - Installment data

### 2. JPA Repository Extensions
Extended existing repositories with query-specific methods:
- Complex JOIN queries with eager fetching
- Aggregation queries for statistics
- Dynamic criteria queries

### 3. Spring Integration
- Spring Data JPA for repository operations
- MapStruct for object mapping
- Spring MVC for REST endpoints
- Transaction management with `@Transactional(readOnly = true)`

## Testing Considerations

### 1. Unit Tests
- Query Handler tests with mocked repositories
- Mapper tests for object transformations
- Specification tests for query building

### 2. Integration Tests
- Repository integration with test database
- End-to-end query flow testing
- Performance testing for large datasets

### 3. API Tests
- REST endpoint testing
- Request validation testing
- Response format verification

## Configuration Requirements

### 1. Spring Configuration
```java
@Configuration
public class QueryConfig {

    @Bean
    public FacilityDTOMapper facilityDTOMapper() {
        return Mappers.getMapper(FacilityDTOMapper.class);
    }

    @Bean
    public FacilityQueryModelMapper facilityQueryModelMapper() {
        return Mappers.getMapper(FacilityQueryModelMapper.class);
    }
}
```

### 2. Dependencies
- Spring Data JPA
- MapStruct for mapping
- Spring Boot Web for REST API
- Lombok for boilerplate reduction

## Future Enhancements

### 1. Caching
- Redis caching for frequently accessed facilities
- Cache invalidation strategies
- Query result caching

### 2. Performance
- Database indexing strategy
- Query optimization
- Batch processing improvements

### 3. Monitoring
- Query performance metrics
- Slow query detection
- Usage analytics

### 4. Security
- Row-level security
- Data filtering based on user permissions
- Audit logging for queries

## Conclusion

This implementation provides a robust, scalable, and maintainable query system that follows enterprise-grade patterns. The CQRS separation ensures clean architecture while the reuse of existing entities maintains consistency with the existing codebase.

The system is designed to handle complex business queries while maintaining good performance characteristics and providing a clean API for client applications.