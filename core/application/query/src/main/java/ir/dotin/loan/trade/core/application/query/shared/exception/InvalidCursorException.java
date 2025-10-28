package ir.dotin.loan.trade.core.application.query.shared.exception;

import ir.dotin.platform.commons.core.exception.OperationalException;

public class InvalidCursorException extends OperationalException {
    public InvalidCursorException(String message) {
        super(message);
    }

    public InvalidCursorException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidCursorException(String errorCode, String message, Object... args) {
        super(errorCode, message, args);
    }

    public InvalidCursorException(String errorCode, String message, Throwable cause, Object... args) {
        super(errorCode, message, cause, args);
    }
}
