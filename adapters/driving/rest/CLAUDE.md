# REST Adapter Implementation Guide

## 1. Overview

REST adapters are driving adapters in hexagonal architecture that translate HTTP requests into Commands/Queries and
dispatch them to handlers. They serve as the HTTP entry point to the application layer.

### Architecture Flow
```
External Client → REST Controller → Dispatcher → Handler → Domain/Repository → Response
```

### Key Components
- **Command**: Immutable record representing write intent
- **Query**: Immutable record representing read intent
- **CommandHandler**: Processes commands, returns `Result<T>`
- **QueryHandler**: Processes queries, returns `Result<T>`
- **Dispatcher**: Routes commands/queries to handlers

## 2. Package Structure Rules

### Command Controllers: Organized Per Use Case
Each command use case gets its own directory with controller and DTOs:

```
adapters/driving/rest/command/
├── createfacility/
│   ├── CreateFacilityController.java
│   ├── CreateFacilityRequest.java
│   └── CreateFacilityResponse.java
├── approvefacility/
│   ├── ApproveFacilityController.java
│   ├── ApproveFacilityRequest.java
│   └── ApproveFacilityResponse.java
├── disbursefacility/
│   ├── DisburseFacilityController.java
│   ├── DisburseFacilityRequest.java
│   └── DisburseFacilityResponse.java
└── deletefacility/
    ├── DeleteFacilityController.java
    └── DeleteFacilityRequest.java
```

### Query Controllers: Organized Per Aggregate
All queries for an aggregate in one controller:

```
adapters/driving/rest/query/
├── loanfacility/
│   ├── FacilityQueryController.java
│   └── response/
│       ├── FacilityResponse.java
│       ├── FacilitySearchResponse.java
│       └── OutstandingBalanceResponse.java
├── loanarrangement/
│   ├── ArrangementQueryController.java
│   └── response/
│       └── ArrangementResponse.java
└── installmentschedule/
    ├── ScheduleQueryController.java
    └── response/
        └── ScheduleResponse.java
```

### Shared Components
```
adapters/driving/rest/shared/
├── error/
│   ├── GlobalExceptionHandler.java
│   ├── ErrorResponse.java
│   └── ErrorCode.java
├── validation/
│   └── RequestValidator.java
└── mapper/
    └── ResponseMapper.java
```

## 3. REST Standards Compliance

All REST adapters **MUST** comply with inter-service communication standards.

### 3.1 URL Structure

**Pattern:**
```
/{version}/{domain-context}/{resource-collection}/{resource-id}/{sub-resource}
```

**Examples:**
```
POST   /v1/facilities
GET    /v1/facilities/{facilityId}
PUT    /v1/facilities/{facilityId}/approve
GET    /v1/facilities/{facilityId}/installments
DELETE /v1/facilities/{facilityId}
```

**Rules:**
- ✅ Use `kebab-case` for path segments
- ✅ Use `camelCase` for query parameters
- ✅ Plural nouns for collections (`facilities`, not `facility`)
- ✅ Mandatory `v{major}` prefix
- ✅ Avoid verbs in paths (use HTTP methods)
- ❌ No trailing slashes

### 3.2 Request Headers (Mandatory)

```http
Idempotency-Key: 123e4567-e89b-42d3-a456-556642440000
Idempotency-Key-TTL: 60
X-Request-DateTime: 2025-08-22T14:30:00.123Z
Accept-Language: fa
Authorization: Bearer {access_token}
```

**Header Rules:**
- `Idempotency-Key`: **Required** for POST/PUT/PATCH/DELETE. Must be UUID v4
- `Idempotency-Key-TTL`: Seconds to cache (1-259200). Recommended: 60
- `X-Request-DateTime`: UTC timestamp, ISO 8601, no offset
- `Accept-Language`: For localized messages
- `Authorization`: Bearer token for all authenticated endpoints

### 3.3 Response Headers (Mandatory)

```http
Idempotency-Key: 123e4567-e89b-42d3-a456-556642440000
X-Request-DateTime: 2025-08-22T14:30:00.123Z
X-Idempotency-Replayed: false
X-Response-DateTime: 2025-08-22T14:30:02.000Z
```

