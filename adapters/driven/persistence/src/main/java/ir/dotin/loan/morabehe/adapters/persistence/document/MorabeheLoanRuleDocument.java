package ir.dotin.loan.morabehe.adapters.persistence.document;

import ir.dotin.loan.baseloan.adapters.persistence.document.AuditableDocument;
import ir.dotin.loan.baseloan.adapters.persistence.document.loanrule.BaseLoanRuleDocument;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("morabehe_loan_rule")
public class MorabeheLoanRuleDocument extends AuditableDocument {

    private BaseLoanRuleDocument loanRule;

    public MorabeheLoanRuleDocument() {
    }

    public MorabeheLoanRuleDocument(String id, BaseLoanRuleDocument loanRule) {
        super(id);
        this.loanRule = loanRule;
    }

    public BaseLoanRuleDocument getLoanRule() {
        return loanRule;
    }

    public void setLoanRule(BaseLoanRuleDocument loanRule) {
        this.loanRule = loanRule;
    }
}
