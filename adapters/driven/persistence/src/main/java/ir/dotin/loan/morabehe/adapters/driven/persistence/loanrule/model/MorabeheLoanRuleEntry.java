package ir.dotin.loan.morabehe.adapters.driven.persistence.loanrule.model;

import org.springframework.data.mongodb.core.mapping.Document;

import ir.dotin.loan.baseloan.adapters.driven.persistence.loanrule.model.BaseLoanRuleEntry;
import ir.dotin.loan.baseloan.adapters.driven.persistence.model.AuditableEntry;

@Document("morabehe_loan_rule")
public class MorabeheLoanRuleEntry extends AuditableEntry {

    private BaseLoanRuleEntry loanRule;

    public MorabeheLoanRuleEntry() {}

    public MorabeheLoanRuleEntry(String id, BaseLoanRuleEntry loanRule) {
        super(id);
        this.loanRule = loanRule;
    }

    public BaseLoanRuleEntry getLoanRule() {
        return loanRule;
    }

    public void setLoanRule(BaseLoanRuleEntry loanRule) {
        this.loanRule = loanRule;
    }
}
