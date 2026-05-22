package ir.dotin.loan.trade.core.application.query.shared.exception;

import ir.dotin.platform.pangaea.commons.core.exception.OperationalException;

public class CursorEncodingException extends OperationalException {
    public CursorEncodingException(String message) {
        super(message);
    }

    public CursorEncodingException(String message, Throwable cause) {
        super(message, cause);
    }

    public CursorEncodingException(String errorCode, String message, Object... args) {
        super(errorCode, message, args);
    }

    public CursorEncodingException(String errorCode, String message, Throwable cause, Object... args) {
        super(errorCode, message, cause, args);
    }
}
