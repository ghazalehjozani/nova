package ir.dotin.loan.trade.adapters.driven.fcbmessaging.exception;

public class FcbServerException extends FcbKafkaException {

    public FcbServerException(String errorCode, String errorMessage) {
        super(errorCode, errorMessage);
    }
}
