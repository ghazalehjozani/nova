# Updated CLAUDE.md - Query Persistence Adapter Section

## Query Implementation in Persistence Layer

### Query Pattern Architecture

The system implements CQRS with Query Models defined in the application layer. **Query adapters reuse existing JPA
entities and map them to Query Models.**

#### Query Flow
```
Controller → QueryHandler → QueryPort → QueryAdapter → JPA Entity → QueryModel → DTO
↑                           ↓
(app layer)                  (persistence adapter)
Mapper: Entity→QueryModel
```

### Query Model Location

Query Models are defined in the **application layer**, not persistence:

```java
// Location: core/application/query/loanfacility/model/
package ir.dotin.loan.trade.core.application.query.loanfacility.model;

public class FacilityQueryModel {
    private UUID id;
    private String facilityCode;
    private BigDecimal requestedAmount;
    private String currency;
    private String facilityStatus;
    // Flattened customer data
    private UUID customerId;
    private String customerName;
    private String customerNationalId;
    // Flattened arrangement data
    private UUID arrangementId;
    private String arrangementCode;
    // Audit fields
    private Instant createdAt;
    private String createdBy;
    // Getters, setters, builder
}
```

### Query Repository Port

Ports return Query Models from application layer:

```java
// Location: core/application/ports/driven/query/loanfacility/
package ir.dotin.loan.trade.core.application.ports.inbound.query.loanfacility;

public interface FacilityQueryRepository {
    Optional<FacilityQueryModel> findById(UUID facilityId);
    
    FacilitySearchQueryModel searchByCriteria(
        FacilitySearchCriteria criteria,
        PageRequest pageRequest
    );
    
    Optional<OutstandingBalanceQueryModel> calculateOutstanding(
        UUID facilityId,
        LocalDate asOfDate
    );
}
```

### Query Adapter Implementation

#### Structure
```
adapters/driven/persistence/loanfacility/query/
├── JpaFacilityQueryAdapter.java              # Implements QueryPort
├── SpringDataFacilityQueryRepository.java    # Spring Data JPA
├── mapper/
│   └── FacilityQueryModelMapper.java         # Entity → QueryModel (app layer)
└── specification/
    └── FacilitySpecification.java
```

#### Query Adapter Pattern

Adapter **reuses existing entities** and maps to application Query Models:

```java
// Location: adapters/driven/persistence/loanfacility/query/
package ir.dotin.loan.trade.adapters.driven.persistence.loanfacility.query;

@Repository
@Transactional(readOnly = true)
public class JpaFacilityQueryAdapter implements FacilityQueryRepository {
    
    // Reuse existing JPA repository
    private final TradeLoanFacilityJpaRepository jpaRepository;
    private final InstallmentJpaRepository installmentRepository;
    
    // Mapper: Entity → QueryModel (from application layer)
    private final FacilityQueryModelMapper queryModelMapper;
    
    @Override
    public Optional<FacilityQueryModel> findById(UUID facilityId) {
        return jpaRepository.findById(facilityId)
            .map(queryModelMapper::toQueryModel);  // Entity → QueryModel
    }
    
    @Override
    public FacilitySearchQueryModel searchByCriteria(
        FacilitySearchCriteria criteria,
        PageRequest pageRequest
    ) {
        Specification<TradeLoanFacilityEntity> spec = 
            FacilitySpecification.fromCriteria(criteria);
        
        Pageable pageable = toSpringPageable(pageRequest);
        Page<TradeLoanFacilityEntity> page = jpaRepository.findAll(spec, pageable);
        
        // Map existing entities to query models
        List<FacilityQueryModel> models = page.getContent()
            .stream()
            .map(queryModelMapper::toQueryModel)
            .toList();
        
        return new FacilitySearchQueryModel(
            models,
            page.getTotalElements(),
            page.getTotalPages(),
            page.getNumber()
        );
    }
    
    @Override
    public Optional<OutstandingBalanceQueryModel> calculateOutstanding(
        UUID facilityId,
        LocalDate asOfDate
    ) {
        Optional<TradeLoanFacilityEntity> facilityOpt = 
            jpaRepository.findById(facilityId);
        
        if (facilityOpt.isEmpty()) return Optional.empty();
        
        TradeLoanFacilityEntity facility = facilityOpt.get();
        
        // Use existing installment repository
        List<InstallmentEntity> installments = 
            installmentRepository.findByFacilityIdAndDueDateBefore(
                facilityId, asOfDate
            );
        
        // Calculate using entities
        BigDecimal principal = installments.stream()
            .filter(i -> !"PAID".equals(i.getStatus()))
            .map(InstallmentEntity::getPrincipalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal interest = installments.stream()
            .filter(i -> !"PAID".equals(i.getStatus()))
            .map(InstallmentEntity::getInterestAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Build query model
        OutstandingBalanceQueryModel model = new OutstandingBalanceQueryModel();
        model.setFacilityId(facilityId);
        model.setPrincipalOutstanding(principal);
        model.setInterestOutstanding(interest);
        model.setTotalOutstanding(principal.add(interest));
        model.setCurrency(facility.getCurrency());
        model.setCalculationDate(asOfDate);
        
        return Optional.of(model);
    }
    
    private Pageable toSpringPageable(PageRequest pageRequest) {
        Sort sort = Sort.unsorted();
        if (pageRequest.sortBy() != null) {
            sort = "DESC".equalsIgnoreCase(pageRequest.direction()) 
                ? Sort.by(pageRequest.sortBy()).descending()
                : Sort.by(pageRequest.sortBy()).ascending();
        }
        return org.springframework.data.domain.PageRequest.of(
            pageRequest.page(), 
            pageRequest.size(), 
            sort
        );
    }
}
```

