package ir.dotin.loan.morabehe.adapters.persistence.mapper;

import ir.dotin.loan.baseloan.adapters.persistence.mapper.BaseLoanRuleDocumentMapper;
import ir.dotin.loan.morabehe.adapters.persistence.document.MorabeheLoanRuleDocument;
import ir.dotin.loan.morabehe.core.domain.config.entity.loanrule.LoanRule;
import ir.dotin.loan.morabehe.core.domain.config.entity.loanrule.LoanRule.LoanRuleBuilder;
import ir.dotin.loan.morabehe.core.domain.config.entity.loanrule.MorabeheLoanRule;
import ir.dotin.loan.morabehe.core.domain.config.valueobject.MorabeheLoanRuleId;
import ir.dotin.platform.ddd.common.interaction.feature.FeatureConfig;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class MorabeheLoanRuleDocumentMapper extends
        BaseLoanRuleDocumentMapper<LoanRule, LoanRuleBuilder> {

    public MorabeheLoanRuleDocument mapToDocument(MorabeheLoanRule loanRule) {
        if (loanRule == null) {
            return null;
        }

        MorabeheLoanRuleDocument morabeheLoanRuleDocument = new MorabeheLoanRuleDocument();
        morabeheLoanRuleDocument.setId(loanRule.id().value().toString());
        var baseLoanRuleDocument = super.mapToDocument(loanRule.getLoanRule());
        morabeheLoanRuleDocument.setLoanRule(baseLoanRuleDocument);
        return morabeheLoanRuleDocument;
    }

    public MorabeheLoanRule mapToAggregate(MorabeheLoanRuleDocument loanRuleDocument) {
        if (loanRuleDocument == null) {
            return null;
        }

        var loanRuleBuilder = new LoanRuleBuilder(new FeatureConfig(Map.of("feat1", false)));
        var morabeheLoanRuleId = new MorabeheLoanRuleId(UUID.fromString(loanRuleDocument.getId()));
        super.mapFromDocument(loanRuleDocument.getLoanRule(), loanRuleBuilder);
        return new MorabeheLoanRule(morabeheLoanRuleId, loanRuleBuilder.validateAndBuild());
    }

    public MorabeheLoanRuleDocument updateDocument(MorabeheLoanRule loanRule,
                                                   MorabeheLoanRuleDocument loanRuleDocument) {
        if (loanRule == null || loanRuleDocument == null) {
            return null;
        }
        loanRuleDocument.setId(loanRule.id().value().toString());
        var baseLoanRuleDocument = super.mapToDocument(loanRule.getLoanRule());
        loanRuleDocument.setLoanRule(baseLoanRuleDocument);
        return loanRuleDocument;
    }


}
