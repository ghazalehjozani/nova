package ir.dotin.loan.trade.core.application.service.configuration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({TradeLoanFormulaFieldMappingProperties.class, TradeFeatureProperties.class})
public class TradeLoanMappingConfiguration {}
