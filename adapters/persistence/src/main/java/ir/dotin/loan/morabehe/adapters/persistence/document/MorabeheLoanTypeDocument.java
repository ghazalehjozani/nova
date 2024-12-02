package ir.dotin.loan.morabehe.adapters.persistence.document;


import ir.dotin.loan.baseloan.adapters.persistence.document.AuditableDocument;
import ir.dotin.loan.baseloan.adapters.persistence.document.loantype.BaseLoanTypeDocument;
import java.util.Set;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document("morabehe_loan_type")
public class MorabeheLoanTypeDocument extends AuditableDocument {

    @Field("loan_type")
    private BaseLoanTypeDocument loanType;
    @Field("has_issue_merchandise_document")
    private Boolean hasIssueMerchandiseDocument;
    @Field("loan_rule_ids")
    private Set<String> loanRuleIds;

    public MorabeheLoanTypeDocument() {
    }

    public MorabeheLoanTypeDocument(String id, BaseLoanTypeDocument loanType) {
        super(id);
        this.loanType = loanType;
    }

    public BaseLoanTypeDocument getLoanType() {
        return loanType;
    }

    public void setLoanType(
            BaseLoanTypeDocument loanType) {
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
