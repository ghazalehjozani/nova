package ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply;

import org.jspecify.annotations.Nullable;

import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbKafkaBaseResponse;

public final class CustomerInfoKafkaResponse extends FcbKafkaBaseResponse {

    private @Nullable String customerNumber;
    private @Nullable String firstName;
    private @Nullable String lastName;
    private @Nullable String title;
    private @Nullable Boolean real;
    private @Nullable String nationalCode;
    private @Nullable Boolean isInBlackList;
    private @Nullable Boolean isIncapable;
    private @Nullable Boolean isInGrayList;
    private @Nullable Boolean active;

    public @Nullable String getCustomerNumber() {
        return customerNumber;
    }

    public void setCustomerNumber(@Nullable String customerNumber) {
        this.customerNumber = customerNumber;
    }

    public @Nullable String getFirstName() {
        return firstName;
    }

    public void setFirstName(@Nullable String firstName) {
        this.firstName = firstName;
    }

    public @Nullable String getLastName() {
        return lastName;
    }

    public void setLastName(@Nullable String lastName) {
        this.lastName = lastName;
    }

    public @Nullable String getTitle() {
        return title;
    }

    public void setTitle(@Nullable String title) {
        this.title = title;
    }

    public @Nullable Boolean getReal() {
        return real;
    }

    public void setReal(@Nullable Boolean real) {
        this.real = real;
    }

    public @Nullable String getNationalCode() {
        return nationalCode;
    }

    public void setNationalCode(@Nullable String nationalCode) {
        this.nationalCode = nationalCode;
    }

    public @Nullable Boolean getIsInBlackList() {
        return isInBlackList;
    }

    public void setIsInBlackList(@Nullable Boolean isInBlackList) {
        this.isInBlackList = isInBlackList;
    }

    public @Nullable Boolean getIsIncapable() {
        return isIncapable;
    }

    public void setIsIncapable(@Nullable Boolean isIncapable) {
        this.isIncapable = isIncapable;
    }

    public @Nullable Boolean getIsInGrayList() {
        return isInGrayList;
    }

    public void setIsInGrayList(@Nullable Boolean isInGrayList) {
        this.isInGrayList = isInGrayList;
    }

    public @Nullable Boolean getActive() {
        return active;
    }

    public void setActive(@Nullable Boolean active) {
        this.active = active;
    }
}
