package ir.dotin.loan.trade.core.application.ports.outbound.client.response;

import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.domain.vo.ValueObject;

import static java.util.Objects.requireNonNull;

public record ReasonType(
        String code,
        String centralBankCode,
        String description,
        String reasonType,
        boolean shouldHasSerial,
        boolean exemptionOfInquiryNumber)
        implements ValueObject<ReasonType> {

    public ReasonType {
        requireNonNull(code, "ReasonType code cannot be null in constructor");
        requireNonNull(description, "ReasonType description cannot be null in constructor");
    }

    public static Result<ReasonType> of(
            String code,
            String centralBankCode,
            String description,
            String reasonType,
            boolean shouldHasSerial,
            boolean exemptionOfInquiryNumber) {

        ReasonType type = new ReasonType(
                code, centralBankCode, description, reasonType, shouldHasSerial, exemptionOfInquiryNumber);

        Notification internalValidation = type.validate();
        if (internalValidation.hasErrors()) {
            return Result.failure(internalValidation);
        }
        return Result.success(type);
    }
}
