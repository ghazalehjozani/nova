package ir.dotin.loan.morabehe.adapters.persistence.mapper;

import ir.dotin.loan.baseloan.adapters.persistence.mapper.BaseLoanApplicationDocumentMapper;
import ir.dotin.loan.morabehe.adapters.persistence.document.MorabeheLoanApplicationDocument;
import ir.dotin.loan.morabehe.core.domain.loanapplication.entity.application.LoanApplication;
import ir.dotin.loan.morabehe.core.domain.loanapplication.entity.application.LoanApplication.LoanApplicationBuilder;
import ir.dotin.loan.morabehe.core.domain.loanapplication.entity.application.MorabeheLoanApplication;
import ir.dotin.loan.morabehe.core.domain.loanapplication.valueobject.MorabeheLoanApplicationId;
import ir.dotin.platform.ddd.common.interaction.feature.FeatureConfig;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class MorabeheLoanApplicationDocumentMapper extends
        BaseLoanApplicationDocumentMapper<LoanApplication, LoanApplicationBuilder> {

    public MorabeheLoanApplicationDocument mapToDocument(MorabeheLoanApplication loanApplication) {
        if (loanApplication == null) {
            return null;
        }

        MorabeheLoanApplicationDocument morabeheLoanApplicationDocument = new MorabeheLoanApplicationDocument();
        morabeheLoanApplicationDocument.setId(loanApplication.getId().value());
        var baseLoanApplicationDocument = super.mapToDocument(loanApplication.getLoanApplication());
        morabeheLoanApplicationDocument.setLoanApplicationDocument(baseLoanApplicationDocument);
        return morabeheLoanApplicationDocument;
    }

    public MorabeheLoanApplication mapToAggregate(
            MorabeheLoanApplicationDocument loanApplicationDocument) {
        if (loanApplicationDocument == null) {
            return null;
        }
        var loanApplicationBuilder = new LoanApplicationBuilder(
                new FeatureConfig(Map.of("feat1", false)));
        var morabeheLoanApplicationId = new MorabeheLoanApplicationId(
                loanApplicationDocument.getId());
        super.mapFromDocument(loanApplicationDocument.getLoanApplicationDocument(),
                              loanApplicationBuilder);
        return new MorabeheLoanApplication(morabeheLoanApplicationId,
                                           loanApplicationBuilder.validateAndBuild());
    }

    public MorabeheLoanApplicationDocument updateDocument(MorabeheLoanApplication loanApplication,
                                                          MorabeheLoanApplicationDocument loanApplicationDocument) {
        if (loanApplication == null || loanApplicationDocument == null) {
            return null;
        }
        loanApplicationDocument.setId(loanApplication.getId().value());
        var baseLoanApplicationDocument = super.mapToDocument(loanApplication.getLoanApplication());
        loanApplicationDocument.setLoanApplicationDocument(baseLoanApplicationDocument);
        return loanApplicationDocument;

    }

}
