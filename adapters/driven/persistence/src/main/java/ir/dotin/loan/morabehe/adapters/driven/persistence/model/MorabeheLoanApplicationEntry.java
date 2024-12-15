package ir.dotin.loan.morabehe.adapters.driven.persistence.model;

import ir.dotin.loan.baseloan.adapters.driven.persistence.loanapplication.model.BaseLoanApplicationEntry;
import ir.dotin.loan.baseloan.adapters.driven.persistence.model.AuditableEntry;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document("morabehe_loan_application")
public class MorabeheLoanApplicationEntry extends AuditableEntry {

    @Field("loan_application")
    private BaseLoanApplicationEntry loanApplication;

    @Field("loan_type_id")
    private String loanTypeId;

    @Field("loan_rule_id")
    private String loanRuleId;

    public MorabeheLoanApplicationEntry() {
    }

    public MorabeheLoanApplicationEntry(String id,
                                        BaseLoanApplicationEntry loanApplication) {
        super(id);
        this.loanApplication = loanApplication;
    }

    public BaseLoanApplicationEntry getLoanApplication() {
        return loanApplication;
    }

    public void setLoanApplication(BaseLoanApplicationEntry loanApplication) {
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
