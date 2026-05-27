package ir.dotin.loan.trade.adapters.driving.rest.query.loanarrangement;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ir.dotin.platform.pangaea.dispatcher.api.dispatcher.QueryDispatcher;
import ir.dotin.platform.pangaea.protocol.api.response.BaseResponse;
import ir.dotin.platform.pangaea.protocol.api.util.PagedResponseUtils;
import ir.dotin.platform.pangaea.protocol.rest.pagination.CursorPaginationHelper;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.DisbursementMethod;
import ir.dotin.loan.trade.adapters.driving.rest.config.SwaggerConfig;
import ir.dotin.loan.trade.core.application.query.loanarrangement.dto.LoanArrangementQueryResult;
import ir.dotin.loan.trade.core.application.query.loanarrangement.dto.TradeLoanArrangementQueryDto;
import ir.dotin.loan.trade.core.application.query.loanarrangement.request.FindAllLoanArrangementsQuery;
import ir.dotin.loan.trade.core.application.query.loanarrangement.request.GetLoanArrangementByIdQuery;
import ir.dotin.loan.trade.core.application.query.loanarrangement.request.LoanTypeArrangementFilterQuery;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v{version}/loan-arrangements")
@RequiredArgsConstructor
@Tag(name = SwaggerConfig.TAG_LOAN_ARRANGEMENT_QUERIES, description = "استعلام شرایط تسهیلات")
class LoanArrangementQueryController {

    private final QueryDispatcher dispatcher;

    @GetMapping(value = "/{loanArrangementId}", version = "1")
    @Operation(summary = "دریافت شرایط تسهیلات بر اساس شناسه")
    public ResponseEntity<BaseResponse<TradeLoanArrangementQueryDto>> getById(@PathVariable UUID loanArrangementId) {
        GetLoanArrangementByIdQuery query = GetLoanArrangementByIdQuery.builder()
                .loanArrangementId(loanArrangementId)
                .build();
        return ResponseEntity.ok(BaseResponse.success(dispatcher.dispatch(query)));
    }

    @GetMapping(version = "1")
    @Operation(summary = "Get all loan arrangements with cursor-based pagination")
    public ResponseEntity<BaseResponse<List<TradeLoanArrangementQueryDto>>> findAll(
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int pageSize) {

        FindAllLoanArrangementsQuery query = FindAllLoanArrangementsQuery.builder()
                .cursor(cursor)
                .pageSize(pageSize)
                .build();

        LoanArrangementQueryResult result = dispatcher.dispatch(query);

        return CursorPaginationHelper.response(
                result.loanArrangements(), result.nextCursor(), result.previousCursor(), pageSize, result.hasNext());
    }

    @GetMapping(value = "/search", version = "1")
    @Operation(summary = "جستجوی شرایط تسهیلات با فیلترهای مختلف")
    public ResponseEntity<BaseResponse<List<TradeLoanArrangementQueryDto>>> searchArrangements(
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

        return ResponseEntity.ok(PagedResponseUtils.offset(
                result.loanArrangements(), result.currentPage(), result.pageSize(), result.totalElements()));
    }
}
