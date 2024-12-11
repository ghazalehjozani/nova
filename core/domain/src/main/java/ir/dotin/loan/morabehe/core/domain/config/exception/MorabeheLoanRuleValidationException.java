package ir.dotin.loan.morabehe.core.domain.config.exception;

import ir.dotin.loan.baseloan.domain.config.exception.LoanRuleException;
import ir.dotin.platform.ddd.common.exception.DomainError;

import java.util.List;

public final class MorabeheLoanRuleValidationException extends LoanRuleException {


    public MorabeheLoanRuleValidationException(List<DomainError> errors) {
        super(errors);
    }

    public MorabeheLoanRuleValidationException(List<DomainError> errors, Throwable cause) {
        super(errors, cause);
    }

    public MorabeheLoanRuleValidationException(String messageKey, String fieldName, Object... args) {
        super(messageKey, fieldName, args);
    }

    public MorabeheLoanRuleValidationException(String messageKey, Throwable cause, String fieldName,
                                               Object... args) {
        super(messageKey, cause, fieldName, args);
    }
}
