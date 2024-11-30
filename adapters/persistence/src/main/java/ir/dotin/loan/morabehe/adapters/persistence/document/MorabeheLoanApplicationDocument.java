package ir.dotin.loan.morabehe.adapters.persistence.document;

import ir.dotin.loan.baseloan.adapters.persistence.document.AuditableDocument;
import ir.dotin.loan.baseloan.adapters.persistence.document.loanaplication.BaseLoanApplicationDocument;
import java.util.UUID;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document("morabehe_loan_application")
public class MorabeheLoanApplicationDocument extends AuditableDocument {

    @Field("loan_application")
    private BaseLoanApplicationDocument loanApplication;

    @Field("loan_type_id")
    private String loanTypeId;

    @Field("loan_rule_id")
    private String loanRuleId;

    public MorabeheLoanApplicationDocument() {
    }

    public MorabeheLoanApplicationDocument(String id,
                                           BaseLoanApplicationDocument loanApplication) {
        super(id);
        this.loanApplication = loanApplication;
    }

    public BaseLoanApplicationDocument getLoanApplication() {
        return loanApplication;
    }

    public void setLoanApplication(BaseLoanApplicationDocument loanApplication) {
        this.loanApplication = loanApplication;
    }

    public String getLoanTypeId() {
        return loanTypeId;
    }

    public void setLoanTypeId(String loanTypeId) {
        this.loanTypeId = loanTypeId;
    }

    public String getLoanRuleId() {
        return loanRuleId;
    }

    public void setLoanRuleId(String loanRuleId) {
        this.loanRuleId = loanRuleId;
    }

}
