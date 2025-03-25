package ir.dotin.loan.morabehe.core.domain.config.exception;

import java.util.List;

import ir.dotin.platform.ddd.common.exception.DomainError;
import ir.dotin.loan.baseloan.domain.config.exception.LoanRuleException;

public final class MorabeheLoanRuleValidationException extends LoanRuleException {

    public MorabeheLoanRuleValidationException(List<DomainError> errors) {
        super(errors);
    }

    public MorabeheLoanRuleValidationException(List<DomainError> errors, Throwable cause) {
        super(errors, cause);
    }

    public MorabeheLoanRuleValidationException(String messageKey, String fieldName, String... args) {
        super(messageKey, fieldName, args);
    }

    public MorabeheLoanRuleValidationException(String messageKey, Throwable cause, String fieldName, String... args) {
        super(messageKey, cause, fieldName, args);
    }
}
