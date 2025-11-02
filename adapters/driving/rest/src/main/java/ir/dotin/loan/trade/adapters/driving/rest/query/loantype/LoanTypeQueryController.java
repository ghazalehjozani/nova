package ir.dotin.loan.trade.adapters.driving.rest.query.loantype;

import java.util.UUID;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ir.dotin.platform.adapter.rest.response.DataResponse;
import ir.dotin.platform.adapter.rest.response.OffsetPaginationInfo;
import ir.dotin.platform.adapter.rest.response.PagedResponse;
import ir.dotin.platform.dispatcher.api.dispatcher.QueryDispatcher;
import ir.dotin.loan.trade.adapters.driving.rest.config.SwaggerConfig;
import ir.dotin.loan.trade.adapters.driving.rest.shared.pagination.CursorPaginationHelper;
import ir.dotin.loan.trade.core.application.query.loantype.dto.LoanTypeQueryResult;
import ir.dotin.loan.trade.core.application.query.loantype.dto.TradeLoanTypeQueryDto;
import ir.dotin.loan.trade.core.application.query.loantype.request.FindAllLoanTypesQuery;
import ir.dotin.loan.trade.core.application.query.loantype.request.GetLoanTypeByIdQuery;
import ir.dotin.loan.trade.core.application.query.loantype.request.LoanTypeFilterQuery;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/loan-types")
@RequiredArgsConstructor
@Tag(name = SwaggerConfig.TAG_LOAN_TYPE_QUERIES, description = "استعلام نوع تسهیلات")
public class LoanTypeQueryController {
    private final QueryDispatcher dispatcher;

    @GetMapping("/{loanTypeId}")
    @Operation(summary = "دریافت نوع تسهیلات بر اساس شناسه")
    public DataResponse<TradeLoanTypeQueryDto> getById(@PathVariable UUID loanTypeId) {
        GetLoanTypeByIdQuery query =
                GetLoanTypeByIdQuery.builder().loanTypeId(loanTypeId).build();
        return DataResponse.of(dispatcher.dispatch(query));
    }

    @GetMapping
    @Operation(summary = "Get all loan types with cursor-based pagination")
    public ResponseEntity<PagedResponse<LoanTypeQueryResult>> findAll(
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int pageSize) {
        FindAllLoanTypesQuery query = FindAllLoanTypesQuery.builder()
                .cursor(cursor)
                .pageSize(pageSize)
                .build();

        LoanTypeQueryResult result = dispatcher.dispatch(query);

        return CursorPaginationHelper.createPaginatedResponse(
                result, result.nextCursor(), result.previousCursor(), result.hasNext(), result.hasPrevious());
    }

    @GetMapping("/search")
    @Operation(summary = "جستجوی انواع تسهیلات با فیلترهای مختلف")
    public ResponseEntity<PagedResponse<LoanTypeQueryResult>> searchLoanTypes(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String title,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int pageSize) {
        LoanTypeFilterQuery query = LoanTypeFilterQuery.of(code, title, page, pageSize);

        LoanTypeQueryResult result = dispatcher.dispatch(query);

        OffsetPaginationInfo paginationInfo = OffsetPaginationInfo.of(
                result.currentPage(),
                result.pageSize(),
                result.totalElements(),
                result.totalPages(),
                result.hasNext(),
                result.hasPrevious());

        return ResponseEntity.ok(PagedResponse.success(result, paginationInfo));
    }
}
