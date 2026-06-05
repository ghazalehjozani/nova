package ir.dotin.loan.trade.adapters.driven.fcbmessaging.exception;

public class FcbClientException extends FcbMessagingException {

    public FcbClientException(String errorCode, String errorMessage) {
        super(errorCode, errorMessage);
    }
}
