package ir.dotin.loan.trade.core.application.service.issuefacilitycontract.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "loan.trade.issue-facility-contract")
public record IssueFacilityContractConfiguration(
        @DefaultValue("Issue Contract - Facility: %s") String postTitleTemplate) {}
