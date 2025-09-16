package ir.dotin.loan.trade.adapters.driven.persistance.exception;

public class InvalidDomainStateException extends PersistenceConversionException {

    public InvalidDomainStateException(String message) {
        super(message);
    }

    public InvalidDomainStateException(String message, Throwable cause) {
        super(message, cause);
    }
}