**Response Header Rules:**
- Copy `Idempotency-Key` and `X-Request-DateTime` from request
- `X-Idempotency-Replayed`: `true` if cached response, `false` if new
- `X-Response-DateTime`: Server timestamp

### 3.4 Response Body Structure

**Success Response:**
```json
{
    "data": {
        "facilityId": "uuid",
        "facilityCode": "FAC-2025-001"
    }
}
```

**Error Response:**
```json
{
    "errors": [
        {
            "code": "LOAN-0001",
            "message": "Invalid credentials",
            "details": []
        }
    ]
}
```

**Rules:**
- `data` field contains business data (only on success)
- `errors` array contains error details (only on failure)
- **Never** include both `data` and `errors` together
- JSON only (no XML support)

### 3.5 HTTP Status Codes

| Code | Status                | When to Use                         |
|------|-----------------------|-------------------------------------|
| 200  | OK                    | GET/PUT/PATCH successful            |
| 201  | Created               | POST successful (new resource)      |
| 204  | No Content            | DELETE successful                   |
| 400  | Bad Request           | Invalid request format/data         |
| 401  | Unauthorized          | Missing/invalid token               |
| 403  | Forbidden             | Insufficient permissions            |
| 404  | Not Found             | Resource not found                  |
| 409  | Conflict              | Duplicate request already processed |
| 422  | Unprocessable Entity  | Business rule violation             |
| 425  | Too Early             | Duplicate request in progress       |
| 429  | Too Many Requests     | Rate limit exceeded                 |
| 500  | Internal Server Error | Unexpected server error             |
| 503  | Service Unavailable   | Service temporarily down            |

### 3.6 Error Code Format

**Pattern:** `[PREFIX]-[SEQUENCE]`

Example: `LOAN-0001`

**Reserved Error Codes (0001-0100):**
| Code | HTTP Status | Meaning |
|------|-------------|---------|
| *-0001 | 400 | Invalid credentials |
| *-0002 | 401 | Invalid/missing token |
| *-0003 | 403 | Insufficient permissions |
| *-0004 | 425 | Duplicate in progress |
| *-0005 | 409 | Duplicate already processed |
| *-0006 | 429 | Rate limit exceeded |

**Rules:**
- PREFIX: 2-6 characters, product/service identifier (e.g., LOAN, PAY, USR)
- SEQUENCE: 4 digits (0001-9999)
- Codes 0001-0100 reserved across all services
- Each service maintains its own error code registry (0101+)

## 4. Command Controller Implementation

### 4.1 Basic Command Controller

```java
package ir.dotin.loan.trade.adapters.driving.rest.command.createfacility;

@RestController
@RequestMapping("/api/{version}/facilities")
@RequiredArgsConstructor
@Tag(name = "Create Facility", description = "Create new loan facility")
@Validated
public class CreateFacilityController {
    
    private final CommandDispatcher dispatcher;
    
    @PostMapping
    @PreAuthorize("hasAuthority('FACILITY_CREATE')")
    @Operation(summary = "Create new loan facility")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Facility created"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<CreateFacilityResponse> create(
        @RequestHeader("Idempotency-Key") 
        @NotNull UUID idempotencyKey,
        
        @RequestHeader("Idempotency-Key-TTL") 
        @Min(1) @Max(259200) Integer ttl,
        
        @RequestHeader("X-Request-DateTime") 
        @NotNull Instant requestDateTime,
        
        @RequestHeader(value = "Accept-Language", defaultValue = "fa") 
        String locale,
        
        @Valid @RequestBody CreateFacilityRequest request
    ) {
        // Map Request → Command
        CreateFacilityCommand command = CreateFacilityCommand.builder()
            .customerId(CustomerId.of(request.getCustomerId()))
            .requestedAmount(Money.of(
                request.getAmount(), 
                request.getCurrency()
            ))
            .arrangementCode(LoanArrangementCode.of(
                request.getArrangementCode()
            ))
            .applicationDate(request.getApplicationDate())
            .branchCode(request.getBranchCode())
            .build();
        
        // Dispatch to handler
        Result<LoanFacilityId> result = dispatcher.dispatch(command);
        
        // Map Result → Response
        return result.fold(
            failure -> ResponseEntity
                .badRequest()
                .body(CreateFacilityResponse.error(failure)),
            success -> ResponseEntity
                .status(HttpStatus.CREATED)
                .body(CreateFacilityResponse.success(success))
        );
    }
}
```

