package ir.dotin.loan.trade.adapters.driving.rest.query.loanfacility;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import org.springframework.format.annotation.DateTimeFormat;
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
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.FacilityStatus;
import ir.dotin.loan.trade.adapters.driving.rest.config.SwaggerConfig;
import ir.dotin.loan.trade.core.application.query.loanfacility.dto.LoanFacilityQueryResult;
import ir.dotin.loan.trade.core.application.query.loanfacility.dto.TradeFacilityQueryDto;
import ir.dotin.loan.trade.core.application.query.loanfacility.request.FindAllLoanFacilitiesQuery;
import ir.dotin.loan.trade.core.application.query.loanfacility.request.GetFacilityByIdQuery;
import ir.dotin.loan.trade.core.application.query.loanfacility.request.LoanFacilityFilterQuery;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/{version}/loan-facilities")
@RequiredArgsConstructor
@Tag(name = SwaggerConfig.TAG_FACILITY_QUERIES, description = "استعلام تسهیلات")
class FacilityQueryController extends BaseController {

    private final QueryDispatcher queryDispatcher;

    @GetMapping(value = "/{facilityId}", version = "1")
    @Operation(summary = "دریافت تسهیلات بر اساس شناسه")
    public ResponseEntity<BaseResponse<TradeFacilityQueryDto>> getById(@PathVariable UUID facilityId) {
        GetFacilityByIdQuery query =
                GetFacilityByIdQuery.builder().loanFacilityId(facilityId).build();
        return ResponseEntity.ok(BaseResponse.success(queryDispatcher.dispatch(query)));
    }

    @GetMapping(version = "1")
    @Operation(summary = "دریافت لیست تمام تسهیلات")
    public ResponseEntity<BaseResponse<List<TradeFacilityQueryDto>>> findAll(
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int pageSize) {

        FindAllLoanFacilitiesQuery query = FindAllLoanFacilitiesQuery.builder()
                .cursor(cursor)
                .pageSize(pageSize)
                .build();

        LoanFacilityQueryResult result = queryDispatcher.dispatch(query);

        return CursorPaginationHelper.response(
                result.facilities(), result.nextCursor(), result.previousCursor(), pageSize, result.hasNext());
    }

    @GetMapping(value = "/search", version = "1")
    @Operation(summary = "جستجوی تسهیلات با فیلترهای مختلف")
    public ResponseEntity<BaseResponse<List<TradeFacilityQueryDto>>> searchFacilities(
            @RequestParam(required = false) UUID loanTypeId,
            @RequestParam(required = false) String customerNumber,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    LocalDateTime createDateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    LocalDateTime createDateTo,
            @RequestParam(required = false) BigDecimal requestAmountMin,
            @RequestParam(required = false) BigDecimal requestAmountMax,
            @RequestParam(required = false) FacilityStatus status,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int pageSize) {

        LoanFacilityFilterQuery query = LoanFacilityFilterQuery.of(
                loanTypeId,
                customerNumber,
                createDateFrom,
                createDateTo,
                requestAmountMin,
                requestAmountMax,
                status,
                page,
                pageSize);

        LoanFacilityQueryResult result = queryDispatcher.dispatch(query);

        return ResponseEntity.ok(PagedResponseUtils.offset(
                result.facilities(), result.currentPage(), result.pageSize(), result.totalElements()));
    }
}
