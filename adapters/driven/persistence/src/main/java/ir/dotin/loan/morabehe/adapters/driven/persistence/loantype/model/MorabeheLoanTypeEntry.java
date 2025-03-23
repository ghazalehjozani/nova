package ir.dotin.loan.morabehe.adapters.driven.persistence.loantype.model;

import java.util.Set;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import ir.dotin.loan.baseloan.adapters.driven.persistence.loantype.model.BaseLoanTypeEntry;
import ir.dotin.loan.baseloan.adapters.driven.persistence.model.AuditableEntry;

@Document("morabehe_loan_type")
public class MorabeheLoanTypeEntry extends AuditableEntry {

    @Field("loan_type")
    private BaseLoanTypeEntry loanType;

    @Field("has_issue_merchandise_document")
    private Boolean hasIssueMerchandiseDocument;

    @Field("loan_rule_ids")
    private Set<String> loanRuleIds;

    public MorabeheLoanTypeEntry() {}

    public MorabeheLoanTypeEntry(String id, BaseLoanTypeEntry loanType) {
        super(id);
        this.loanType = loanType;
    }

    public BaseLoanTypeEntry getLoanType() {
        return loanType;
    }

    public void setLoanType(BaseLoanTypeEntry loanType) {
        this.loanType = loanType;
    }

    public Boolean getHasIssueMerchandiseDocument() {
        return hasIssueMerchandiseDocument;
    }

    public void setHasIssueMerchandiseDocument(Boolean hasIssueMerchandiseDocument) {
        this.hasIssueMerchandiseDocument = hasIssueMerchandiseDocument;
    }

    public Set<String> getLoanRuleIds() {
        return loanRuleIds;
    }

    public void setLoanRuleIds(Set<String> loanRuleIds) {
        this.loanRuleIds = loanRuleIds;
    }
}