### 4.2 Request DTO

```java
package ir.dotin.loan.trade.adapters.driving.rest.command.createfacility;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateFacilityRequest {
    
    @NotNull(message = "Customer ID is required")
    @Schema(description = "Customer unique identifier", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID customerId;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be positive")
    @Schema(description = "Requested loan amount", example = "1000000.00")
    private BigDecimal amount;
    
    @NotBlank(message = "Currency is required")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Invalid currency code")
    @Schema(description = "Currency code", example = "IRR")
    private String currency;
    
    @NotBlank(message = "Arrangement code is required")
    @Schema(description = "Loan arrangement code", example = "ARR-001")
    private String arrangementCode;
    
    @NotNull(message = "Application date is required")
    @PastOrPresent(message = "Application date cannot be in future")
    @Schema(description = "Facility application date", example = "2025-10-06")
    private LocalDate applicationDate;
    
    @Min(value = 1, message = "Term must be at least 1 month")
    @Max(value = 360, message = "Term cannot exceed 360 months")
    @Schema(description = "Loan term in months", example = "24")
    private Integer termMonths;
    
    @Schema(description = "Branch code", example = "BR-001")
    private String branchCode;
}
```

### 4.3 Response DTO

```java
package ir.dotin.loan.trade.adapters.driving.rest.command.createfacility;

@Data
@Builder
public class CreateFacilityResponse {
    private DataWrapper data;
    private List<ErrorDetail> errors;
    
    @Data
    @Builder
    public static class DataWrapper {
        private UUID facilityId;
        private String facilityCode;
    }
    
    public static CreateFacilityResponse success(LoanFacilityId facilityId) {
        return CreateFacilityResponse.builder()
            .data(DataWrapper.builder()
                .facilityId(facilityId.value())
                .build())
            .build();
    }
    
    public static CreateFacilityResponse error(Notification notification) {
        return CreateFacilityResponse.builder()
            .errors(notification.getErrors().stream()
                .map(e -> ErrorDetail.builder()
                    .code(e.getCode())
                    .message(e.getMessage())
                    .build())
                .toList())
            .build();
    }
}
```

### 4.4 Update Command Example

```java
package ir.dotin.loan.trade.adapters.driving.rest.command.approvefacility;

@RestController
@RequestMapping("/api/{version}/facilities")
@RequiredArgsConstructor
@Tag(name = "Approve Facility")
public class ApproveFacilityController {
    
    private final CommandDispatcher dispatcher;
    
    @PutMapping("/{facilityId}/approve")
    @PreAuthorize("hasAuthority('FACILITY_APPROVE')")
    public ResponseEntity<ApproveFacilityResponse> approve(
        @PathVariable UUID facilityId,
        @RequestHeader("Idempotency-Key") UUID idempotencyKey,
        @RequestHeader("Idempotency-Key-TTL") Integer ttl,
        @RequestHeader("X-Request-DateTime") Instant requestDateTime,
        @Valid @RequestBody ApproveFacilityRequest request
    ) {
        ApproveFacilityCommand command = new ApproveFacilityCommand(
            LoanFacilityId.of(facilityId),
            Money.of(request.getApprovedAmount(), request.getCurrency()),
            request.getApprovalDate()
        );
        
        Result<Void> result = dispatcher.dispatch(command);
        
        return result.fold(
            failure -> ResponseEntity.badRequest()
                .body(ApproveFacilityResponse.error(failure)),
            success -> ResponseEntity.ok()
                .body(ApproveFacilityResponse.success())
        );
    }
}
```

### 4.5 Delete Command Example

