package ir.dotin.loan.morabehe.adapters.persistence.document;

import ir.dotin.loan.baseloan.adapters.persistence.document.AuditableDocument;
import ir.dotin.loan.baseloan.adapters.persistence.document.loanaplication.BaseLoanApplicationDocument;
import java.util.UUID;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("morabehe_loan_application")
public class MorabeheLoanApplicationDocument extends AuditableDocument {

    private BaseLoanApplicationDocument loanApplicationDocument;


    public MorabeheLoanApplicationDocument() {
    }

    public MorabeheLoanApplicationDocument(UUID id,
                                           BaseLoanApplicationDocument loanApplicationDocument) {
        super(id);
        this.loanApplicationDocument = loanApplicationDocument;
    }

    public BaseLoanApplicationDocument getLoanApplicationDocument() {
        return loanApplicationDocument;
    }

    public void setLoanApplicationDocument(
            BaseLoanApplicationDocument loanApplicationDocument) {
        this.loanApplicationDocument = loanApplicationDocument;
    }
}
