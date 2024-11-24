package ir.dotin.loan.morabehe.core.domain.config.exception;

import ir.dotin.platform.ddd.common.exception.AggregateException;
import ir.dotin.platform.ddd.common.exception.DomainError;
import java.util.List;

public final class MorabeheLoanTypeException extends AggregateException {

    public MorabeheLoanTypeException(List<DomainError> errors) {
        super(errors);
    }

    public MorabeheLoanTypeException(List<DomainError> errors, Throwable cause) {
        super(errors, cause);
    }

    public MorabeheLoanTypeException(String messageKey, Object... args) {
        super(messageKey, args);
    }

    public MorabeheLoanTypeException(String messageKey, Throwable cause, Object... args) {
        super(messageKey, cause, args);
    }
}
