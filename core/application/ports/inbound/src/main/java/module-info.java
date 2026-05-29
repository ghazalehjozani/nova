import org.jspecify.annotations.NullMarked;

/**
 * Trade-loan inbound application ports — commands, queries and DTOs that driving adapters use to invoke the application
 * core.
 *
 * <p>Declared {@code open} for reflection and module-level {@link NullMarked} for the NullAway gate.
 */
@NullMarked
open module ir.dotin.loan.trade.core.application.ports.inbound {
    requires transitive ir.dotin.loan.trade.core.domain;
    requires transitive ir.dotin.loan.baseloan.core.domain;
    requires transitive ir.dotin.platform.pangaea.commons.core;
    requires transitive ir.dotin.platform.pangaea.commons.domain;
    requires transitive ir.dotin.platform.pangaea.dispatcher.api;
    requires org.jspecify;
    requires static lombok;
    requires static jakarta.validation;
    requires jakarta.annotation;

    exports ir.dotin.loan.trade.core.application.ports.inbound.command;
    exports ir.dotin.loan.trade.core.application.ports.inbound.dto;
    exports ir.dotin.loan.trade.core.application.ports.inbound.query;
}
