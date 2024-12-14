package ir.dotin.loan.morabehe.adapters.persistence.mapper;

import ir.dotin.loan.baseloan.adapters.persistence.mapper.BaseLoanApplicationDocumentMapper;
import ir.dotin.loan.morabehe.adapters.persistence.document.MorabeheLoanApplicationDocument;
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
public class MorabeheLoanApplicationDocumentMapper extends
        BaseLoanApplicationDocumentMapper<LoanApplication, LoanApplicationBuilder> {


    public MorabeheLoanApplication mapToAggregate(MorabeheLoanApplicationDocument loanApplicationDocument) {
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

    public MorabeheLoanApplicationDocument mapToDocument(MorabeheLoanApplication loanApplication,
                                                         MorabeheLoanApplicationDocument document) {
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

    public MorabeheLoanApplicationDocument mapToDocument(MorabeheLoanApplication loanApplication) {
        return mapToDocument(loanApplication, new MorabeheLoanApplicationDocument());
    }

    public MorabeheLoanApplicationDocument updateDocument(MorabeheLoanApplication loanApplication,
                                                   MorabeheLoanApplicationDocument loanApplicationDocument) {
        if (loanApplication == null || loanApplicationDocument == null) {
            return null;
        }
        loanApplicationDocument.setId(loanApplication.id().value().toString());
        var baseLoanApplicationDocument = super.mapToDocument(loanApplication.loanApplication());
        loanApplicationDocument.setLoanApplication(baseLoanApplicationDocument);
        return loanApplicationDocument;
    }


}
