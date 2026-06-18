import org.jspecify.annotations.NullMarked;

/**
 * Trade-loan driving-adapter contract — external request/message DTOs and MapStruct mappers that translate legacy wire
 * terms into inbound {@code *Command} types from {@code core/application/ports/inbound}.
 *
 * <p>Declared {@code open} for reflection (Spring component-scan, MapStruct, Jackson) and module-level
 * {@link NullMarked} for the NullAway gate.
 */
@NullMarked
open module ir.dotin.loan.trade.adapters.driving.contract {
    requires transitive ir.dotin.loan.trade.core.application.ports.inbound;
    requires transitive ir.dotin.loan.baseloan.core.domain;
    requires ir.dotin.platform.pangaea.protocol.api;
    requires ir.dotin.platform.pangaea.messaging.api;
    requires transitive expression.kit.service;
    requires com.fasterxml.jackson.annotation;
    requires net.time4j.base;
    requires io.swagger.v3.oas.annotations;
    requires org.mapstruct;
    requires spring.context;
    requires spring.beans;
    requires org.jspecify;
    requires static lombok;
    requires static jakarta.annotation;
    requires static jakarta.validation;

    exports ir.dotin.loan.trade.adapters.driving.contract.dto;
    exports ir.dotin.loan.trade.adapters.driving.contract.mapper;
}
