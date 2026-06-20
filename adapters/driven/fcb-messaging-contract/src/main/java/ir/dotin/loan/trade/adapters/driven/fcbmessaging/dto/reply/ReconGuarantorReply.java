package ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply;

import org.jspecify.annotations.Nullable;

/**
 * Wire element of {@link ReconStateResponse}: one guarantor of the facility as seen by FCB. Field names are part of the
 * FCB wire contract — keep them byte-for-byte aligned with the FCB-side {@code ReconGuarantorDto}
 * ({@code customerNumber}, {@code guaranteePercent}, both strings). The recon adapter maps this onto the port value
 * type {@code ReconGuarantor}, parsing {@code guaranteePercent} into a {@code BigDecimal} for the drift comparison.
 */
public final class ReconGuarantorReply {

    @SuppressWarnings("NullAway.Init")
    private String customerNumber;

    private @Nullable String guaranteePercent;

    public String getCustomerNumber() {
        return customerNumber;
    }

    public void setCustomerNumber(String customerNumber) {
        this.customerNumber = customerNumber;
    }

    public @Nullable String getGuaranteePercent() {
        return guaranteePercent;
    }

    public void setGuaranteePercent(@Nullable String guaranteePercent) {
        this.guaranteePercent = guaranteePercent;
    }
}
