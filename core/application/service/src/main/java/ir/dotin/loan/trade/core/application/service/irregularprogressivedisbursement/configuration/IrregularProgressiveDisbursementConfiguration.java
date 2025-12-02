package ir.dotin.loan.trade.core.application.service.irregularprogressivedisbursement.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "loan.trade.irregular-disbursement")
public record IrregularProgressiveDisbursementConfiguration(
        @DefaultValue("Irregular Disbursement - Facility: %s - Tranche: %d")
        String postTitleTemplate,

        @DefaultValue("Irregular Tranche Disbursement") String fcbMergedDocumentTitle) {}
