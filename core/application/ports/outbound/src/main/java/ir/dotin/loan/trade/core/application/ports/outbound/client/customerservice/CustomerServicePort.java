package ir.dotin.loan.trade.core.application.ports.outbound.client.customerservice;

import java.util.List;

import ir.dotin.platform.commons.core.Result;
import ir.dotin.loan.trade.core.application.ports.outbound.client.request.CustomerInfoLoadOptions;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.PartyBirthInfo;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.PartyInfo;

public interface CustomerServicePort {

    Result<PartyInfo> loadCustomerInfo(String customerNumber, CustomerInfoLoadOptions options);

    Result<List<PartyInfo>> findRelatedCustomers(List<String> customerNumbers);

    Result<PartyBirthInfo> loadCustomerBirthInfo(String customerNumber);
}
