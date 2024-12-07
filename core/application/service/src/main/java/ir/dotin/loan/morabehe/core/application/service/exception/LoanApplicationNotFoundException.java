package ir.dotin.loan.morabehe.core.application.service.exception;

import ir.dotin.loan.baseloan.application.service.exception.NotFoundException;
import ir.dotin.loan.morabehe.core.domain.loanapplication.valueobject.MorabeheLoanApplicationId;

public class LoanApplicationNotFoundException extends NotFoundException {

    public LoanApplicationNotFoundException(MorabeheLoanApplicationId loanApplicationId) {
        super(loanApplicationId.value());
    }

}
