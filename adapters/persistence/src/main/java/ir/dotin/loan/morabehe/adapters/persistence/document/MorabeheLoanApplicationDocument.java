package ir.dotin.loan.morabehe.adapters.persistence.document;

import ir.dotin.loan.baseloan.adapters.persistence.document.AuditableDocument;
import ir.dotin.loan.baseloan.adapters.persistence.document.loanaplication.BaseLoanApplicationDocument;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document("morabehe_loan_application")
public class MorabeheLoanApplicationDocument extends AuditableDocument {

    @Field("loan_application")
    private BaseLoanApplicationDocument loanApplication;


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

}
