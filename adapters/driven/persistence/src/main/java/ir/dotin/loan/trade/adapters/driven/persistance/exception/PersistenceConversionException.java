package ir.dotin.loan.trade.adapters.driven.persistance.exception;

public class PersistenceConversionException extends RuntimeException {
    public PersistenceConversionException(String message) {
        super(message);
    }

    public PersistenceConversionException(String message, Throwable cause) {
        super(message, cause);
    }
}