```java
package ir.dotin.loan.trade.adapters.driving.rest.command.deletefacility;

@RestController
@RequestMapping("/api/{version}/facilities")
@RequiredArgsConstructor
@Tag(name = "Delete Facility")
public class DeleteFacilityController {
    
    private final CommandDispatcher dispatcher;
    
    @DeleteMapping("/{facilityId}")
    @PreAuthorize("hasAuthority('FACILITY_DELETE')")
    public ResponseEntity<Void> delete(
        @PathVariable UUID facilityId,
        @RequestHeader("Idempotency-Key") UUID idempotencyKey
    ) {
        DeleteFacilityCommand command = new DeleteFacilityCommand(
            LoanFacilityId.of(facilityId)
        );
        
        Result<Void> result = dispatcher.dispatch(command);
        
        return result.isSuccess() 
            ? ResponseEntity.noContent().build()
            : ResponseEntity.badRequest().build();
    }
}
```

## 5. Query Controller Implementation

### 5.0 Query Architecture & Dependencies

**Queries use a simplified architecture compared to commands:**

```
Web Adapter → Query Handler (application/query) → Persistence Adapter
     ↓              ↓                                    ↓
  depends on    defines ViewModel               returns ViewModel
```

**Key differences from commands:**
- **No explicit port inbound needed** - Web adapter calls query handler directly
- **Shared ViewModels** - Query module defines DTOs/ViewModels used by both adapters
- **Direct dependency** - Web adapter depends on application/query module

**Dependency structure:**
```
adapters/
├── driving/rest/          (depends on core/application/query)
└── driven/persistence/    (depends on core/application/query)

core/
└── application/
    └── query/
        ├── handlers/      (query handler classes)
        └── dto/           (ViewModels/DTOs shared by all adapters)
```

This approach is pragmatic for reads - strict port isolation is more valuable for commands protecting domain invariants.


### 5.1 Complete Query Controller

```java
package ir.dotin.loan.trade.adapters.driving.rest.query.loanfacility;

@RestController
@RequestMapping("/api/{version}/facilities")
@RequiredArgsConstructor
@Tag(name = "Facility Queries", description = "Query loan facilities")
public class FacilityQueryController {
    
    private final QueryDispatcher dispatcher;
    private final ResponseMapper mapper;
    
    @GetMapping("/{facilityId}")
    @PreAuthorize("hasAuthority('FACILITY_VIEW')")
    @Operation(summary = "Get facility by ID")
    public ResponseEntity<FacilityResponse> getById(
        @PathVariable UUID facilityId,
        @RequestHeader(value = "Accept-Language", defaultValue = "fa") String locale
    ) {
        GetFacilityByIdQuery query = new GetFacilityByIdQuery(
            LoanFacilityId.of(facilityId)
        );
        
        Result<TradeFacilityDTO> result = dispatcher.dispatch(query);
        
        return result.fold(
            failure -> ResponseEntity.notFound().build(),
            success -> ResponseEntity.ok(
                FacilityResponse.success(mapper.toFacilityData(success))
            )
        );
    }
    
    @GetMapping
    @PreAuthorize("hasAuthority('FACILITY_SEARCH')")
    @Operation(summary = "Search facilities by criteria")
    public ResponseEntity<FacilitySearchResponse> search(
        @RequestParam(required = false) String facilityCode,
        @RequestParam(required = false) String customerNationalId,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String branchCode,
        @RequestParam(defaultValue = "0") @Min(0) int page,
        @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
        @RequestParam(defaultValue = "createdAt") String sortBy,
        @RequestParam(defaultValue = "desc") String sortDirection
    ) {
        FacilitySearchCriteria criteria = FacilitySearchCriteria.builder()
            .facilityCode(facilityCode)
            .customerNationalId(customerNationalId)
            .status(status != null ? FacilityStatus.valueOf(status) : null)
            .branchCode(branchCode)
            .build();
        
        Pageable pageable = PageRequest.of(
            page, size, 
            Sort.by(Sort.Direction.fromString(sortDirection), sortBy)
        );
        
        SearchFacilitiesByCriteriaQuery query = 
            new SearchFacilitiesByCriteriaQuery(criteria, pageable);
        
        Result<PaginatedResult<TradeFacilityDTO>> result = 
            dispatcher.dispatch(query);
        
        return result.fold(
            failure -> ResponseEntity.badRequest().build(),
            success -> ResponseEntity.ok(
                FacilitySearchResponse.success(
                    mapper.toFacilitySearchData(success)
                )
            )
        );
    }
    
    @GetMapping("/{facilityId}/outstanding")
    @PreAuthorize("hasAuthority('FACILITY_VIEW')")
    @Cacheable(value = "facilityOutstanding", key = "#facilityId + '-' + #asOfDate")
    public ResponseEntity<OutstandingBalanceResponse> getOutstanding(
        @PathVariable UUID facilityId,
        @RequestParam(required = false) 
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOfDate
    ) {
        CalculateFacilityOutstandingQuery query = 
            new CalculateFacilityOutstandingQuery(
                LoanFacilityId.of(facilityId),
                asOfDate != null ? asOfDate : LocalDate.now()
            );
        
        Result<OutstandingBalanceDTO> result = dispatcher.dispatch(query);
        
        return result.fold(
            failure -> ResponseEntity.notFound().build(),
            success -> ResponseEntity.ok(
                OutstandingBalanceResponse.success(
                    mapper.toOutstandingData(success)
                )
            )
        );
    }
    
    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAuthority('FACILITY_VIEW')")
    public ResponseEntity<FacilitySearchResponse> getByCustomer(
        @PathVariable UUID customerId,
        @RequestParam(required = false) String status,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        SearchFacilitiesByCustomerQuery query = 
            new SearchFacilitiesByCustomerQuery(
                CustomerId.of(customerId),
                status != null ? FacilityStatus.valueOf(status) : null,
                PageRequest.of(page, size)
            );
        
        Result<PaginatedResult<TradeFacilityDTO>> result = 
            dispatcher.dispatch(query);
        
        return result.fold(
            failure -> ResponseEntity.badRequest().build(),
            success -> ResponseEntity.ok(
                FacilitySearchResponse.success(
                    mapper.toFacilitySearchData(success)
                )
            )
        );
    }
}
```

