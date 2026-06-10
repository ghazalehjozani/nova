import org.jspecify.annotations.NullMarked;

/**
 * Trade-loan Kafka driving adapter — {@code @KafkaListener} consumers that translate inbound Kafka records into
 * {@code *Command} objects and dispatch them through the platform Inbox or flow-command machinery.
 *
 * <p>Declared {@code open} for reflection (Spring component-scan, Jackson deserialization) and module-level
 * {@link NullMarked} for the NullAway gate.
 */
@NullMarked
open module ir.dotin.loan.trade.adapters.driving.messaging.kafka {
    requires transitive ir.dotin.loan.trade.adapters.driving.contract;
    requires ir.dotin.platform.pangaea.messaging.api;
    requires ir.dotin.platform.pangaea.messaging.core;
    requires ir.dotin.platform.pangaea.inbox.api;
    requires ir.dotin.platform.pangaea.inbox.core;
    requires ir.dotin.platform.pangaea.messaging.kafka;
    requires ir.dotin.platform.pangaea.servicelayer.api;
    requires ir.dotin.platform.pangaea.commons.core;
    requires ir.dotin.platform.pangaea.commons.security;
    requires spring.context;
    requires spring.beans;
    requires spring.kafka;
    requires kafka.clients;
    requires tools.jackson.databind;
    requires micrometer.tracing;
    requires net.time4j.base;
    requires org.jspecify;
    requires static lombok;
    requires org.slf4j;
    requires spring.boot.autoconfigure;

    exports ir.dotin.loan.trade.adapters.driving.messaging.kafka.consumer;
    exports ir.dotin.loan.trade.adapters.driving.messaging.kafka.consumer.handler;
}
