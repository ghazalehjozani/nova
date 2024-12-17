package ir.dotin.loan.morabehe.adapters.driven.persistence.loanapplication.mapper;

import ir.dotin.loan.baseloan.adapters.driven.persistence.loanapplication.mapper.BaseLoanApplicationEntryMapper;
import ir.dotin.loan.morabehe.adapters.driven.persistence.loanapplication.model.MorabeheLoanApplicationEntry;
import ir.dotin.loan.morabehe.core.domain.config.valueobject.MorabeheLoanRuleId;
import ir.dotin.loan.morabehe.core.domain.config.valueobject.MorabeheLoanTypeId;
import ir.dotin.loan.morabehe.core.domain.loanapplication.entity.application.LoanApplication;
import ir.dotin.loan.morabehe.core.domain.loanapplication.entity.application.LoanApplication.LoanApplicationBuilder;
import ir.dotin.loan.morabehe.core.domain.loanapplication.entity.application.MorabeheLoanApplication;
import ir.dotin.loan.morabehe.core.domain.loanapplication.valueobject.MorabeheLoanApplicationId;
import ir.dotin.platform.ddd.common.interaction.feature.FeatureConfig;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class MorabeheLoanApplicationEntryMapper extends
        BaseLoanApplicationEntryMapper<LoanApplication, LoanApplicationBuilder> {


    public MorabeheLoanApplication mapToAggregate(MorabeheLoanApplicationEntry loanApplicationDocument) {
        if (loanApplicationDocument == null) {
            return null;
        }
        var builder = new LoanApplicationBuilder(new FeatureConfig(Map.of("feat1", false)));
        var morabeheLoanApplicationId = new MorabeheLoanApplicationId(UUID.fromString(loanApplicationDocument.getId()));
        builder.withLoanRuleId(new MorabeheLoanRuleId(UUID.fromString(loanApplicationDocument.getLoanRuleId())));
        builder.withLoanTypeId(new MorabeheLoanTypeId(UUID.fromString(loanApplicationDocument.getLoanTypeId())));
        super.mapFromDocument(loanApplicationDocument.getLoanApplication(), builder);
        return new MorabeheLoanApplication(morabeheLoanApplicationId, builder.validateAndBuild());
    }

    public MorabeheLoanApplicationEntry mapToDocument(MorabeheLoanApplication loanApplication,
                                                      MorabeheLoanApplicationEntry document) {
        if (loanApplication == null || document == null) {
            return null;
        }
        document.setId(loanApplication.id().value().toString());
        document.setLoanRuleId(loanApplication.loanApplication().loanRuleId().value().toString());
        document.setLoanTypeId(loanApplication.loanApplication().loanTypeId().value().toString());
        var baseLoanApplicationDocument = super.mapToDocument(loanApplication.loanApplication());
        document.setLoanApplication(baseLoanApplicationDocument);
        return document;

    }

    public MorabeheLoanApplicationEntry mapToDocument(MorabeheLoanApplication loanApplication) {
        return mapToDocument(loanApplication, new MorabeheLoanApplicationEntry());
    }

    public MorabeheLoanApplicationEntry updateDocument(MorabeheLoanApplication loanApplication,
                                                       MorabeheLoanApplicationEntry loanApplicationDocument) {
        if (loanApplication == null || loanApplicationDocument == null) {
            return null;
        }
        loanApplicationDocument.setId(loanApplication.id().value().toString());
        var baseLoanApplicationDocument = super.mapToDocument(loanApplication.loanApplication());
        loanApplicationDocument.setLoanApplication(baseLoanApplicationDocument);
        return loanApplicationDocument;
    }


}
