import org.jspecify.annotations.NullMarked;

/**
 * Trade-loan outbound application ports — interfaces and DTOs that the domain drives toward external systems (account
 * service, customer service, deposit service, samat, loan service).
 *
 * <p>Declared {@code open} for reflection and module-level {@link NullMarked} for the NullAway gate.
 */
@NullMarked
open module ir.dotin.loan.trade.core.application.ports.outbound {
    requires transitive ir.dotin.loan.trade.core.domain;
    requires transitive ir.dotin.loan.baseloan.core.domain;
    requires transitive ir.dotin.platform.pangaea.commons.core;
    requires transitive ir.dotin.platform.pangaea.commons.domain;
    requires transitive ir.dotin.platform.accounting.document.api;
    requires org.jspecify;
    requires static jakarta.validation;

    exports ir.dotin.loan.trade.core.application.ports.outbound.client;
    exports ir.dotin.loan.trade.core.application.ports.outbound.client.accountservice;
    exports ir.dotin.loan.trade.core.application.ports.outbound.client.customerservice;
    exports ir.dotin.loan.trade.core.application.ports.outbound.client.depositservice;
    exports ir.dotin.loan.trade.core.application.ports.outbound.client.error;
    exports ir.dotin.loan.trade.core.application.ports.outbound.client.loanservice;
    exports ir.dotin.loan.trade.core.application.ports.outbound.client.request;
    exports ir.dotin.loan.trade.core.application.ports.outbound.client.response;
    exports ir.dotin.loan.trade.core.application.ports.outbound.client.samat;
    exports ir.dotin.loan.trade.core.application.ports.outbound.command.repository;
    exports ir.dotin.loan.trade.core.application.ports.outbound.query;
}
