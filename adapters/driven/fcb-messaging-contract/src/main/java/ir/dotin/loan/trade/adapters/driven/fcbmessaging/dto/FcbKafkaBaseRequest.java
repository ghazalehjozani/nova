package ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto;

import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public abstract class FcbKafkaBaseRequest {

    @SuppressWarnings("NullAway.Init")
    private String operationName;

    protected FcbKafkaBaseRequest(String operationName) {
        this.operationName = operationName;
    }

    public String getOperationName() {
        return operationName;
    }
}
