package ir.dotin.loan.trade.core.application.ports.outbound.client.response;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.commons.domain.vo.ValueObject;

public record DebtorCreditorDepositValidation(boolean isDebtor)
        implements ValueObject<DebtorCreditorDepositValidation> {

    public static Result<DebtorCreditorDepositValidation> of(boolean isDebtor) {
        DebtorCreditorDepositValidation validation = new DebtorCreditorDepositValidation(isDebtor);

        Notification internalValidation = validation.validate();
        if (internalValidation.hasErrors()) {
            return Result.failure(internalValidation);
        }

        return Result.success(validation);
    }

    public static Result<DebtorCreditorDepositValidation> debtor() {
        return of(true);
    }

    public static Result<DebtorCreditorDepositValidation> notDebtor() {
        return of(false);
    }
}
