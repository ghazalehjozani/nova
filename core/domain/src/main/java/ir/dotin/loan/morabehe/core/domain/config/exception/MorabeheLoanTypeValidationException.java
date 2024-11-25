package ir.dotin.loan.morabehe.core.domain.config.exception;

import ir.dotin.platform.ddd.common.exception.AggregateValidationException;
import ir.dotin.platform.ddd.common.exception.ValidationError;
import java.util.List;

public final class MorabeheLoanTypeValidationException extends AggregateValidationException {

    public MorabeheLoanTypeValidationException(
            List<ValidationError> errors) {
        super(errors);
    }

    public MorabeheLoanTypeValidationException(List<ValidationError> errors, Throwable cause) {
        super(errors, cause);
    }

    public MorabeheLoanTypeValidationException(String messageKey, String fieldName, Object... args) {
        super(messageKey, fieldName, args);
    }

    public MorabeheLoanTypeValidationException(String messageKey, Throwable cause, String fieldName,
                                               Object... args) {
        super(messageKey, cause, fieldName, args);
    }
}
