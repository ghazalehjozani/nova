import org.jspecify.annotations.NullMarked;

/**
 * Trade-loan FCB outbound Kafka adapter — implements the outbound client ports against the legacy FCB core over a Kafka
 * request/reply corridor, plus the active health-probe stack and wire DTOs.
 *
 * <p>Declared {@code open} for reflection (Spring config binding, Jackson serialization) and module-level
 * {@link NullMarked} for the NullAway gate.
 */
@NullMarked
open module ir.dotin.loan.trade.adapters.driven.fcbmessaging {
    requires transitive ir.dotin.loan.trade.adapters.driven.fcbmessaging.contract;
    requires transitive ir.dotin.loan.trade.core.application.ports.outbound;
    requires transitive ir.dotin.loan.trade.core.domain;
    requires transitive ir.dotin.loan.baseloan.core.domain;
    requires transitive ir.dotin.platform.pangaea.commons.core;
    requires transitive ir.dotin.platform.pangaea.commons.domain;
    requires ir.dotin.platform.pangaea.commons.security;
    requires ir.dotin.platform.pangaea.envelope.api;
    requires ir.dotin.platform.pangaea.messaging.autoconfigure;
    requires ir.dotin.platform.pangaea.messaging.kafka;
    requires ir.dotin.platform.accounting.document.api;
    requires spring.context;
    requires spring.beans;
    requires spring.core;
    requires spring.tx;
    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.boot.actuator;
    requires spring.boot.health;
    requires spring.kafka;
    requires kafka.clients;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.databind;
    requires tools.jackson.databind;
    requires java.management;
    requires micrometer.core;
    requires micrometer.tracing;
    requires org.slf4j;
    requires org.jspecify;
    requires static lombok;
    requires static jakarta.annotation;
    requires static jakarta.validation;

    exports ir.dotin.loan.trade.adapters.driven.fcbmessaging;
    exports ir.dotin.loan.trade.adapters.driven.fcbmessaging.config;
    exports ir.dotin.loan.trade.adapters.driven.fcbmessaging.health;
    exports ir.dotin.loan.trade.adapters.driven.fcbmessaging.metrics;
    exports ir.dotin.loan.trade.adapters.driven.fcbmessaging.service;
    exports ir.dotin.loan.trade.adapters.driven.fcbmessaging.util;
}
