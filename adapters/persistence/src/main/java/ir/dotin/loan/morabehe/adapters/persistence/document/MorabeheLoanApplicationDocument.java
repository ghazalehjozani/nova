package ir.dotin.loan.morabehe.adapters.persistence.document;

import ir.dotin.loan.baseloan.adapters.persistence.document.AuditableDocument;
import ir.dotin.loan.baseloan.adapters.persistence.document.loanaplication.BaseLoanApplicationDocument;
import ir.dotin.loan.baseloan.domain.shared.valueobject.Money;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document("morabehe_loan_application")
public class MorabeheLoanApplicationDocument extends AuditableDocument {

    @Field("loan_application")
    private BaseLoanApplicationDocument loanApplication;

    @Field("prepayment_amount")
    private Money prePaymentAmount;

    @Field("prepayment_deposit_number")
    private String prePaymentDepositNumber;


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

    public Money getPrePaymentAmount() {
        return prePaymentAmount;
    }

    public void setPrePaymentAmount(Money prePaymentAmount) {
        this.prePaymentAmount = prePaymentAmount;
    }

    public String getPrePaymentDepositNumber() {
        return prePaymentDepositNumber;
    }

    public void setPrePaymentDepositNumber(String prePaymentDepositNumber) {
        this.prePaymentDepositNumber = prePaymentDepositNumber;
    }
}
