import org.jspecify.annotations.NullMarked;

/**
 * Trade-loan reconciliation driven adapter — implements the pangaea reconciliation SPI ({@code ReconciliationSource},
 * {@code DivergenceProbe}, {@code ConvergenceAction}) for the {@code facility-state} reconciliation type, comparing
 * Nova facility status against FCB's legacy loan-file state and converging by re-driving stored outbox/inbox rows.
 *
 * <p>This is the ONLY place the reconciliation SPI is implemented on the Nova side — keeping it out of
 * {@code core/application/service} so the application layer never touches admin ports or the FCB adapter (ArchUnit
 * {@code applicationShouldNotDependOnAdapters}). Declared {@code open} for Spring reflection and module-level
 * {@link NullMarked} for the NullAway gate.
 */
@NullMarked
open module ir.dotin.loan.trade.adapters.driven.reconciliation {
    requires transitive ir.dotin.loan.trade.core.application.ports.outbound;
    requires transitive ir.dotin.loan.baseloan.core.domain;
    requires ir.dotin.platform.pangaea.commons.core;
    requires ir.dotin.platform.pangaea.reconciliation.api;
    requires ir.dotin.platform.pangaea.outbox.api;
    requires ir.dotin.platform.pangaea.inbox.api;
    requires ir.dotin.platform.pangaea.saga.api;
    requires spring.context;
    requires spring.beans;
    requires org.slf4j;
    requires org.jspecify;
    requires static lombok;

    exports ir.dotin.loan.trade.adapters.driven.reconciliation;
}
