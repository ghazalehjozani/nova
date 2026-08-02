import org.jspecify.annotations.NullMarked;

/**
 * Trade-loan JPA + outbox + Redis persistence adapter — implements outbound command repositories, query repositories,
 * outbox handlers, and Redis cache configuration for the trade-loan bounded context.
 *
 * <p>Declared {@code open} for reflection (Spring, Hibernate, MapStruct, Jackson) and module-level {@link NullMarked}
 * for the NullAway gate.
 */
@NullMarked
open module ir.dotin.loan.trade.adapters.driven.persistence {
    requires transitive ir.dotin.loan.trade.core.application.ports.outbound;
    requires transitive ir.dotin.loan.trade.core.application.query;
    requires transitive ir.dotin.loan.trade.core.domain;
    requires transitive ir.dotin.loan.baseloan.core.domain;
    requires transitive ir.dotin.platform.pangaea.commons.core;
    requires transitive ir.dotin.platform.pangaea.commons.domain;
    requires ir.dotin.platform.pangaea.persistence.jpa;
    requires ir.dotin.platform.pangaea.outbox.jpa;
    requires ir.dotin.platform.pangaea.outbox.api;
    requires ir.dotin.platform.accounting.document.api;
    requires ir.dotin.platform.formula.infrastructure;
    requires spring.context;
    requires spring.beans;
    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.boot.actuator;
    requires spring.data.jpa;
    requires spring.data.commons;
    requires spring.data.redis;
    requires spring.tx;
    requires spring.context.support;
    requires org.hibernate.orm.core;
    requires jakarta.persistence;
    requires com.fasterxml.jackson.annotation;
    requires tools.jackson.databind;
    requires tools.jackson.core;
    requires spring.boot.cache;
    requires com.google.common;
    requires com.github.benmanes.caffeine;
    requires org.slf4j;
    requires org.mapstruct;
    requires org.jspecify;
    requires static lombok;
    requires static jakarta.validation;

    exports ir.dotin.loan.trade.adapters.driven.persistence.config;
    exports ir.dotin.loan.trade.adapters.driven.persistence.embdeddable;
    exports ir.dotin.loan.trade.adapters.driven.persistence.installmentschedule;
    exports ir.dotin.loan.trade.adapters.driven.persistence.installmentschedule.entity;
    exports ir.dotin.loan.trade.adapters.driven.persistence.installmentschedule.mapper;
    exports ir.dotin.loan.trade.adapters.driven.persistence.installmentschedule.query;
    exports ir.dotin.loan.trade.adapters.driven.persistence.installmentschedule.query.mapper;
    exports ir.dotin.loan.trade.adapters.driven.persistence.installmentschedule.repository;
    exports ir.dotin.loan.trade.adapters.driven.persistence.loanarrangement;
    exports ir.dotin.loan.trade.adapters.driven.persistence.loanarrangement.entity;
    exports ir.dotin.loan.trade.adapters.driven.persistence.loanarrangement.mapper;
    exports ir.dotin.loan.trade.adapters.driven.persistence.loanarrangement.projection;
    exports ir.dotin.loan.trade.adapters.driven.persistence.loanarrangement.query;
    exports ir.dotin.loan.trade.adapters.driven.persistence.loanarrangement.query.mapper;
    exports ir.dotin.loan.trade.adapters.driven.persistence.loanarrangement.repository;
    exports ir.dotin.loan.trade.adapters.driven.persistence.loanfacility;
    exports ir.dotin.loan.trade.adapters.driven.persistence.loanfacility.entity;
    exports ir.dotin.loan.trade.adapters.driven.persistence.loanfacility.mapper;
    exports ir.dotin.loan.trade.adapters.driven.persistence.loanfacility.projection;
    exports ir.dotin.loan.trade.adapters.driven.persistence.loanfacility.query;
    exports ir.dotin.loan.trade.adapters.driven.persistence.loanfacility.query.mapper;
    exports ir.dotin.loan.trade.adapters.driven.persistence.loanfacility.repository;
    exports ir.dotin.loan.trade.adapters.driven.persistence.loantype;
    exports ir.dotin.loan.trade.adapters.driven.persistence.loantype.entity;
    exports ir.dotin.loan.trade.adapters.driven.persistence.loantype.mapper;
    exports ir.dotin.loan.trade.adapters.driven.persistence.loantype.projection;
    exports ir.dotin.loan.trade.adapters.driven.persistence.loantype.query;
    exports ir.dotin.loan.trade.adapters.driven.persistence.loantype.query.mapper;
    exports ir.dotin.loan.trade.adapters.driven.persistence.loantype.repository;
    exports ir.dotin.loan.trade.adapters.driven.persistence.loantypegroup;
    exports ir.dotin.loan.trade.adapters.driven.persistence.loantypegroup.entity;
    exports ir.dotin.loan.trade.adapters.driven.persistence.loantypegroup.mapper;
    exports ir.dotin.loan.trade.adapters.driven.persistence.loantypegroup.query;
    exports ir.dotin.loan.trade.adapters.driven.persistence.loantypegroup.repository;
    exports ir.dotin.loan.trade.adapters.driven.persistence.mapper;
    exports ir.dotin.loan.trade.adapters.driven.persistence.shared.query;
}
