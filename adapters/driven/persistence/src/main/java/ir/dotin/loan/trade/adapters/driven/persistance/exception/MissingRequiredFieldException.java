package ir.dotin.loan.trade.adapters.driven.persistance.exception;

public class MissingRequiredFieldException extends PersistenceConversionException {
    public MissingRequiredFieldException(String message) {
        super(message);
    }

    public MissingRequiredFieldException(String message, Throwable cause) {
        super(message, cause);
    }
}
