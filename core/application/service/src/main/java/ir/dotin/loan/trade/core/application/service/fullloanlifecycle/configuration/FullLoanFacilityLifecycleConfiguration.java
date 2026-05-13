package ir.dotin.loan.trade.core.application.service.fullloanlifecycle.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@Data
@ConfigurationProperties(prefix = "loan.trade.full-lifecycle")
public class FullLoanFacilityLifecycleConfiguration {
    private String contractPostTitleTemplate = "Contract Issue - Facility: %s";
    private String disbursementPostTitleTemplate = "Disbursement %s - Facility: %s";
    private long defaultTimeoutMs = 300000L;
    private int maxRetries = 3;
}
