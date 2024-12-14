package ir.dotin.loan.morabehe.core.application.service.exception;

import ir.dotin.loan.baseloan.core.application.service.exception.AggregateRootNotFoundException;
import ir.dotin.loan.morabehe.core.domain.loanapplication.valueobject.MorabeheLoanApplicationId;

public class LoanApplicationNotFoundException extends AggregateRootNotFoundException {

    public LoanApplicationNotFoundException(MorabeheLoanApplicationId loanApplicationId) {
        super(loanApplicationId);
    }

}
