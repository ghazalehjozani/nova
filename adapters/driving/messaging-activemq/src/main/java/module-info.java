import org.jspecify.annotations.NullMarked;

/**
 * Trade-loan ActiveMQ/JMS driving adapter — {@code @JmsListener} consumer that translates inbound JMS text messages
 * into {@code *Command} objects and dispatches them through the platform flow-command machinery.
 *
 * <p>Declared {@code open} for reflection (Spring component-scan, Jackson deserialization) and module-level
 * {@link NullMarked} for the NullAway gate.
 */
@NullMarked
open module ir.dotin.loan.trade.adapters.driving.messaging.activemq {
    requires transitive ir.dotin.loan.trade.adapters.driving.contract;
    requires ir.dotin.platform.pangaea.messaging.api;
    requires ir.dotin.platform.pangaea.messaging.core;
    requires ir.dotin.platform.pangaea.messaging.activemq;
    requires jakarta.messaging;
    requires spring.context;
    requires spring.beans;
    requires spring.jms;
    requires tools.jackson.databind;
    requires org.jspecify;
    requires static lombok;
    requires org.slf4j;

    exports ir.dotin.loan.trade.adapters.driving.messaging.activemq.config;
    exports ir.dotin.loan.trade.adapters.driving.messaging.activemq.consumer;
}
