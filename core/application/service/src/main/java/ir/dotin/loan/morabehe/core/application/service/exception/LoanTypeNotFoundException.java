package ir.dotin.loan.morabehe.core.application.service.exception;

import ir.dotin.loan.baseloan.core.application.service.exception.AggregateRootNotFoundException;
import ir.dotin.loan.morabehe.core.domain.config.valueobject.MorabeheLoanTypeId;

public class LoanTypeNotFoundException extends AggregateRootNotFoundException {

    public LoanTypeNotFoundException(MorabeheLoanTypeId loanTypeId) {
        super(loanTypeId);
    }

}
