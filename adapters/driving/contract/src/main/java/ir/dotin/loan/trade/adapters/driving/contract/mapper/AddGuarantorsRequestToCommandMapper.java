package ir.dotin.loan.trade.adapters.driving.contract.mapper;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import ir.dotin.loan.trade.adapters.driving.contract.dto.AddGuarantorsRequest;
import ir.dotin.loan.trade.core.application.ports.inbound.command.AddGuarantorsCommand;

@Component
public class AddGuarantorsRequestToCommandMapper {

    public AddGuarantorsCommand toCommand(UUID facilityId, AddGuarantorsRequest request) {
        // uid is stamped from the request's idempotency key and branchCode from the auth context, both in the
        // controller.
        List<AddGuarantorsCommand.GuarantorInput> guarantors = request.guarantors().stream()
                .map(g -> new AddGuarantorsCommand.GuarantorInput(g.customerNumber(), g.guaranteePercentage()))
                .toList();
        return AddGuarantorsCommand.builder()
                .version(request.version())
                .loanFacilityId(facilityId)
                .guarantors(guarantors)
                .build();
    }
}
