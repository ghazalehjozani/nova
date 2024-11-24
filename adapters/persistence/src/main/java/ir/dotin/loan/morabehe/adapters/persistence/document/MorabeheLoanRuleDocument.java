package ir.dotin.loan.morabehe.adapters.persistence.document;

import ir.dotin.loan.baseloan.adapters.persistence.document.loanrule.BaseLoanRuleDocument;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("morabehe_loan_rule")
public class MorabeheLoanRuleDocument {

    @Id
    private UUID id;

    private BaseLoanRuleDocument loanRule;

    public MorabeheLoanRuleDocument() {
    }

    public MorabeheLoanRuleDocument(UUID id, BaseLoanRuleDocument loanRule) {
        this.id = id;
        this.loanRule = loanRule;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public BaseLoanRuleDocument getLoanRule() {
        return loanRule;
    }

    public void setLoanRule(BaseLoanRuleDocument loanRule) {
        this.loanRule = loanRule;
    }
}
