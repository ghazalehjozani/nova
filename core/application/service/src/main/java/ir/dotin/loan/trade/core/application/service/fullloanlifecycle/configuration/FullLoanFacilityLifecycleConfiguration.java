package ir.dotin.loan.trade.core.application.service.fullloanlifecycle.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "loan.trade.full-lifecycle")
public record FullLoanFacilityLifecycleConfiguration(
        @DefaultValue("Contract Issue - Facility: %s") String contractPostTitleTemplate,
        @DefaultValue("Disbursement %s - Facility: %s") String disbursementPostTitleTemplate,
        @DefaultValue("300000") long defaultTimeoutMs,
        @DefaultValue("3") int maxRetries) {}
