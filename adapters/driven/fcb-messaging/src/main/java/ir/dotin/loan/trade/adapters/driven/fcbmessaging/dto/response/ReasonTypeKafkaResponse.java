package ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.response;

import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbKafkaBaseResponse;

import org.jspecify.annotations.Nullable;

public final class ReasonTypeKafkaResponse extends FcbKafkaBaseResponse {

    private @Nullable String code;
    private @Nullable String centralBankCode;
    private @Nullable String description;
    private @Nullable String reasonType;
    private boolean shouldHasSerial;
    private boolean exemptionOfInquiryNumber;

    public @Nullable String getCode() {
        return code;
    }

    public void setCode(@Nullable String code) {
        this.code = code;
    }

    public @Nullable String getCentralBankCode() {
        return centralBankCode;
    }

    public void setCentralBankCode(@Nullable String centralBankCode) {
        this.centralBankCode = centralBankCode;
    }

    public @Nullable String getDescription() {
        return description;
    }

    public void setDescription(@Nullable String description) {
        this.description = description;
    }

    public @Nullable String getReasonType() {
        return reasonType;
    }

    public void setReasonType(@Nullable String reasonType) {
        this.reasonType = reasonType;
    }

    public boolean isShouldHasSerial() {
        return shouldHasSerial;
    }

    public void setShouldHasSerial(boolean shouldHasSerial) {
        this.shouldHasSerial = shouldHasSerial;
    }

    public boolean isExemptionOfInquiryNumber() {
        return exemptionOfInquiryNumber;
    }

    public void setExemptionOfInquiryNumber(boolean exemptionOfInquiryNumber) {
        this.exemptionOfInquiryNumber = exemptionOfInquiryNumber;
    }
}
