package ir.dotin.loan.trade.core.application.service.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import ir.dotin.loan.trade.core.application.service.originateloanfacility.strategy.ApplicationNumberGenerationType;

@ConfigurationProperties(prefix = "loan.trade.application-number")
public record TradeApplicationNumberConfiguration(
        @DefaultValue("FCB_VALIDATION") ApplicationNumberGenerationType generationType) {}
