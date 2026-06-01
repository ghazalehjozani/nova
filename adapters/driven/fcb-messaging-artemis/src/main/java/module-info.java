import org.jspecify.annotations.NullMarked;

/**
 * Trade-loan FCB outbound <em>Artemis</em> adapter — implements the transport-neutral {@code FcbRequestReplyClient}
 * seam (from {@code fcb-messaging-contract}) over an ActiveMQ Artemis JMS request/reply corridor.
 *
 * <p>Per request: build the polymorphic FCB request JSON, stamp the pinned JMS string properties (operation, auth,
 * signed actor envelope, deadlines, trace), create a JMS <em>temporary</em> reply queue as {@code JMSReplyTo} so the
 * broker delivers the reply only to this originating instance (no partition lease), and synchronously
 * {@code receive(timeout)} the reply. Error routing mirrors the Kafka client via the contract's
 * {@code KafkaErrorCodeMapper} + {@code CoreBankingErrors}. No Kafka types; never references
 * {@code fcb-messaging-kafka}.
 *
 * <p>Declared {@code open} for Spring config binding and {@link NullMarked} for the NullAway gate.
 */
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
