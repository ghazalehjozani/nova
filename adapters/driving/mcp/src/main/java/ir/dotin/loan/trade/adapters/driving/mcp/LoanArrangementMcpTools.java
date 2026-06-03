package ir.dotin.loan.trade.adapters.driving.mcp;

import java.math.BigDecimal;
import java.util.UUID;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import ir.dotin.platform.pangaea.ai.mcp.api.McpBusinessTool;
import ir.dotin.platform.pangaea.ai.mcp.api.McpTool;
import ir.dotin.platform.pangaea.ai.mcp.api.McpToolParam;
import ir.dotin.platform.pangaea.dispatcher.api.dispatcher.QueryDispatcher;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.DisbursementMethod;
import ir.dotin.loan.trade.core.application.query.loanarrangement.dto.LoanArrangementQueryResult;
import ir.dotin.loan.trade.core.application.query.loanarrangement.dto.TradeLoanArrangementQueryDto;
import ir.dotin.loan.trade.core.application.query.loanarrangement.request.GetLoanArrangementByIdQuery;
import ir.dotin.loan.trade.core.application.query.loanarrangement.request.LoanTypeArrangementFilterQuery;

import lombok.RequiredArgsConstructor;

/**
 * Read-only MCP business tools over the <b>loan arrangement</b> aggregate (the terms/conditions template a facility is
 * originated against). Mirrors the REST {@code LoanArrangementQueryController}: builds the same query records and
 * returns the same DTOs through {@link QueryDispatcher}.
 */
@McpBusinessTool
@Component
@RequiredArgsConstructor
public class LoanArrangementMcpTools {

    private final QueryDispatcher dispatcher;

    @McpTool(
            name = "loan_get_arrangement",
            description =
                    "Fetch a single loan arrangement (a loan terms/conditions definition) by its UUID. Returns the full"
                            + " arrangement projection: code, title, currency, economic sector, amount bounds,"
                            + " disbursement method, and active/disabled flags. Use loan_search_arrangements to discover"
                            + " an id first.")
    public TradeLoanArrangementQueryDto loanGetArrangement(
            @McpToolParam(name = "loanArrangementId", description = "The loan arrangement UUID.")
                    UUID loanArrangementId) {
        GetLoanArrangementByIdQuery query = GetLoanArrangementByIdQuery.builder()
                .loanArrangementId(loanArrangementId)
                .build();
        return dispatcher.dispatch(query);
    }

    @McpTool(
            name = "loan_search_arrangements",
            description =
                    "Search loan arrangements by optional filters with offset (page-number) pagination. Any subset of"
                            + " filters may be supplied; omitted filters are ignored. Returns the matching page plus the"
                            + " total element count. Filter by code, title, currency, economic sector, amount range,"
                            + " active/disabled state, or disbursement method.")
    public LoanArrangementQueryResult loanSearchArrangements(
            @McpToolParam(
                            name = "code",
                            description = "Arrangement code; digits only (English numerals).",
                            required = false)
                    @Nullable
                    String code,
            @McpToolParam(name = "title", description = "Free-text title filter.", required = false) @Nullable
                    String title,
            @McpToolParam(name = "currencyType", description = "Currency type filter.", required = false) @Nullable
                    String currencyType,
            @McpToolParam(name = "economicSector", description = "Economic-sector filter.", required = false) @Nullable
                    String economicSector,
            @McpToolParam(
                            name = "minAmount",
                            description = "Lower bound (inclusive) of the arrangement amount; positive decimal.",
                            required = false)
                    @Nullable
                    BigDecimal minAmount,
            @McpToolParam(
                            name = "maxAmount",
                            description =
                                    "Upper bound (inclusive) of the arrangement amount; must not be below minAmount.",
                            required = false)
                    @Nullable
                    BigDecimal maxAmount,
            @McpToolParam(
                            name = "active",
                            description = "Restrict to active (true) or inactive (false) arrangements.",
                            required = false)
                    @Nullable
                    Boolean active,
            @McpToolParam(
                            name = "disable",
                            description = "Restrict to disabled (true) or enabled (false) arrangements.",
                            required = false)
                    @Nullable
                    Boolean disable,
            @McpToolParam(
                            name = "disbursementMethod",
                            description = "Restrict to one disbursement method (e.g. LUMP_SUM, REGULAR, IRREGULAR).",
                            required = false)
                    @Nullable
                    DisbursementMethod disbursementMethod,
            @McpToolParam(name = "page", description = "Zero-based page index. Defaults to 0.", required = false)
                    @Nullable
                    Integer page,
            @McpToolParam(name = "pageSize", description = "Page size, 1..100. Defaults to 20.", required = false)
                    @Nullable
                    Integer pageSize) {

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
                McpPaging.page(page),
                McpPaging.pageSize(pageSize));
        return dispatcher.dispatch(query);
    }
}
