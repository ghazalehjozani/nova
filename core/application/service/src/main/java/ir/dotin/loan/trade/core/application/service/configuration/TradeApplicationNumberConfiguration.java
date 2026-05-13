package ir.dotin.loan.trade.core.application.service.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import ir.dotin.loan.trade.core.application.service.originateloanfacility.strategy.ApplicationNumberGenerationType;

import lombok.Data;

@Data
@ConfigurationProperties(prefix = "loan.trade.application-number")
public class TradeApplicationNumberConfiguration {
    private ApplicationNumberGenerationType generationType = ApplicationNumberGenerationType.FCB_VALIDATION;
}
