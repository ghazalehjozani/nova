import org.jspecify.annotations.NullMarked;

/**
 * Trade-loan REST driving adapter — Spring MVC controllers, OpenAPI/Swagger configuration, API versioning, and the
 * dev-only OAuth2 callback proxy that translate HTTP requests into inbound {@code *Command} and query types.
 *
 * <p>Declared {@code open} for reflection (Spring component-scan, Jackson serialization) and module-level
 * {@link NullMarked} for the NullAway gate.
 */
@NullMarked
open module ir.dotin.loan.trade.adapters.driving.rest {
    requires transitive ir.dotin.loan.trade.adapters.driving.contract;
    requires transitive ir.dotin.loan.trade.core.application.query;
    requires ir.dotin.loan.baseloan.core.domain;
    requires ir.dotin.platform.pangaea.protocol.rest;
    requires ir.dotin.platform.pangaea.protocol.api;
    requires ir.dotin.platform.pangaea.servicelayer.api;
    requires ir.dotin.platform.pangaea.commons.security;
    requires ir.dotin.platform.pangaea.security.oauth2;
    requires spring.context;
    requires spring.beans;
    requires spring.web;
    requires spring.webmvc;
    requires spring.core;
    requires spring.boot;
    requires io.swagger.v3.oas.annotations;
    requires io.swagger.v3.oas.models;
    requires org.springdoc.openapi.common;
    requires org.springdoc.openapi.ui;
    requires jakarta.servlet;
    requires static jakarta.validation;
    requires com.fasterxml.jackson.databind;
    requires tools.jackson.databind;
    requires micrometer.tracing;
    requires org.jspecify;
    requires static lombok;
    requires org.slf4j;

    exports ir.dotin.loan.trade.adapters.driving.rest.auth;
    exports ir.dotin.loan.trade.adapters.driving.rest.command.controller;
    exports ir.dotin.loan.trade.adapters.driving.rest.config;
    exports ir.dotin.loan.trade.adapters.driving.rest.filter;
    exports ir.dotin.loan.trade.adapters.driving.rest.query.installmentschedule;
    exports ir.dotin.loan.trade.adapters.driving.rest.query.loanarrangement;
    exports ir.dotin.loan.trade.adapters.driving.rest.query.loanfacility;
    exports ir.dotin.loan.trade.adapters.driving.rest.query.loantype;
}
