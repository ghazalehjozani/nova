package ir.dotin.loan.morabehe.core.application.service.exception;

import ir.dotin.loan.baseloan.application.service.exception.NotFoundException;
import ir.dotin.loan.morabehe.core.domain.config.valueobject.MorabeheLoanRuleId;

public class LoanRuleNotFoundException extends NotFoundException {

    public LoanRuleNotFoundException(MorabeheLoanRuleId loanRuleId) {
        super(loanRuleId.value());
    }

}
