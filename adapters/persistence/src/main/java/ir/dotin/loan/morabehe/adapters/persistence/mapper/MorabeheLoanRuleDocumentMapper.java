package ir.dotin.loan.morabehe.adapters.persistence.mapper;

import ir.dotin.loan.baseloan.adapters.persistence.mapper.BaseLoanRuleDocumentMapper;
import ir.dotin.loan.morabehe.adapters.persistence.document.MorabeheLoanRuleDocument;
import ir.dotin.loan.morabehe.core.domain.config.entity.loanrule.LoanRule;
import ir.dotin.loan.morabehe.core.domain.config.entity.loanrule.LoanRule.LoanRuleBuilder;
import ir.dotin.loan.morabehe.core.domain.config.entity.loanrule.MorabeheLoanRule;
import ir.dotin.loan.morabehe.core.domain.config.valueobject.MorabeheLoanRuleId;
import ir.dotin.platform.ddd.common.interaction.feature.FeatureConfig;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class MorabeheLoanRuleDocumentMapper extends
        BaseLoanRuleDocumentMapper<LoanRuleBuilder, LoanRule> {

    public MorabeheLoanRuleDocument mapToDocument(MorabeheLoanRule loanRule) {
        if (loanRule == null) {
            return null;
        }

        MorabeheLoanRuleDocument morabeheLoanRuleDocument = new MorabeheLoanRuleDocument();
        morabeheLoanRuleDocument.setId(loanRule.getId().value());
        var baseLoanRuleDocument = super.mapCommonPropertiesToDocument(loanRule.getLoanRule());
        morabeheLoanRuleDocument.setLoanRule(baseLoanRuleDocument);
        return morabeheLoanRuleDocument;
    }

    public MorabeheLoanRule mapToAggregate(MorabeheLoanRuleDocument loanRuleDocument) {
        if (loanRuleDocument == null) {
            return null;
        }

        var loanRuleBuilder = new LoanRuleBuilder(new FeatureConfig(Map.of("feat1", false)));
        var morabeheLoanRuleId = new MorabeheLoanRuleId(loanRuleDocument.getId());
        super.mapCommonPropertiesToBuilder(loanRuleDocument.getLoanRule(), loanRuleBuilder);
        return new MorabeheLoanRule(morabeheLoanRuleId, loanRuleBuilder.validateAndBuild());
    }
}
