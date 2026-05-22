package ir.dotin.loan.trade.core.application.ports.outbound.client.customerservice;

import java.math.BigDecimal;
import java.util.List;
import jakarta.validation.constraints.NotNull;

import org.jspecify.annotations.Nullable;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.loan.baseloan.core.domain.shared.enums.PartyRole;
import ir.dotin.loan.trade.core.application.ports.outbound.client.request.CustomerInfoLoadOptions;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.PartyBirthInfo;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.PartyInfoResponse;

public interface CustomerServicePort {

    Result<PartyInfoResponse> loadCustomerInfo(
            String customerNumber,
            @NotNull PartyRole role,
            @Nullable BigDecimal guaranteePercentage,
            CustomerInfoLoadOptions options);

    Result<List<PartyInfoResponse>> findRelatedCustomers(List<String> customerNumbers);

    Result<PartyBirthInfo> loadCustomerBirthInfo(String customerNumber);
}
