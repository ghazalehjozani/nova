package ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply;

import java.math.BigDecimal;

import org.jspecify.annotations.Nullable;

import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbKafkaBaseResponse;

public final class CollateralDetailsKafkaResponse extends FcbKafkaBaseResponse {

    private @Nullable String serial;
    private @Nullable String customerNo;
    private @Nullable String assuranceTypeCode;
    private @Nullable String assuranceTypeName;
    private @Nullable BigDecimal guaranteeAmount;
    private @Nullable BigDecimal price;
    private @Nullable BigDecimal usedMortgagePrice;
    private @Nullable Integer guaranteeDuration;
    private @Nullable String guaranteeNumber;
    private @Nullable String guaranteeIssuer;
    private @Nullable String guaranteeBranchCode;
    private @Nullable String loanFileNumber;
    private @Nullable String branchCode;
    private @Nullable String currency;
    private boolean active;
    private boolean isEscrowed;
    private boolean isReleaseAllowed;
    private boolean isSpecial;
    private @Nullable String address;

    public @Nullable String getSerial() {
        return serial;
    }

    public void setSerial(@Nullable String serial) {
        this.serial = serial;
    }

    public @Nullable String getCustomerNo() {
        return customerNo;
    }

    public void setCustomerNo(@Nullable String customerNo) {
        this.customerNo = customerNo;
    }

    public @Nullable String getAssuranceTypeCode() {
        return assuranceTypeCode;
    }

    public void setAssuranceTypeCode(@Nullable String assuranceTypeCode) {
        this.assuranceTypeCode = assuranceTypeCode;
    }

    public @Nullable String getAssuranceTypeName() {
        return assuranceTypeName;
    }

    public void setAssuranceTypeName(@Nullable String assuranceTypeName) {
        this.assuranceTypeName = assuranceTypeName;
    }

    public @Nullable BigDecimal getGuaranteeAmount() {
        return guaranteeAmount;
    }

    public void setGuaranteeAmount(@Nullable BigDecimal guaranteeAmount) {
        this.guaranteeAmount = guaranteeAmount;
    }

    public @Nullable BigDecimal getPrice() {
        return price;
    }

    public void setPrice(@Nullable BigDecimal price) {
        this.price = price;
    }

    public @Nullable BigDecimal getUsedMortgagePrice() {
        return usedMortgagePrice;
    }

    public void setUsedMortgagePrice(@Nullable BigDecimal usedMortgagePrice) {
        this.usedMortgagePrice = usedMortgagePrice;
    }

    public @Nullable Integer getGuaranteeDuration() {
        return guaranteeDuration;
    }

    public void setGuaranteeDuration(@Nullable Integer guaranteeDuration) {
        this.guaranteeDuration = guaranteeDuration;
    }

    public @Nullable String getGuaranteeNumber() {
        return guaranteeNumber;
    }

    public void setGuaranteeNumber(@Nullable String guaranteeNumber) {
        this.guaranteeNumber = guaranteeNumber;
    }

    public @Nullable String getGuaranteeIssuer() {
        return guaranteeIssuer;
    }

    public void setGuaranteeIssuer(@Nullable String guaranteeIssuer) {
        this.guaranteeIssuer = guaranteeIssuer;
    }

    public @Nullable String getGuaranteeBranchCode() {
        return guaranteeBranchCode;
    }

    public void setGuaranteeBranchCode(@Nullable String guaranteeBranchCode) {
        this.guaranteeBranchCode = guaranteeBranchCode;
    }

    public @Nullable String getLoanFileNumber() {
        return loanFileNumber;
    }

    public void setLoanFileNumber(@Nullable String loanFileNumber) {
        this.loanFileNumber = loanFileNumber;
    }

    public @Nullable String getBranchCode() {
        return branchCode;
    }

    public void setBranchCode(@Nullable String branchCode) {
        this.branchCode = branchCode;
    }

    public @Nullable String getCurrency() {
        return currency;
    }

    public void setCurrency(@Nullable String currency) {
        this.currency = currency;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isEscrowed() {
        return isEscrowed;
    }

    public void setEscrowed(boolean escrowed) {
        isEscrowed = escrowed;
    }

    public boolean isReleaseAllowed() {
        return isReleaseAllowed;
    }

    public void setReleaseAllowed(boolean releaseAllowed) {
        isReleaseAllowed = releaseAllowed;
    }

    public boolean isSpecial() {
        return isSpecial;
    }

    public void setSpecial(boolean special) {
        isSpecial = special;
    }

    public @Nullable String getAddress() {
        return address;
    }

    public void setAddress(@Nullable String address) {
        this.address = address;
    }
}
