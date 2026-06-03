package ir.dotin.loan.trade.adapters.driving.mcp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import ir.dotin.platform.pangaea.ai.mcp.api.McpBusinessTool;
import ir.dotin.platform.pangaea.ai.mcp.api.McpTool;
import ir.dotin.platform.pangaea.ai.mcp.api.McpToolParam;
import ir.dotin.platform.pangaea.dispatcher.api.dispatcher.QueryDispatcher;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.FacilityStatus;
import ir.dotin.loan.trade.core.application.query.loanfacility.dto.LoanFacilityQueryResult;
import ir.dotin.loan.trade.core.application.query.loanfacility.dto.TradeFacilityQueryDto;
import ir.dotin.loan.trade.core.application.query.loanfacility.request.FindAllLoanFacilitiesQuery;
import ir.dotin.loan.trade.core.application.query.loanfacility.request.GetFacilityByIdQuery;
import ir.dotin.loan.trade.core.application.query.loanfacility.request.LoanFacilityFilterQuery;

import lombok.RequiredArgsConstructor;

/**
 * Read-only MCP business tools over the <b>loan facility</b> aggregate. Every method builds the same query record the
 * REST {@code FacilityQueryController} builds and dispatches it through {@link QueryDispatcher}, returning the existing
 * query DTO/result verbatim. No write tools live here — the business endpoint is read-only by scope.
 *
 * <p>The {@code pangaea-ai-mcp-server} spec factory wraps each call (serialization, central never-throw error
 * envelope); these methods therefore neither catch nor wrap — they dispatch and return.
 */
@McpBusinessTool
@Component
@RequiredArgsConstructor
public class LoanFacilityMcpTools {

    private final QueryDispatcher dispatcher;

    @McpTool(
            name = "loan_get_facility",
            description =
                    "Fetch a single loan facility (a Morabehe trade-loan application/contract) by its UUID. Returns the"
                            + " full facility projection: status, customer number, loan type, requested/approved amounts,"
                            + " parties, collaterals, disbursements and timestamps. Use when the user references one"
                            + " specific facility id; use loan_search_facilities to find an id first.")
    public TradeFacilityQueryDto loanGetFacility(
            @McpToolParam(name = "facilityId", description = "The loan facility UUID.") UUID facilityId) {
        GetFacilityByIdQuery query =
                GetFacilityByIdQuery.builder().loanFacilityId(facilityId).build();
        return dispatcher.dispatch(query);
    }

    @McpTool(
            name = "loan_search_facilities",
            description =
                    "Search loan facilities by optional filters with offset (page-number) pagination. Any subset of"
                            + " filters may be supplied; omitted filters are ignored. Returns the matching page plus the"
                            + " total element count and current page so the caller can page through results. Prefer this"
                            + " over loan_list_facilities when filtering by customer, loan type, status, date range or"
                            + " amount range.")
    public LoanFacilityQueryResult loanSearchFacilities(
            @McpToolParam(
                            name = "loanTypeId",
                            description = "Restrict to facilities of this loan-type UUID.",
                            required = false)
                    @Nullable
                    UUID loanTypeId,
            @McpToolParam(
                            name = "customerNumber",
                            description = "Restrict to this customer number (exact match).",
                            required = false)
                    @Nullable
                    String customerNumber,
            @McpToolParam(
                            name = "createDateFrom",
                            description = "Lower bound (inclusive) of facility creation time, ISO-8601 local date-time"
                                    + " (e.g. 2026-01-31T00:00:00). Must not be in the future.",
                            required = false)
                    @Nullable
                    LocalDateTime createDateFrom,
            @McpToolParam(
                            name = "createDateTo",
                            description = "Upper bound (inclusive) of facility creation time, ISO-8601 local date-time."
                                    + " Must not be before createDateFrom.",
                            required = false)
                    @Nullable
                    LocalDateTime createDateTo,
            @McpToolParam(
                            name = "requestAmountMin",
                            description = "Lower bound (inclusive) of the requested amount; positive decimal.",
                            required = false)
                    @Nullable
                    BigDecimal requestAmountMin,
            @McpToolParam(
                            name = "requestAmountMax",
                            description =
                                    "Upper bound (inclusive) of the requested amount; must not be below requestAmountMin.",
                            required = false)
                    @Nullable
                    BigDecimal requestAmountMax,
            @McpToolParam(
                            name = "status",
                            description = "Restrict to one facility status (e.g. DRAFT, APPROVED, DISBURSED, CLOSED).",
                            required = false)
                    @Nullable
                    FacilityStatus status,
            @McpToolParam(name = "page", description = "Zero-based page index. Defaults to 0.", required = false)
                    @Nullable
                    Integer page,
            @McpToolParam(name = "pageSize", description = "Page size, 1..100. Defaults to 20.", required = false)
                    @Nullable
                    Integer pageSize) {

        LoanFacilityFilterQuery query = LoanFacilityFilterQuery.of(
                loanTypeId,
                customerNumber,
                createDateFrom,
                createDateTo,
                requestAmountMin,
                requestAmountMax,
                status,
                McpPaging.page(page),
                McpPaging.pageSize(pageSize));
        return dispatcher.dispatch(query);
    }

    @McpTool(
            name = "loan_list_facilities",
            description =
                    "List loan facilities with cursor-based pagination, newest first. Call with no cursor for the first"
                            + " page; pass the returned nextCursor to fetch the following page. Returns the facilities"
                            + " plus nextCursor/previousCursor and a hasNext flag. Use for plain browsing; use"
                            + " loan_search_facilities when you need to filter.")
    public LoanFacilityQueryResult loanListFacilities(
            @McpToolParam(
                            name = "cursor",
                            description = "Opaque pagination cursor from a previous response; omit for the first page.",
                            required = false)
                    @Nullable
                    String cursor,
            @McpToolParam(name = "pageSize", description = "Page size, 1..100. Defaults to 20.", required = false)
                    @Nullable
                    Integer pageSize) {

        FindAllLoanFacilitiesQuery query = FindAllLoanFacilitiesQuery.builder()
                .cursor(cursor)
                .pageSize(McpPaging.pageSize(pageSize))
                .build();
        return dispatcher.dispatch(query);
    }
}
