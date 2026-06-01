package ir.dotin.loan.trade.adapters.driven.fcbmessaging.exception;

public class FcbSerializationException extends FcbKafkaException {

    public FcbSerializationException(String errorMessage) {
        super("SERIALIZATION_ERROR", errorMessage);
    }
}
