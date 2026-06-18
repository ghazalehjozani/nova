package ir.dotin.loan.trade.core.application.service.configuration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import ir.dotin.loan.trade.core.application.service.issuefacilitycontract.configuration.IssueFacilityContractConfiguration;

@Configuration
@EnableConfigurationProperties({
    TradeFeatureProperties.class,
    FormulaCorridorProperties.class,
    IssueFacilityContractConfiguration.class
})
public class TradeLoanMappingConfiguration {}
