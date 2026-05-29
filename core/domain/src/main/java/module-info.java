import org.jspecify.annotations.NullMarked;

/**
 * Trade-loan core domain module. Hexagonal DDD domain — no Spring, no HTTP.
 *
 * <p>Declared {@code open} so downstream adapters (persistence / Jackson) may reflect over entities/VOs at runtime, and
 * module-level {@link NullMarked} so the platform NullAway gate treats every package here as null-marked.
 */
@NullMarked
open module ir.dotin.loan.trade.core.domain {
    requires transitive ir.dotin.loan.baseloan.core.domain;
    requires transitive ir.dotin.platform.pangaea.commons.core;
    requires transitive ir.dotin.platform.pangaea.commons.domain;
    requires transitive ir.dotin.platform.accounting.document.api;
    requires ir.dotin.platform.accounting.document.core;
    requires ir.dotin.platform.formula.api;
    requires com.google.common;
    requires org.jspecify;

    exports ir.dotin.loan.trade.core.domain.installmentschedule.event;
    exports ir.dotin.loan.trade.core.domain.installmentschedule.intraction;
    exports ir.dotin.loan.trade.core.domain.installmentschedule.service;
    exports ir.dotin.loan.trade.core.domain.loanarrangement.entity;
    exports ir.dotin.loan.trade.core.domain.loanarrangement.event;
    exports ir.dotin.loan.trade.core.domain.loanfacility.entity;
    exports ir.dotin.loan.trade.core.domain.loanfacility.error;
    exports ir.dotin.loan.trade.core.domain.loanfacility.event;
    exports ir.dotin.loan.trade.core.domain.loanfacility.service;
    exports ir.dotin.loan.trade.core.domain.loanfacility.service.validator;
    exports ir.dotin.loan.trade.core.domain.loanfacility.specification;
    exports ir.dotin.loan.trade.core.domain.loanfacility.vo;
    exports ir.dotin.loan.trade.core.domain.loantype.entity;
    exports ir.dotin.loan.trade.core.domain.loantype.enums;
    exports ir.dotin.loan.trade.core.domain.loantype.event;
    exports ir.dotin.loan.trade.core.domain.loantype.service;
    exports ir.dotin.loan.trade.core.domain.loantype.specification;
    exports ir.dotin.loan.trade.core.domain.shared.document.builder;
    exports ir.dotin.loan.trade.core.domain.shared.document.config;
    exports ir.dotin.loan.trade.core.domain.shared.document.enums;
    exports ir.dotin.loan.trade.core.domain.shared.document.factory;
    exports ir.dotin.loan.trade.core.domain.shared.document.strategy;
    exports ir.dotin.loan.trade.core.domain.shared.document.strategy.impl;
    exports ir.dotin.loan.trade.core.domain.shared.document.transaction;
    exports ir.dotin.loan.trade.core.domain.shared.error;
    exports ir.dotin.loan.trade.core.domain.shared.formula;
    exports ir.dotin.loan.trade.core.domain.shared.intraction;
}
