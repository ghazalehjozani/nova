package ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.response;

import java.util.List;

import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbKafkaBaseResponse;

import org.jspecify.annotations.Nullable;

public final class CustomerListKafkaResponse extends FcbKafkaBaseResponse {

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
