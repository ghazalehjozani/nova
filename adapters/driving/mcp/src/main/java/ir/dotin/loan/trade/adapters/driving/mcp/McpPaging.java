package ir.dotin.loan.trade.adapters.driving.mcp;

import org.jspecify.annotations.Nullable;

/**
 * Applies the same default paging values the REST query controllers get for free from Spring's
 * {@code @RequestParam(defaultValue = ...)}. Over MCP there is no Spring parameter binding, so an LLM that omits
 * {@code page}/{@code pageSize} would otherwise send {@code 0} — which fails the query records' {@code @Min(1)} bound
 * on page size. Centralizing the fallbacks here keeps every tool's paging behavior identical to its REST sibling (page
 * defaults to 0, page size defaults to 20).
 */
final class McpPaging {

    static final int DEFAULT_PAGE = 0;
    static final int DEFAULT_PAGE_SIZE = 20;

    private McpPaging() {
        throw new UnsupportedOperationException("Utility class");
    }

    /** Zero-based page index, defaulting to {@value #DEFAULT_PAGE} when the LLM omits it. */
    static int page(@Nullable Integer page) {
        return page == null ? DEFAULT_PAGE : page;
    }

    /** Page size, defaulting to {@value #DEFAULT_PAGE_SIZE} when the LLM omits it. Bounds are enforced downstream. */
    static int pageSize(@Nullable Integer pageSize) {
        return pageSize == null ? DEFAULT_PAGE_SIZE : pageSize;
    }
}
