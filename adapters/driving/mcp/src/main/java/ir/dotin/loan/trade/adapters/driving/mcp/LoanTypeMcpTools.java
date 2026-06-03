package ir.dotin.loan.trade.adapters.driving.mcp;

import java.util.UUID;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import ir.dotin.platform.pangaea.ai.mcp.api.McpBusinessTool;
import ir.dotin.platform.pangaea.ai.mcp.api.McpTool;
import ir.dotin.platform.pangaea.ai.mcp.api.McpToolParam;
import ir.dotin.platform.pangaea.dispatcher.api.dispatcher.QueryDispatcher;
import ir.dotin.loan.trade.core.application.query.loantype.dto.LoanTypeQueryResult;
import ir.dotin.loan.trade.core.application.query.loantype.dto.TradeLoanTypeQueryDto;
import ir.dotin.loan.trade.core.application.query.loantype.request.GetLoanTypeByIdQuery;
import ir.dotin.loan.trade.core.application.query.loantype.request.LoanTypeFilterQuery;

import lombok.RequiredArgsConstructor;

/**
 * Read-only MCP business tools over the <b>loan type</b> aggregate (the product catalog entry a facility is opened
 * under). Mirrors the REST {@code LoanTypeQueryController}: builds the same query records and returns the same DTOs
 * through {@link QueryDispatcher}.
 */
@McpBusinessTool
@Component
@RequiredArgsConstructor
public class LoanTypeMcpTools {

    private final QueryDispatcher dispatcher;

    @McpTool(
            name = "loan_get_loan_type",
            description =
                    "Fetch a single loan type (a Morabehe product-catalog entry) by its UUID. Returns the loan-type"
                            + " projection: code, title, and configuration. Use loan_search_loan_types to discover an id"
                            + " first.")
    public TradeLoanTypeQueryDto loanGetLoanType(
            @McpToolParam(name = "loanTypeId", description = "The loan type UUID.") UUID loanTypeId) {
        GetLoanTypeByIdQuery query =
                GetLoanTypeByIdQuery.builder().loanTypeId(loanTypeId).build();
        return dispatcher.dispatch(query);
    }

    @McpTool(
            name = "loan_search_loan_types",
            description =
                    "Search loan types by code and/or title with offset (page-number) pagination. At least one of code"
                            + " or title MUST be supplied (a blank search is rejected). Returns the matching page plus"
                            + " the total element count.")
    public LoanTypeQueryResult loanSearchLoanTypes(
            @McpToolParam(
                            name = "code",
                            description = "Loan-type code; digits only (English numerals). Provide this and/or title.",
                            required = false)
                    @Nullable
                    String code,
            @McpToolParam(
                            name = "title",
                            description = "Free-text title filter. Provide this and/or code.",
                            required = false)
                    @Nullable
                    String title,
            @McpToolParam(name = "page", description = "Zero-based page index. Defaults to 0.", required = false)
                    @Nullable
                    Integer page,
            @McpToolParam(name = "pageSize", description = "Page size, 1..100. Defaults to 20.", required = false)
                    @Nullable
                    Integer pageSize) {

        LoanTypeFilterQuery query =
                LoanTypeFilterQuery.of(code, title, McpPaging.page(page), McpPaging.pageSize(pageSize));
        return dispatcher.dispatch(query);
    }
}
