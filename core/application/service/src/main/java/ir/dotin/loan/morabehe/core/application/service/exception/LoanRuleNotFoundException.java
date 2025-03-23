package ir.dotin.loan.morabehe.core.application.service.exception;

import ir.dotin.loan.baseloan.core.application.service.exception.AggregateRootNotFoundException;
import ir.dotin.loan.morabehe.core.domain.config.valueobject.MorabeheLoanRuleId;

public class LoanRuleNotFoundException extends AggregateRootNotFoundException {

    public LoanRuleNotFoundException(MorabeheLoanRuleId loanRuleId) {
        super(loanRuleId);
    }
}
