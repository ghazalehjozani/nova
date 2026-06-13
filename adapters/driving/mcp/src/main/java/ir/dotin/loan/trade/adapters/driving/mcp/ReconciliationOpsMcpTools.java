package ir.dotin.loan.trade.adapters.driving.mcp;

import java.util.UUID;

import org.springframework.stereotype.Component;

import ir.dotin.platform.pangaea.ai.mcp.api.McpOpsTool;
import ir.dotin.platform.pangaea.ai.mcp.api.McpTool;
import ir.dotin.platform.pangaea.ai.mcp.api.McpToolParam;
import ir.dotin.platform.pangaea.reconciliation.api.admin.DiscrepancyView;
import ir.dotin.platform.pangaea.reconciliation.management.service.ReconciliationManagementService;
import ir.dotin.platform.pangaea.servicelayer.api.dispatcher.QueryDispatcher;
import ir.dotin.loan.trade.core.application.query.loanfacility.dto.FacilityIdView;
import ir.dotin.loan.trade.core.application.query.loanfacility.request.ResolveFacilityIdByApplicationNumberQuery;

import lombok.RequiredArgsConstructor;

@McpOpsTool
@Component
@RequiredArgsConstructor
public class ReconciliationOpsMcpTools {

    private static final String FACILITY_STATE = "facility-state";

    private final QueryDispatcher dispatcher;
    private final ReconciliationManagementService reconciliationManagementService;

    @McpTool(
            name = "loan_recon_get_by_application_number",
            description = "Find the facility-state reconciliation discrepancy for a loan by its application number"
                    + " (e.g. 1-1404-10088-279). Nova-specific: resolves the application number to the facility id,"
                    + " then fetches the discrepancy.")
    public DiscrepancyView loanReconGetByApplicationNumber(
            @McpToolParam(
                            name = "applicationNumber",
                            description = "The loan application number, e.g. 1-1404-10088-279.")
                    String applicationNumber) {
        FacilityIdView resolved = dispatcher.dispatch(ResolveFacilityIdByApplicationNumberQuery.builder()
                .applicationNumber(applicationNumber)
                .build());
        UUID facilityId = resolved.loanFacilityId();
        return reconciliationManagementService.get(FACILITY_STATE, facilityId.toString());
    }
}
