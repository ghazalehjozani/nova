package ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply;

import org.jspecify.annotations.Nullable;

import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbBaseResponse;

public final class BranchDetailsResponse extends FcbBaseResponse {

    private @Nullable String code;
    private @Nullable String name;
    private @Nullable String foreignName;
    private @Nullable Long globalCode;
    private @Nullable String managerName;
    private @Nullable String samCode;
    private @Nullable String swiftCode;
    private @Nullable String clearBranch;
    private @Nullable String cityCode;
    private @Nullable String bankCode;

    public @Nullable String getCode() {
        return code;
    }

    public void setCode(@Nullable String code) {
        this.code = code;
    }

    public @Nullable String getName() {
        return name;
    }

    public void setName(@Nullable String name) {
        this.name = name;
    }

    public @Nullable String getForeignName() {
        return foreignName;
    }

    public void setForeignName(@Nullable String foreignName) {
        this.foreignName = foreignName;
    }

    public @Nullable Long getGlobalCode() {
        return globalCode;
    }

    public void setGlobalCode(@Nullable Long globalCode) {
        this.globalCode = globalCode;
    }

    public @Nullable String getManagerName() {
        return managerName;
    }

    public void setManagerName(@Nullable String managerName) {
        this.managerName = managerName;
    }

    public @Nullable String getSamCode() {
        return samCode;
    }

    public void setSamCode(@Nullable String samCode) {
        this.samCode = samCode;
    }

    public @Nullable String getSwiftCode() {
        return swiftCode;
    }

    public void setSwiftCode(@Nullable String swiftCode) {
        this.swiftCode = swiftCode;
    }

    public @Nullable String getClearBranch() {
        return clearBranch;
    }

    public void setClearBranch(@Nullable String clearBranch) {
        this.clearBranch = clearBranch;
    }

    public @Nullable String getCityCode() {
        return cityCode;
    }

    public void setCityCode(@Nullable String cityCode) {
        this.cityCode = cityCode;
    }

    public @Nullable String getBankCode() {
        return bankCode;
    }

    public void setBankCode(@Nullable String bankCode) {
        this.bankCode = bankCode;
    }
}
