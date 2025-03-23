package ir.dotin.loan.morabehe.core.domain.config.exception;

import java.util.List;

import ir.dotin.platform.ddd.common.exception.AggregateException;
import ir.dotin.platform.ddd.common.exception.DomainError;

public final class MorabeheLoanTypeValidationException extends AggregateException {

    public MorabeheLoanTypeValidationException(List<DomainError> errors) {
        super(errors);
    }

    public MorabeheLoanTypeValidationException(List<DomainError> errors, Throwable cause) {
        super(errors, cause);
    }

    public MorabeheLoanTypeValidationException(String messageKey, String fieldName, Object... args) {
        super(messageKey, fieldName, args);
    }

    public MorabeheLoanTypeValidationException(String messageKey, Throwable cause, String fieldName, Object... args) {
        super(messageKey, cause, fieldName, args);
    }
}