### 5.2 Query Response DTOs

```java
package ir.dotin.loan.trade.adapters.driving.rest.query.loanfacility.response;

@Data
@Builder
public class FacilityResponse {
    private FacilityData data;
    private List<ErrorDetail> errors;
    
    @Data
    @Builder
    public static class FacilityData {
        private UUID id;
        private String facilityCode;
        private CustomerInfo customer;
        private MoneyInfo requestedAmount;
        private MoneyInfo approvedAmount;
        private String status;
        private ArrangementInfo arrangement;
        private LocalDate applicationDate;
        private LocalDate approvalDate;
        private AuditInfo audit;
    }
    
    public static FacilityResponse success(FacilityData data) {
        return FacilityResponse.builder().data(data).build();
    }
}

@Data
@Builder
public class FacilitySearchResponse {
    private SearchData data;
    private List<ErrorDetail> errors;
    
    @Data
    @Builder
    public static class SearchData {
        private List<FacilityData> facilities;
        private long totalElements;
        private int totalPages;
        private int currentPage;
        private int pageSize;
    }
    
    public static FacilitySearchResponse success(SearchData data) {
        return FacilitySearchResponse.builder().data(data).build();
    }
}
```

## 6. Error Handling

### 6.1 Global Exception Handler

```java
package ir.dotin.loan.trade.adapters.driving.rest.shared.error;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
        MethodArgumentNotValidException ex
    ) {
        List<ErrorDetail> errors = ex.getBindingResult().getFieldErrors()
            .stream()
            .map(error -> ErrorDetail.builder()
                .code("LOAN-1000")
                .message(error.getDefaultMessage())
                .details(List.of(Map.of("field", error.getField())))
                .build())
            .toList();
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse.builder().errors(errors).build());
    }
    
    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResponse> handleDomain(DomainException ex) {
        ErrorDetail error = ErrorDetail.builder()
            .code(ex.getCode())
            .message(ex.getMessage())
            .build();
        
        return ResponseEntity
            .status(HttpStatus.UNPROCESSABLE_ENTITY)
            .body(ErrorResponse.builder().errors(List.of(error)).build());
    }
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
        ResourceNotFoundException ex
    ) {
        ErrorDetail error = ErrorDetail.builder()
            .code("LOAN-1001")
            .message(ex.getMessage())
            .build();
        
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse.builder().errors(List.of(error)).build());
    }
    
    @ExceptionHandler(InsufficientPermissionsException.class)
    public ResponseEntity<ErrorResponse> handlePermissions(
        InsufficientPermissionsException ex
    ) {
        ErrorDetail error = ErrorDetail.builder()
            .code("LOAN-0003")  // Reserved code
            .message("Current user does not have permission for this operation")
            .build();
        
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(ErrorResponse.builder().errors(List.of(error)).build());
    }
    
    @ExceptionHandler(DuplicateRequestException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(
        DuplicateRequestException ex
    ) {
        ErrorDetail error = ErrorDetail.builder()
            .code("LOAN-0005")  // Reserved code
            .message("This operation is duplicate and already processed")
            .build();
        
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(ErrorResponse.builder().errors(List.of(error)).build());
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        log.error("Unexpected error occurred", ex);
        
        ErrorDetail error = ErrorDetail.builder()
            .code("LOAN-9999")
            .message("An unexpected error occurred")
            .build();
        
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse.builder().errors(List.of(error)).build());
    }
}
```

