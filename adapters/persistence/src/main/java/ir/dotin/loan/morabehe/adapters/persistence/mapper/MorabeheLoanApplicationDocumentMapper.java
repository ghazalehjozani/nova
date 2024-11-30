package ir.dotin.loan.morabehe.adapters.persistence.mapper;

import ir.dotin.loan.baseloan.adapters.persistence.mapper.BaseLoanApplicationDocumentMapper;
import ir.dotin.loan.morabehe.adapters.persistence.document.MorabeheLoanApplicationDocument;
import ir.dotin.loan.morabehe.core.domain.loanapplication.entity.application.LoanApplication;
import ir.dotin.loan.morabehe.core.domain.loanapplication.entity.application.LoanApplication.LoanApplicationBuilder;
import ir.dotin.loan.morabehe.core.domain.loanapplication.entity.application.MorabeheLoanApplication;
import ir.dotin.loan.morabehe.core.domain.loanapplication.valueobject.MorabeheLoanApplicationId;
import ir.dotin.platform.ddd.common.interaction.feature.FeatureConfig;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class MorabeheLoanApplicationDocumentMapper extends
        BaseLoanApplicationDocumentMapper<LoanApplication, LoanApplicationBuilder> {


    public MorabeheLoanApplication mapToAggregate(MorabeheLoanApplicationDocument loanApplicationDocument) {
        if (loanApplicationDocument == null) {
            return null;
        }
        var builder = new LoanApplicationBuilder(new FeatureConfig(Map.of("feat1", false)));
        var morabeheLoanApplicationId = new MorabeheLoanApplicationId(UUID.fromString(loanApplicationDocument.getId()));
        super.mapFromDocument(loanApplicationDocument.getLoanApplication(), builder);
        return new MorabeheLoanApplication(morabeheLoanApplicationId, builder.validateAndBuild());
    }

    public MorabeheLoanApplicationDocument mapToDocument(MorabeheLoanApplication loanApplication,
                                                         MorabeheLoanApplicationDocument document) {
        if (loanApplication == null || document == null) {
            return null;
        }
        document.setId(loanApplication.getId().value().toString());
        var baseLoanApplicationDocument = super.mapToDocument(loanApplication.getLoanApplication());
        document.setLoanApplication(baseLoanApplicationDocument);
        return document;

    }

    public MorabeheLoanApplicationDocument mapToDocument(MorabeheLoanApplication loanApplication) {
        return mapToDocument(loanApplication, new MorabeheLoanApplicationDocument());
    }


}
