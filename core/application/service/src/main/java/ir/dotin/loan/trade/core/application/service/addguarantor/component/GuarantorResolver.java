package ir.dotin.loan.trade.core.application.service.addguarantor.component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.loan.baseloan.core.domain.shared.enums.PartyRole;
import ir.dotin.loan.baseloan.core.domain.shared.vo.customer.GuarantorParty;
import ir.dotin.loan.trade.core.application.ports.inbound.command.AddGuarantorsCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.client.customerservice.CustomerServicePort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.request.CustomerInfoLoadOptions;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.PartyInfoResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class GuarantorResolver {

    private final CustomerServicePort customerServicePort;

    public Result<List<GuarantorParty>> resolve(List<AddGuarantorsCommand.GuarantorInput> guarantors) {
        log.debug("Resolving customer-info for {} guarantor(s)", guarantors.size());

        List<GuarantorParty> resolved = new ArrayList<>(guarantors.size());
        for (AddGuarantorsCommand.GuarantorInput input : guarantors) {
            BigDecimal percentage = BigDecimal.valueOf(input.guaranteePercentage());
            Result<PartyInfoResponse> infoResult = customerServicePort.loadCustomerInfo(
                    input.customerNumber(), PartyRole.GUARANTOR, percentage, CustomerInfoLoadOptions.baseInfoOnly());
            if (infoResult.isFailure()) {
                return Result.failure(infoResult.err().orElseThrow());
            }
            resolved.add((GuarantorParty) infoResult.unwrap().party());
        }
        return Result.success(resolved);
    }
}
