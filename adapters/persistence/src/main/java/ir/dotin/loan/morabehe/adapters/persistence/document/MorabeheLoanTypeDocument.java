package ir.dotin.loan.morabehe.adapters.persistence.document;


import ir.dotin.loan.baseloan.adapters.persistence.document.AuditableDocument;
import ir.dotin.loan.baseloan.adapters.persistence.document.loantype.BaseLoanTypeDocument;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Document("morabehe_loan_type")
public class MorabeheLoanTypeDocument extends AuditableDocument {

    private BaseLoanTypeDocument loanType;

    public MorabeheLoanTypeDocument() {
    }

    public MorabeheLoanTypeDocument(UUID id, BaseLoanTypeDocument loanType) {
        super(id);
        this.loanType = loanType;
    }

    public BaseLoanTypeDocument getLoanType() {
        return loanType;
    }

    public void setLoanType(BaseLoanTypeDocument loanType) {
        this.loanType = loanType;
    }
}
