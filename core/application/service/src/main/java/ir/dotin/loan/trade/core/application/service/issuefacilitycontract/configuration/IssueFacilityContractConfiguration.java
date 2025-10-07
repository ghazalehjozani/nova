package ir.dotin.loan.trade.core.application.service.issuefacilitycontract.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "loan.trade.issue-facility-contract")
public record IssueFacilityContractConfiguration(String postTitleTemplate) {

    public IssueFacilityContractConfiguration {
        if (postTitleTemplate == null || postTitleTemplate.isBlank()) {
//            throw new IllegalArgumentException("Post title template cannot be null or blank");
        }
    }

    public static IssueFacilityContractConfiguration defaults() {
        return new IssueFacilityContractConfiguration("Issue Contract - Facility: %s");
    }
}
