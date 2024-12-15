package ir.dotin.loan.morabehe.adapters.driven.persistence.mapper;

import ir.dotin.loan.baseloan.adapters.driven.persistence.loanrule.mapper.BaseLoanRuleEntryMapper;
import ir.dotin.loan.morabehe.adapters.driven.persistence.model.MorabeheLoanRuleEntry;
import ir.dotin.loan.morabehe.core.domain.config.entity.loanrule.LoanRule;
import ir.dotin.loan.morabehe.core.domain.config.entity.loanrule.LoanRule.LoanRuleBuilder;
import ir.dotin.loan.morabehe.core.domain.config.entity.loanrule.MorabeheLoanRule;
import ir.dotin.loan.morabehe.core.domain.config.valueobject.MorabeheLoanRuleId;
import ir.dotin.platform.ddd.common.interaction.feature.FeatureConfig;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class MorabeheLoanRuleEntryMapper extends
        BaseLoanRuleEntryMapper<LoanRule, LoanRuleBuilder> {

    public MorabeheLoanRuleEntry mapToDocument(MorabeheLoanRule loanRule) {
        if (loanRule == null) {
            return null;
        }

        MorabeheLoanRuleEntry morabeheLoanRuleDocument = new MorabeheLoanRuleEntry();
        morabeheLoanRuleDocument.setId(loanRule.id().value().toString());
        var baseLoanRuleDocument = super.mapToDocument(loanRule.getLoanRule());
        morabeheLoanRuleDocument.setLoanRule(baseLoanRuleDocument);
        return morabeheLoanRuleDocument;
    }

    public MorabeheLoanRule mapToAggregate(MorabeheLoanRuleEntry loanRuleDocument) {
        if (loanRuleDocument == null) {
            return null;
        }

        var loanRuleBuilder = new LoanRuleBuilder(new FeatureConfig(Map.of("feat1", false)));
        var morabeheLoanRuleId = new MorabeheLoanRuleId(UUID.fromString(loanRuleDocument.getId()));
        super.mapFromDocument(loanRuleDocument.getLoanRule(), loanRuleBuilder);
        return new MorabeheLoanRule(morabeheLoanRuleId, loanRuleBuilder.validateAndBuild());
    }

    public MorabeheLoanRuleEntry updateDocument(MorabeheLoanRule loanRule,
                                                MorabeheLoanRuleEntry loanRuleDocument) {
        if (loanRule == null || loanRuleDocument == null) {
            return null;
        }
        loanRuleDocument.setId(loanRule.id().value().toString());
        var baseLoanRuleDocument = super.mapToDocument(loanRule.getLoanRule());
        loanRuleDocument.setLoanRule(baseLoanRuleDocument);
        return loanRuleDocument;
    }


}
