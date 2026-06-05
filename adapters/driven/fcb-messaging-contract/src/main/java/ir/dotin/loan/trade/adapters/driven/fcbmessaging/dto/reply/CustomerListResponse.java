package ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply;

import java.util.List;

import org.jspecify.annotations.Nullable;

import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbBaseResponse;

public final class CustomerListResponse extends FcbBaseResponse {

    private @Nullable List<CustomerInfoDto> customers;

    public @Nullable List<CustomerInfoDto> getCustomers() {
        return customers;
    }

    public void setCustomers(@Nullable List<CustomerInfoDto> customers) {
        this.customers = customers;
    }

    public record CustomerInfoDto(
            @Nullable String customerNumber,
            @Nullable String firstName,
            @Nullable String lastName,
            @Nullable String title,
            @Nullable Boolean real,
            @Nullable String nationalCode,
            @Nullable Boolean isInBlackList,
            @Nullable Boolean isIncapable,
            @Nullable Boolean isInGrayList) {}
}
