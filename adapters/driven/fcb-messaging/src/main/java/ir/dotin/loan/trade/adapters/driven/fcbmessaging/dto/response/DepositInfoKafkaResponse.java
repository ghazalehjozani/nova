package ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.response;

import java.util.List;

import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbKafkaBaseResponse;

import org.jspecify.annotations.Nullable;

public final class DepositInfoKafkaResponse extends FcbKafkaBaseResponse {

    private @Nullable String number;
    private @Nullable String title;
    private @Nullable String type;
    private @Nullable String currency;
    private @Nullable String status;
    private boolean isExternalDeposit;
    private @Nullable String branchCode;
    private @Nullable List<String> ownerNationalCodes;

    public @Nullable String getNumber() {
        return number;
    }

    public void setNumber(@Nullable String number) {
        this.number = number;
    }

    public @Nullable String getTitle() {
        return title;
    }

    public void setTitle(@Nullable String title) {
        this.title = title;
    }

    public @Nullable String getType() {
        return type;
    }

    public void setType(@Nullable String type) {
        this.type = type;
    }

    public @Nullable String getCurrency() {
        return currency;
    }

    public void setCurrency(@Nullable String currency) {
        this.currency = currency;
    }

    public @Nullable String getStatus() {
        return status;
    }

    public void setStatus(@Nullable String status) {
        this.status = status;
    }

    public boolean isExternalDeposit() {
        return isExternalDeposit;
    }

    public void setExternalDeposit(boolean externalDeposit) {
        isExternalDeposit = externalDeposit;
    }

    public @Nullable String getBranchCode() {
        return branchCode;
    }

    public void setBranchCode(@Nullable String branchCode) {
        this.branchCode = branchCode;
    }

    public @Nullable List<String> getOwnerNationalCodes() {
        return ownerNationalCodes;
    }

    public void setOwnerNationalCodes(@Nullable List<String> ownerNationalCodes) {
        this.ownerNationalCodes = ownerNationalCodes;
    }
}
