package ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply;

import java.math.BigDecimal;

import org.jspecify.annotations.Nullable;

import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbKafkaBaseResponse;

public final class SanctionDetailsKafkaResponse extends FcbKafkaBaseResponse {

    private @Nullable String sanctionSerial;
    private @Nullable String sanctionType;
    private @Nullable BigDecimal approvedAmount;
    private @Nullable String currency;
    private @Nullable String gracePeriod;
    private @Nullable Integer installmentCount;
    private @Nullable String loanDuration;
    private @Nullable String disbursementMethod;
    private @Nullable String lifeInsuranceId;
    private @Nullable String collateralSerial;
    private @Nullable String revocationReason;
    private @Nullable String confirmType;

    public @Nullable String getSanctionSerial() {
        return sanctionSerial;
    }

    public void setSanctionSerial(@Nullable String sanctionSerial) {
        this.sanctionSerial = sanctionSerial;
    }

    public @Nullable String getSanctionType() {
        return sanctionType;
    }

    public void setSanctionType(@Nullable String sanctionType) {
        this.sanctionType = sanctionType;
    }

    public @Nullable BigDecimal getApprovedAmount() {
        return approvedAmount;
    }

    public void setApprovedAmount(@Nullable BigDecimal approvedAmount) {
        this.approvedAmount = approvedAmount;
    }

    public @Nullable String getCurrency() {
        return currency;
    }

    public void setCurrency(@Nullable String currency) {
        this.currency = currency;
    }

    public @Nullable String getGracePeriod() {
        return gracePeriod;
    }

    public void setGracePeriod(@Nullable String gracePeriod) {
        this.gracePeriod = gracePeriod;
    }

    public @Nullable Integer getInstallmentCount() {
        return installmentCount;
    }

    public void setInstallmentCount(@Nullable Integer installmentCount) {
        this.installmentCount = installmentCount;
    }

    public @Nullable String getLoanDuration() {
        return loanDuration;
    }

    public void setLoanDuration(@Nullable String loanDuration) {
        this.loanDuration = loanDuration;
    }

    public @Nullable String getDisbursementMethod() {
        return disbursementMethod;
    }

    public void setDisbursementMethod(@Nullable String disbursementMethod) {
        this.disbursementMethod = disbursementMethod;
    }

    public @Nullable String getLifeInsuranceId() {
        return lifeInsuranceId;
    }

    public void setLifeInsuranceId(@Nullable String lifeInsuranceId) {
        this.lifeInsuranceId = lifeInsuranceId;
    }

    public @Nullable String getCollateralSerial() {
        return collateralSerial;
    }

    public void setCollateralSerial(@Nullable String collateralSerial) {
        this.collateralSerial = collateralSerial;
    }

    public @Nullable String getRevocationReason() {
        return revocationReason;
    }

    public void setRevocationReason(@Nullable String revocationReason) {
        this.revocationReason = revocationReason;
    }

    public @Nullable String getConfirmType() {
        return confirmType;
    }

    public void setConfirmType(@Nullable String confirmType) {
        this.confirmType = confirmType;
    }
}
