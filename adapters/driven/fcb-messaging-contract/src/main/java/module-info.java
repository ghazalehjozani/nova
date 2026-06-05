import org.jspecify.annotations.NullMarked;

/**
 * Trade-loan FCB messaging <em>contract</em> — transport-neutral seam shared by every FCB transport adapter.
 *
 * <p>Holds the wire DTOs (polymorphic {@code FcbBaseRequest}/{@code FcbBaseResponse} keyed on
 * {@code operationName}), the domain⇄wire mappers, the FCB error/exception taxonomy, the request/reply abstraction
 * {@code FcbRequestReplyClient}, and the port-implementing adapters that delegate to it. Carries <strong>zero</strong>
 * Kafka / JMS / broker types — the transport lives in {@code fcb-messaging-kafka} and {@code fcb-messaging-artemis}.
 *
 * <p>Declared {@code open} for Spring/Jackson reflection and {@link NullMarked} for the NullAway gate.
 */
@NullMarked
open module ir.dotin.loan.trade.adapters.driven.fcbmessaging.contract {
    requires transitive ir.dotin.loan.trade.core.application.ports.outbound;
    requires transitive ir.dotin.loan.trade.core.domain;
    requires transitive ir.dotin.loan.baseloan.core.domain;
    requires transitive ir.dotin.platform.pangaea.commons.core;
    requires transitive ir.dotin.platform.pangaea.commons.domain;
    requires ir.dotin.platform.pangaea.commons.security;
    requires ir.dotin.platform.accounting.document.api;
    requires spring.context;
    requires spring.beans;
    requires spring.core;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.databind;
    requires micrometer.core;
    requires org.slf4j;
    requires org.jspecify;
    requires static lombok;
    requires static jakarta.annotation;
    requires static jakarta.validation;

    exports ir.dotin.loan.trade.adapters.driven.fcbmessaging.client;
    exports ir.dotin.loan.trade.adapters.driven.fcbmessaging.adapter;
    exports ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto;
    exports ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply;
    exports ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request;
    exports ir.dotin.loan.trade.adapters.driven.fcbmessaging.exception;
    exports ir.dotin.loan.trade.adapters.driven.fcbmessaging.mapper;
}
