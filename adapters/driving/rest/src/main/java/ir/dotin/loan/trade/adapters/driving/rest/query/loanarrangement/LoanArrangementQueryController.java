package ir.dotin.loan.trade.adapters.driving.rest.query.loanarrangement;

import java.math.BigDecimal;
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
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.DisbursementMethod;
import ir.dotin.loan.trade.adapters.driving.rest.config.SwaggerConfig;
import ir.dotin.loan.trade.adapters.driving.rest.shared.pagination.CursorPaginationHelper;
import ir.dotin.loan.trade.core.application.query.loanarrangement.dto.LoanArrangementQueryResult;
import ir.dotin.loan.trade.core.application.query.loanarrangement.dto.TradeLoanArrangementQueryDto;
import ir.dotin.loan.trade.core.application.query.loanarrangement.request.FindAllLoanArrangementsQuery;
import ir.dotin.loan.trade.core.application.query.loanarrangement.request.GetLoanArrangementByIdQuery;
import ir.dotin.loan.trade.core.application.query.loanarrangement.request.LoanTypeArrangementFilterQuery;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/{version}/loan-arrangements")
@RequiredArgsConstructor
@Tag(name = SwaggerConfig.TAG_LOAN_ARRANGEMENT_QUERIES, description = "استعلام شرایط تسهیلات")
public class LoanArrangementQueryController {
    private final QueryDispatcher dispatcher;

    @GetMapping(value = "/{loanArrangementId}", version = "1")
    @Operation(summary = "دریافت شرایط تسهیلات بر اساس شناسه")
    public DataResponse<TradeLoanArrangementQueryDto> getById(@PathVariable UUID loanArrangementId) {
        GetLoanArrangementByIdQuery query = GetLoanArrangementByIdQuery.builder()
                .loanArrangementId(loanArrangementId)
                .build();
        return DataResponse.of(dispatcher.dispatch(query));
    }

    @GetMapping(version = "1")
    @Operation(summary = "Get all loan arrangements with cursor-based pagination")
    public ResponseEntity<PagedResponse<LoanArrangementQueryResult>> findAll(
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int pageSize) {
        FindAllLoanArrangementsQuery query = FindAllLoanArrangementsQuery.builder()
                .cursor(cursor)
                .pageSize(pageSize)
                .build();

        LoanArrangementQueryResult result = dispatcher.dispatch(query);

        return CursorPaginationHelper.createPaginatedResponse(
                result, result.nextCursor(), result.previousCursor(), result.hasNext(), result.hasPrevious());
    }

    @GetMapping(value = "/search", version = "1")
    @Operation(summary = "جستجوی شرایط تسهیلات با فیلترهای مختلف")
    public ResponseEntity<PagedResponse<LoanArrangementQueryResult>> searchArrangements(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String currencyType,
            @RequestParam(required = false) String economicSector,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Boolean disable,
            @RequestParam(required = false) DisbursementMethod disbursementMethod,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int pageSize) {
        LoanTypeArrangementFilterQuery query = LoanTypeArrangementFilterQuery.of(
                code,
                title,
                currencyType,
                economicSector,
                minAmount,
                maxAmount,
                active,
                disable,
                disbursementMethod,
                page,
                pageSize);

        LoanArrangementQueryResult result = dispatcher.dispatch(query);

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
