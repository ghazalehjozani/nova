package ir.dotin.loan.trade.adapters.driving.rest.query.loantype;

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
import ir.dotin.platform.pangaea.protocol.rest.controller.BaseController;
import ir.dotin.platform.pangaea.protocol.rest.pagination.CursorPaginationHelper;
import ir.dotin.loan.trade.adapters.driving.rest.config.SwaggerConfig;
import ir.dotin.loan.trade.core.application.query.loantype.dto.LoanTypeQueryResult;
import ir.dotin.loan.trade.core.application.query.loantype.dto.TradeLoanTypeQueryDto;
import ir.dotin.loan.trade.core.application.query.loantype.request.FindAllLoanTypesQuery;
import ir.dotin.loan.trade.core.application.query.loantype.request.GetLoanTypeByCodeQuery;
import ir.dotin.loan.trade.core.application.query.loantype.request.GetLoanTypeByIdQuery;
import ir.dotin.loan.trade.core.application.query.loantype.request.LoanTypeFilterQuery;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v{version}/loan-types")
@RequiredArgsConstructor
@Tag(name = SwaggerConfig.TAG_LOAN_TYPE_QUERIES, description = "استعلام نوع تسهیلات")
class LoanTypeQueryController extends BaseController {

    private final QueryDispatcher dispatcher;

    @GetMapping(value = "/{loanTypeId}", version = "1")
    @Operation(summary = "دریافت نوع تسهیلات بر اساس شناسه")
    public ResponseEntity<BaseResponse<TradeLoanTypeQueryDto>> getById(@PathVariable UUID loanTypeId) {
        GetLoanTypeByIdQuery query =
                GetLoanTypeByIdQuery.builder().loanTypeId(loanTypeId).build();
        return ResponseEntity.ok(BaseResponse.success(dispatcher.dispatch(query)));
    }

    @GetMapping(params = "code", version = "1")
    @Operation(summary = "دریافت نوع تسهیلات بر اساس کد")
    public ResponseEntity<BaseResponse<TradeLoanTypeQueryDto>> getByCode(@RequestParam String code) {
        GetLoanTypeByCodeQuery query =
                GetLoanTypeByCodeQuery.builder().code(code).build();
        return ResponseEntity.ok(BaseResponse.success(dispatcher.dispatch(query)));
    }

    @GetMapping(version = "1")
    @Operation(summary = "Get all loan types with cursor-based pagination")
    public ResponseEntity<BaseResponse<List<TradeLoanTypeQueryDto>>> findAll(
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int pageSize) {

        FindAllLoanTypesQuery query = FindAllLoanTypesQuery.builder()
                .cursor(cursor)
                .pageSize(pageSize)
                .build();

        LoanTypeQueryResult result = dispatcher.dispatch(query);

        return CursorPaginationHelper.response(
                result.loanTypes(), result.nextCursor(), result.previousCursor(), pageSize, result.hasNext());
    }

    @GetMapping(value = "/search", version = "1")
    @Operation(summary = "جستجوی انواع تسهیلات با فیلترهای مختلف")
    public ResponseEntity<BaseResponse<List<TradeLoanTypeQueryDto>>> searchLoanTypes(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String title,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int pageSize) {

        LoanTypeFilterQuery query = LoanTypeFilterQuery.of(code, title, page, pageSize);
        LoanTypeQueryResult result = dispatcher.dispatch(query);

        return ResponseEntity.ok(PagedResponseUtils.offset(
                result.loanTypes(), result.currentPage(), result.pageSize(), result.totalElements()));
    }
}
