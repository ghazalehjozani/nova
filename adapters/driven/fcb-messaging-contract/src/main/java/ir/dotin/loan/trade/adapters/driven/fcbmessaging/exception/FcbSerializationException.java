package ir.dotin.loan.trade.adapters.driven.fcbmessaging.exception;

public class FcbSerializationException extends FcbMessagingException {

    public FcbSerializationException(String errorMessage) {
        super("SERIALIZATION_ERROR", errorMessage);
    }
}