### Mapping Strategy

#### Entity → QueryModel Mapper (in persistence adapter)

Maps existing JPA entities to application Query Models:

```java
// Location: adapters/driven/persistence/loanfacility/query/mapper/
package ir.dotin.loan.trade.adapters.driven.persistence.loanfacility.query.mapper;

@Mapper(componentModel = "spring")
public interface FacilityQueryModelMapper {
    
    // Map entity to QueryModel (from application layer)
    FacilityQueryModel toQueryModel(TradeLoanFacilityEntity entity);
    
    List<FacilityQueryModel> toQueryModels(List<TradeLoanFacilityEntity> entities);
}
```

#### QueryModel → DTO Mapper (in application layer)

```java
// Location: core/application/query/loanfacility/mapper/
package ir.dotin.loan.trade.core.application.query.loanfacility.mapper;

@Mapper(componentModel = "spring")
public interface FacilityDTOMapper {
    
    @Mapping(target = "customer", source = ".")
    @Mapping(target = "requestedAmount", source = ".")
    @Mapping(target = "arrangement", source = ".")
    TradeFacilityDTO toDTO(FacilityQueryModel model);
    
    default CustomerDTO toCustomerDTO(FacilityQueryModel model) {
        return new CustomerDTO(
            model.getCustomerId(),
            model.getCustomerName(),
            model.getCustomerNationalId()
        );
    }
    
    default MoneyDTO toMoneyDTO(FacilityQueryModel model) {
        return new MoneyDTO(
            model.getRequestedAmount(),
            model.getCurrency()
        );
    }
}
```

### Key Principles

1. **Reuse existing entities**: No duplicate read models in persistence layer
2. **Query Models in application**: Defined once in core/application/query
3. **Adapter responsibility**: Map Entity → QueryModel using existing entities
4. **Handler responsibility**: Map QueryModel → DTO in application layer
5. **No domain entities in queries**: Entities stay in persistence adapter

### JPA Specifications for Complex Queries

```java
// Location: adapters/driven/persistence/loanfacility/query/specification/
package ir.dotin.loan.trade.adapters.driven.persistence.loanfacility.query.specification;

public class FacilitySpecification {
    
    public static Specification<TradeLoanFacilityEntity> fromCriteria(
        FacilitySearchCriteria criteria
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            Optional.ofNullable(criteria.facilityCode())
                .ifPresent(code -> predicates.add(
                    cb.like(cb.lower(root.get("facilityCode")), 
                        "%" + code.toLowerCase() + "%")
                ));
            
            Optional.ofNullable(criteria.customerNationalId())
                .ifPresent(nid -> predicates.add(
                    cb.equal(root.get("customer").get("nationalId"), nid)
                ));
            
            Optional.ofNullable(criteria.status())
                .ifPresent(status -> predicates.add(
                    cb.equal(root.get("status"), status)
                ));
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
```

### Query Optimization

#### Custom JPQL with Existing Entities

```java
// Add to existing Spring Data repository
public interface TradeLoanFacilityJpaRepository 
    extends JpaRepository<TradeLoanFacilityEntity, UUID>,
            JpaSpecificationExecutor<TradeLoanFacilityEntity> {
    
    // Query methods for query adapter
    @Query("""
        SELECT f FROM TradeLoanFacilityEntity f
        LEFT JOIN FETCH f.arrangement
        LEFT JOIN FETCH f.customer
        WHERE f.id = :facilityId
    """)
    Optional<TradeLoanFacilityEntity> findByIdWithDetails(
        @Param("facilityId") UUID facilityId
    );
    
    @Query("""
        SELECT f FROM TradeLoanFacilityEntity f
        WHERE f.status = 'ACTIVE'
        AND EXISTS (
            SELECT 1 FROM InstallmentEntity i
            WHERE i.facilityId = f.id
            AND i.dueDate < :asOfDate
            AND i.status != 'PAID'
        )
    """)
    List<TradeLoanFacilityEntity> findOverdueFacilities(
        @Param("asOfDate") LocalDate asOfDate
    );
}
```

#### Caching in Adapter

```java
@Repository
@Transactional(readOnly = true)
public class JpaFacilityQueryAdapter implements FacilityQueryRepository {
    
    @Override
    @Cacheable(value = "facilityById", key = "#facilityId")
    public Optional<FacilityQueryModel> findById(UUID facilityId) {
        return jpaRepository.findByIdWithDetails(facilityId)
            .map(queryModelMapper::toQueryModel);
    }
}
```

### Complete Package Structure

```
core/application/query/loanfacility/
├── GetFacilityByIdQuery.java
├── GetFacilityByIdQueryHandler.java
├── model/                                    # Query Models (application layer)
│   ├── FacilityQueryModel.java
│   ├── FacilitySearchQueryModel.java
│   └── OutstandingBalanceQueryModel.java
├── mapper/                                   # QueryModel → DTO
│   └── FacilityDTOMapper.java
└── dto/
    └── TradeFacilityDTO.java

adapters/driven/persistence/loanfacility/query/
├── JpaFacilityQueryAdapter.java              # Implements port
├── mapper/
│   └── FacilityQueryModelMapper.java         # Entity → QueryModel
└── specification/
    └── FacilitySpecification.java

adapters/driven/persistence/loanfacility/      # Reuse existing
├── TradeLoanFacilityJpaRepository.java       # Add query methods
└── entity/
    └── TradeLoanFacilityEntity.java          # Existing entity
```

**Key Benefits:**
- Single source of entities (no duplication)
- Query Models centralized in application layer
- Clear separation: adapter maps Entity→QueryModel, handler maps QueryModel→DTO
- Existing repositories extended for query needs

