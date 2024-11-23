package ir.dotin.loan.morabehe.adapters.persistence.mapper;

import ir.dotin.loan.morabehe.adapters.persistence.document.MorabeheLoanRuleDocument;
import ir.dotin.loan.morabehe.core.domain.config.entity.loanrule.LoanRule;
import ir.dotin.loan.morabehe.core.domain.config.entity.loanrule.MorabeheLoanRule;
import ir.dotin.loan.morabehe.core.domain.config.valueobject.MorabeheLoanRuleId;
import org.springframework.stereotype.Component;

@Component
public class MorabeheLoanRuleDocumentMapper extends BaseLoanRuleDocumentMapper {

    public MorabeheLoanRuleDocument mapToDocument(MorabeheLoanRule loanRule) {
        if (loanRule == null) {
            return null;
        }

        MorabeheLoanRuleDocument morabeheLoanRuleDocument = new MorabeheLoanRuleDocument();
        morabeheLoanRuleDocument.setId(loanRule.getId().id());
        morabeheLoanRuleDocument.setLoanRule(mapBaseLoanRule(loanRule.getLoanRule()));
        return morabeheLoanRuleDocument;
    }

    public MorabeheLoanRule mapToAggregate(MorabeheLoanRuleDocument loanRuleDocument) {
        if (loanRuleDocument == null) {
            return null;
        }

        MorabeheLoanRuleId morabeheLoanRuleId = new MorabeheLoanRuleId(loanRuleDocument.getId());
        LoanRule.LoanRuleBuilder loanRuleBuilder = createLoanRuleBuilder(
                loanRuleDocument.getLoanRule());

        return new MorabeheLoanRule(morabeheLoanRuleId, loanRuleBuilder.validateAndBuild());
    }
}
