package ir.dotin.loan.trade.adapters.driving.mcp;

import java.util.UUID;

import org.springframework.stereotype.Component;

import ir.dotin.platform.pangaea.ai.mcp.api.McpBusinessTool;
import ir.dotin.platform.pangaea.ai.mcp.api.McpTool;
import ir.dotin.platform.pangaea.ai.mcp.api.McpToolParam;
import ir.dotin.platform.pangaea.dispatcher.api.dispatcher.QueryDispatcher;
import ir.dotin.loan.trade.core.application.query.installmentschedule.dto.TradeInstallmentScheduleQueryDto;
import ir.dotin.loan.trade.core.application.query.installmentschedule.request.GetInstallmentScheduleByIdQuery;

import lombok.RequiredArgsConstructor;

/**
 * Read-only MCP business tool over the <b>installment schedule</b> aggregate (the repayment plan generated for a
 * disbursed facility). Mirrors the REST {@code InstallmentScheduleQueryController}, which today exposes only a
 * get-by-id read.
 */
@McpBusinessTool
@Component
@RequiredArgsConstructor
public class InstallmentScheduleMcpTools {

    private final QueryDispatcher dispatcher;

    @McpTool(
            name = "loan_get_installment_schedule",
            description =
                    "Fetch an installment (repayment) schedule by its UUID. Returns the full schedule projection: the"
                            + " ordered installments with their due dates, principal/profit breakdown and amounts. Use"
                            + " when the user references a specific installment-schedule id (obtainable from a facility"
                            + " projection).")
    public TradeInstallmentScheduleQueryDto loanGetInstallmentSchedule(
            @McpToolParam(name = "installmentScheduleId", description = "The installment schedule UUID.")
                    UUID installmentScheduleId) {
        GetInstallmentScheduleByIdQuery query = GetInstallmentScheduleByIdQuery.builder()
                .installmentScheduleId(installmentScheduleId)
                .build();
        return dispatcher.dispatch(query);
    }
}