### 6.2 Error Response Models

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    private List<ErrorDetail> errors;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorDetail {
    private String code;
    private String message;
    private List<Object> details;
}
```

## 7. Response Mapping

```java
package ir.dotin.loan.trade.adapters.driving.rest.shared.mapper;

@Component
public class ResponseMapper {
    
    public FacilityData toFacilityData(TradeFacilityDTO dto) {
        return FacilityData.builder()
            .id(dto.id())
            .facilityCode(dto.facilityCode())
            .customer(toCustomerInfo(dto.customer()))
            .requestedAmount(toMoneyInfo(dto.requestedAmount()))
            .approvedAmount(toMoneyInfo(dto.approvedAmount()))
            .status(dto.facilityStatus())
            .arrangement(toArrangementInfo(dto.arrangement()))
            .applicationDate(dto.applicationDate())
            .approvalDate(dto.approvalDate())
            .audit(toAuditInfo(dto.audit()))
            .build();
    }
    
    public SearchData toFacilitySearchData(
        PaginatedResult<TradeFacilityDTO> result
    ) {
        return SearchData.builder()
            .facilities(result.content().stream()
                .map(this::toFacilityData)
                .toList())
            .totalElements(result.totalElements())
            .totalPages(result.totalPages())
            .currentPage(result.currentPage())
            .pageSize(result.pageSize())
            .build();
    }
    
    private CustomerInfo toCustomerInfo(CustomerDTO dto) {
        return CustomerInfo.builder()
            .id(dto.id())
            .name(dto.name())
            .nationalId(dto.nationalId())
            .build();
    }
    
    private MoneyInfo toMoneyInfo(MoneyDTO dto) {
        return MoneyInfo.builder()
            .amount(dto.amount())
            .currency(dto.currency())
            .build();
    }
}
```

## 8. Security Configuration

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health", "/v3/api-docs/**").permitAll()
                .requestMatchers("/v1/**").authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtConverter()))
            );
        
        return http.build();
    }
    
    @Bean
    public JwtAuthenticationConverter jwtConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(
            new CustomAuthoritiesConverter()
        );
        return converter;
    }
}
```

## 9. Idempotency Implementation

Handled by dispatchers

## 10. OpenAPI Documentation

```java
@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Trade Loan API",
        version = "1.0",
        description = "Trade Loan REST API"
    ),
    servers = {
        @Server(url = "https://api-staging.loan.dotin.ir", description = "Staging")
    }
)
public class OpenAPIConfig {
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .components(new Components()
                .addSecuritySchemes("bearer-jwt",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                )
            )
            .addSecurityItem(
                new SecurityRequirement().addList("bearer-jwt")
            );
    }
}
```

## 11. Testing

### 11.1 Command Controller Test

