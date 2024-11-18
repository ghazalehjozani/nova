package ir.dotin.loan.core.domain.config.entity.exception;

import ir.dotin.loan.baseloan.domain.config.exception.LoanRuleException;
import ir.dotin.platform.ddd.common.exception.DomainError;

import java.util.List;

public final class MorabeheLoanRuleException extends LoanRuleException {


    public MorabeheLoanRuleException(List<DomainError> errors) {
        super(errors);
    }

    public MorabeheLoanRuleException(List<DomainError> errors, Throwable cause) {
        super(errors, cause);
    }

    public MorabeheLoanRuleException(String messageKey, Object... args) {
        super(messageKey, args);
    }

    public MorabeheLoanRuleException(String messageKey, Throwable cause, Object... args) {
        super(messageKey, cause, args);
    }
}
