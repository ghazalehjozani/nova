package ir.dotin.loan.trade.core.application.service.irregularprogressivedisbursement.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@Data
@ConfigurationProperties(prefix = "loan.trade.irregular-disbursement")
public class IrregularProgressiveDisbursementConfiguration {
    private String postTitleTemplate = "Irregular Disbursement - Facility: %s - Tranche: %d";
    private String fcbMergedDocumentTitle = "Irregular Tranche Disbursement";
}
