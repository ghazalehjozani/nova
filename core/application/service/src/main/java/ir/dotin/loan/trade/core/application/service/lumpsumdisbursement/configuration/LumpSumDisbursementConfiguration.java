package ir.dotin.loan.trade.core.application.service.lumpsumdisbursement.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@Data
@ConfigurationProperties(prefix = "loan.trade.lump-sum-disbursement")
public class LumpSumDisbursementConfiguration {
    private String postTitleTemplate = "Lump Sum Disbursement - Facility: %s";
    private String fcbMergedDocumentTitle = "Lump Sum Disbursement";
}
