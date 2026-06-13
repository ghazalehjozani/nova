import org.jspecify.annotations.NullMarked;

/**
 * Trade-loan MCP driving adapter — exposes read-only business tools (the {@code loan_*} tool set) over the Model
 * Context Protocol. Each tool method is a thin shim: it builds an existing
 * {@link ir.dotin.platform.pangaea.servicelayer.api.query.Query Query} record, dispatches it through the platform
 * {@link ir.dotin.platform.pangaea.servicelayer.api.dispatcher.QueryDispatcher QueryDispatcher}, and returns the
 * existing {@code Trade*QueryDto} / {@code *QueryResult} verbatim — the same contracts the REST query controllers
 * serve.
 *
 * <p>Tool beans carry the inert {@link ir.dotin.platform.pangaea.ai.mcp.api.McpBusinessTool} marker; the
 * {@code pangaea-ai-mcp-server} spec factory (a separate module this adapter never depends on) discovers them
 * reflectively, builds the JSON input schema from {@link ir.dotin.platform.pangaea.ai.mcp.api.McpToolParam}-annotated
 * parameters, and registers them with the business stateless server only.
 *
 * <p>This module depends on the query slice and the dispatcher — never on outbound ports, the command side, the MCP
 * transport runtime, or sibling driving adapters (ArchUnit enforces this in {@code architecture-tests}). Declared
 * {@code open} for Spring component-scan and module-level {@link NullMarked} for the NullAway gate.
 */
@NullMarked
open module ir.dotin.loan.trade.adapters.driving.mcp {
    requires transitive ir.dotin.loan.trade.core.application.query;
    requires ir.dotin.loan.baseloan.core.domain;
    requires ir.dotin.platform.pangaea.ai.mcp.api;
    requires ir.dotin.platform.pangaea.reconciliation.management;
    requires ir.dotin.platform.pangaea.reconciliation.api;
    requires ir.dotin.platform.pangaea.servicelayer.api;
    requires spring.context;
    requires spring.beans;
    requires org.jspecify;
    requires static lombok;

    exports ir.dotin.loan.trade.adapters.driving.mcp;
}
