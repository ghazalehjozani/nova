package ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto;

import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public abstract class FcbBaseRequest {

    @SuppressWarnings("NullAway.Init")
    private String operationName;

    protected FcbBaseRequest(String operationName) {
        this.operationName = operationName;
    }

    public String getOperationName() {
        return operationName;
    }
}