```java
@WebMvcTest(CreateFacilityController.class)
@Import(SecurityTestConfig.class)
class CreateFacilityControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private CommandDispatcher dispatcher;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    @WithMockUser(authorities = "FACILITY_CREATE")
    void shouldCreateFacility() throws Exception {
        CreateFacilityRequest request = CreateFacilityRequest.builder()
            .customerId(UUID.randomUUID())
            .amount(new BigDecimal("1000000"))
            .currency("IRR")
            .arrangementCode("ARR-001")
            .applicationDate(LocalDate.now())
            .build();
        
        LoanFacilityId facilityId = LoanFacilityId.of(UUID.randomUUID());
        when(dispatcher.dispatch(any(CreateFacilityCommand.class)))
            .thenReturn(Result.success(facilityId));
        
        mockMvc.perform(post("/v1/facilities")
                .header("Idempotency-Key", UUID.randomUUID())
                .header("Idempotency-Key-TTL", "60")
                .header("X-Request-DateTime", Instant.now())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.facilityId").exists());
    }
    
    @Test
    @WithMockUser(authorities = "FACILITY_CREATE")
    void shouldReturnBadRequestWhenValidationFails() throws Exception {
        CreateFacilityRequest request = CreateFacilityRequest.builder()
            .amount(new BigDecimal("-100"))  // Invalid
            .build();
        
        mockMvc.perform(post("/v1/facilities")
                .header("Idempotency-Key", UUID.randomUUID())
                .header("Idempotency-Key-TTL", "60")
                .header("X-Request-DateTime", Instant.now())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors").isArray());
    }
}
```

### 11.2 Query Controller Test

```java
@WebMvcTest(FacilityQueryController.class)
@Import(SecurityTestConfig.class)
class FacilityQueryControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private QueryDispatcher dispatcher;
    
    @MockBean
    private ResponseMapper mapper;
    
    @Test
    @WithMockUser(authorities = "FACILITY_VIEW")
    void shouldGetFacilityById() throws Exception {
        UUID facilityId = UUID.randomUUID();
        TradeFacilityDTO dto = createTestDTO();
        
        when(dispatcher.dispatch(any(GetFacilityByIdQuery.class)))
            .thenReturn(Result.success(dto));
        
        mockMvc.perform(get("/v1/facilities/{id}", facilityId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").exists());
    }
    
    @Test
    @WithMockUser(authorities = "FACILITY_VIEW")
    void shouldReturn404WhenFacilityNotFound() throws Exception {
        UUID facilityId = UUID.randomUUID();
        
        when(dispatcher.dispatch(any(GetFacilityByIdQuery.class)))
            .thenReturn(Result.failure(Notification.ofError("Not found")));
        
        mockMvc.perform(get("/v1/facilities/{id}", facilityId))
            .andExpect(status().isNotFound());
    }
}
```

## 12. Implementation Rules

### 12.1 Mandatory Rules

1. **Organization**
    - Commands: One use case per directory
    - Queries: One aggregate per controller

2. **Dispatching**
    - Always use CommandDispatcher/QueryDispatcher
    - Never call handlers directly

3. **Mapping**
    - Request → Command/Query in controller
    - Result → Response in controller
    - Use dedicated mapper classes

4. **Headers**
    - Enforce Idempotency-Key for POST/PUT/PATCH/DELETE
    - Return all mandatory response headers
    - Copy request headers to response

5. **Error Handling**
    - Use GlobalExceptionHandler
    - Follow error code format: PREFIX-SEQUENCE
    - Use reserved codes (0001-0100) correctly

6. **Security**
    - @PreAuthorize on all endpoints
    - Validate JWT tokens
    - Check permissions via AMS

7. **Standards**
    - Follow inter-service communication standards
    - Use standard HTTP status codes
    - Return standard response format

### 12.2 Best Practices

1. **Validation**
    - Bean Validation on request DTOs
    - Business validation in domain layer
    - Clear error messages

2. **Documentation**
    - OpenAPI annotations on all endpoints
    - Example values in schemas
    - Describe error responses

3. **Testing**
    - Unit tests for controllers
    - Mock dispatchers
    - Test happy path and error cases

4. **Performance**
    - Cache query results when appropriate
    - Use pagination for lists
    - Limit page size (max 100)

5. **Logging**
    - Log all errors with context
    - Include request/correlation IDs
    - Don't log sensitive data
