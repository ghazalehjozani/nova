package ir.dotin.loan.morabehe.core.application.service.exception;

import ir.dotin.loan.baseloan.application.service.exception.NotFoundException;
import ir.dotin.loan.morabehe.core.domain.config.valueobject.MorabeheLoanTypeId;

public class LoanTypeNotFoundException extends NotFoundException {

    public LoanTypeNotFoundException(MorabeheLoanTypeId loanTypeId) {
        super(loanTypeId.value());
    }

}
