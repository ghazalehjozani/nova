package ir.dotin.loan.trade.adapters.driven.fcbmessaging.exception;

import lombok.Getter;

@Getter
abstract class FcbKafkaException extends RuntimeException {

    private final String errorCode;
    private final String errorMessage;

    protected FcbKafkaException(String errorCode, String errorMessage) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }
}
