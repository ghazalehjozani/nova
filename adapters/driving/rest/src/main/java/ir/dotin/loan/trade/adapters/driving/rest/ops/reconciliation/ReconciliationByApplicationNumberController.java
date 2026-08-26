package ir.dotin.loan.trade.adapters.driving.rest.ops.reconciliation;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ir.dotin.platform.pangaea.protocol.api.response.BaseResponse;
import ir.dotin.platform.pangaea.protocol.rest.controller.BaseController;
import ir.dotin.platform.pangaea.reconciliation.api.admin.DiscrepancyView;
import ir.dotin.platform.pangaea.reconciliation.management.service.ReconciliationManagementService;
import ir.dotin.platform.pangaea.servicelayer.api.dispatcher.QueryDispatcher;
import ir.dotin.loan.trade.adapters.driving.rest.config.SwaggerConfig;
import ir.dotin.loan.trade.core.application.query.loanfacility.dto.FacilityIdView;
import ir.dotin.loan.trade.core.application.query.loanfacility.request.ResolveFacilityIdByApplicationNumberQuery;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v{version}/ops/reconciliation/facility-state")
@RequiredArgsConstructor
@Tag(name = SwaggerConfig.TAG_RECONCILIATION_OPS, description = "عملیات مغایرت‌گیری Nova↔FCB")
class ReconciliationByApplicationNumberController extends BaseController {

    // The wire code, duplicated deliberately: it is owned by NovaReconciliationType.FACILITY_STATE in
    // adapters/driven/reconciliation, and a driving adapter must not depend on a driven one
    // (TradeLoanAdapterArchitectureTest). Change both together.
    private static final String FACILITY_STATE = "facility-state";

    private final QueryDispatcher queryDispatcher;
    private final ReconciliationManagementService reconciliationManagementService;

    @GetMapping(value = "/by-application-number/{applicationNumber}", version = "1")
    @Operation(summary = "یافتن مغایرت وضعیت تسهیلات بر اساس شماره درخواست")
    public ResponseEntity<BaseResponse<DiscrepancyView>> getByApplicationNumber(
            @PathVariable String applicationNumber) {
        FacilityIdView resolved = queryDispatcher.dispatch(ResolveFacilityIdByApplicationNumberQuery.builder()
                .applicationNumber(applicationNumber)
                .build());
        UUID facilityId = resolved.loanFacilityId();
        DiscrepancyView discrepancy = reconciliationManagementService.get(FACILITY_STATE, facilityId.toString());
        return ResponseEntity.ok(BaseResponse.success(discrepancy));
    }
}
