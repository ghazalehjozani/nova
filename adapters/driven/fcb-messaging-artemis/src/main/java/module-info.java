import org.jspecify.annotations.NullMarked;

@NullMarked
open module ir.dotin.loan.trade.adapters.driven.fcbmessaging.artemis {
    requires transitive ir.dotin.loan.trade.adapters.driven.fcbmessaging.contract;
    requires transitive ir.dotin.loan.trade.core.application.ports.outbound;
    requires transitive ir.dotin.platform.pangaea.commons.core;
    requires ir.dotin.platform.pangaea.commons.security;
    requires ir.dotin.platform.pangaea.envelope.api;
    requires jakarta.messaging;
    requires artemis.jakarta.client;
    requires spring.context;
    requires spring.beans;
    requires spring.core;
    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires tools.jackson.databind;
    requires tools.jackson.core;
    requires micrometer.core;
    requires micrometer.tracing;
    requires java.management;
    requires org.slf4j;
    requires org.jspecify;
    requires static lombok;
    requires static jakarta.annotation;
    requires static jakarta.validation;

    exports ir.dotin.loan.trade.adapters.driven.fcbmessaging.artemis.config;
    exports ir.dotin.loan.trade.adapters.driven.fcbmessaging.artemis.client;
}
