package ir.dotin.loan.morabehe.core.domain.loanapplication.exception;

import ir.dotin.loan.baseloan.domain.loanapplication.exception.LoanApplicationException;
import ir.dotin.platform.ddd.common.exception.DomainError;
import java.util.List;

public final class MorabeheLoanApplicationException extends LoanApplicationException {

    public MorabeheLoanApplicationException(List<DomainError> errors) {
        super(errors);
    }

    public MorabeheLoanApplicationException(List<DomainError> errors, Throwable cause) {
        super(errors, cause);
    }

    public MorabeheLoanApplicationException(String messageKey, Object... args) {
        super(messageKey, args);
    }

    public MorabeheLoanApplicationException(String messageKey, Throwable cause, Object... args) {
        super(messageKey, cause, args);
    }

}
