package ir.dotin.loan.morabehe.core.domain.loanapplication.exception;

import java.util.List;

import ir.dotin.platform.ddd.common.exception.DomainError;
import ir.dotin.loan.baseloan.domain.loanapplication.exception.LoanApplicationValidationException;

public final class MorabeheLoanApplicationValidationException extends LoanApplicationValidationException {

    public MorabeheLoanApplicationValidationException(List<DomainError> errors) {
        super(errors);
    }

    public MorabeheLoanApplicationValidationException(List<DomainError> errors, Throwable cause) {
        super(errors, cause);
    }

    public MorabeheLoanApplicationValidationException(String messageKey, String fieldName, Object... args) {
        super(messageKey, fieldName, args);
    }

    public MorabeheLoanApplicationValidationException(
            String messageKey, Throwable cause, String fieldName, Object... args) {
        super(messageKey, cause, fieldName, args);
    }
}
