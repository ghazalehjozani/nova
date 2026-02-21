package ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.response;

import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbKafkaBaseResponse;

import org.jspecify.annotations.Nullable;

public final class CustomerBirthInfoKafkaResponse extends FcbKafkaBaseResponse {

    private @Nullable String customerNumber;
    private @Nullable Integer age;
    private boolean isGrowthOrder;
    private boolean isUnderEighteenYearsOld;
    private @Nullable String birthDate;
    private boolean isCheckGrowthAg;

    public @Nullable String getCustomerNumber() {
        return customerNumber;
    }

    public void setCustomerNumber(@Nullable String customerNumber) {
        this.customerNumber = customerNumber;
    }

    public @Nullable Integer getAge() {
        return age;
    }

    public void setAge(@Nullable Integer age) {
        this.age = age;
    }

    public boolean isGrowthOrder() {
        return isGrowthOrder;
    }

    public void setGrowthOrder(boolean growthOrder) {
        isGrowthOrder = growthOrder;
    }

    public boolean isUnderEighteenYearsOld() {
        return isUnderEighteenYearsOld;
    }

    public void setUnderEighteenYearsOld(boolean underEighteenYearsOld) {
        isUnderEighteenYearsOld = underEighteenYearsOld;
    }

    public @Nullable String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(@Nullable String birthDate) {
        this.birthDate = birthDate;
    }

    public boolean isCheckGrowthAg() {
        return isCheckGrowthAg;
    }

    public void setCheckGrowthAg(boolean checkGrowthAg) {
        isCheckGrowthAg = checkGrowthAg;
    }
}
