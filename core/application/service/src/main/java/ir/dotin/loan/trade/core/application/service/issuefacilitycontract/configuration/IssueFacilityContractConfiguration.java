package ir.dotin.loan.trade.core.application.service.issuefacilitycontract.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@Data
@ConfigurationProperties(prefix = "loan.trade.issue-facility-contract")
public class IssueFacilityContractConfiguration {
    private String postTitleTemplate = "Issue Contract - Facility: %s";
}
