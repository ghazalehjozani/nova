package ir.dotin.loan.trade.core.application.service.lumpsumdisbursement.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "loan.trade.lump-sum-disbursement")
public record LumpSumDisbursementConfiguration(
        @DefaultValue("Lump Sum Disbursement - Facility: %s") String postTitleTemplate) {}
