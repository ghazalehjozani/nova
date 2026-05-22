package ir.dotin.loan.trade.core.application.ports.outbound.client.response;

import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.domain.vo.ValueObject;

public record EconomicalSectorResponse(String code, String name, Boolean hasChild, String parentCode)
        implements ValueObject<EconomicalSectorResponse> {

    public static Result<EconomicalSectorResponse> of(String code, String name, Boolean hasChild, String parentCode) {

        EconomicalSectorResponse type = new EconomicalSectorResponse(code, name, hasChild, parentCode);

        Notification internalValidation = type.validate();
        if (internalValidation.hasErrors()) {
            return Result.failure(internalValidation);
        }
        return Result.success(type);
    }
}
