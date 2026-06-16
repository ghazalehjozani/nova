import org.jspecify.annotations.NullMarked;

/**
 * Trade-loan query application layer — query objects, DTOs, repository interfaces and handlers for read-side
 * operations.
 *
 * <p>Declared {@code open} for reflection and module-level {@link NullMarked} for the NullAway gate.
 */
@NullMarked
open module ir.dotin.loan.trade.core.application.query {
    requires transitive ir.dotin.loan.trade.core.application.ports.inbound;
    requires transitive ir.dotin.loan.trade.core.application.ports.outbound;
    requires transitive ir.dotin.loan.trade.core.domain;
    requires transitive ir.dotin.loan.baseloan.core.domain;
    requires transitive ir.dotin.platform.pangaea.commons.core;
    requires ir.dotin.platform.pangaea.protocol.projection.api;
    requires transitive ir.dotin.platform.pangaea.servicelayer.api;
    requires transitive ir.dotin.platform.pangaea.servicelayer.cache;
    requires ir.dotin.platform.accounting.document.api;
    requires tools.jackson.databind;
    requires com.fasterxml.jackson.annotation;
    requires org.jspecify;
    requires static lombok;
    requires static jakarta.validation;
    requires jakarta.annotation;
    requires spring.beans;
    requires spring.context;

    exports ir.dotin.loan.trade.core.application.query.installmentschedule.dto;
    exports ir.dotin.loan.trade.core.application.query.installmentschedule.handler;
    exports ir.dotin.loan.trade.core.application.query.installmentschedule.i18n;
    exports ir.dotin.loan.trade.core.application.query.installmentschedule.repository;
    exports ir.dotin.loan.trade.core.application.query.installmentschedule.request;
    exports ir.dotin.loan.trade.core.application.query.loanarrangement.dto;
    exports ir.dotin.loan.trade.core.application.query.loanarrangement.handler;
    exports ir.dotin.loan.trade.core.application.query.loanarrangement.i18n;
    exports ir.dotin.loan.trade.core.application.query.loanarrangement.repository;
    exports ir.dotin.loan.trade.core.application.query.loanarrangement.request;
    exports ir.dotin.loan.trade.core.application.query.loanfacility.dto;
    exports ir.dotin.loan.trade.core.application.query.loanfacility.handler;
    exports ir.dotin.loan.trade.core.application.query.loanfacility.i18n;
    exports ir.dotin.loan.trade.core.application.query.loanfacility.repository;
    exports ir.dotin.loan.trade.core.application.query.loanfacility.request;
    exports ir.dotin.loan.trade.core.application.query.loantype.dto;
    exports ir.dotin.loan.trade.core.application.query.loantype.handler;
    exports ir.dotin.loan.trade.core.application.query.loantype.i18n;
    exports ir.dotin.loan.trade.core.application.query.loantype.repository;
    exports ir.dotin.loan.trade.core.application.query.loantype.request;
    exports ir.dotin.loan.trade.core.application.query.shared.exception;
    exports ir.dotin.loan.trade.core.application.query.shared.pagination;
}
