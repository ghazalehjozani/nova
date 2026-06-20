package ir.dotin.loan.trade.adapters.driving.contract.mapper;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import ir.dotin.loan.trade.adapters.driving.contract.dto.ChangeGuarantorRequest;
import ir.dotin.loan.trade.core.application.ports.inbound.command.ChangeGuarantorCommand;

@Component
public class ChangeGuarantorRequestToCommandMapper {

    public ChangeGuarantorCommand toCommand(UUID facilityId, ChangeGuarantorRequest request) {
        // uid is stamped from the request's idempotency key and branchCode from the auth context, both in the
        // controller.
        List<ChangeGuarantorCommand.GuarantorInput> guarantors = request.guarantors().stream()
                .map(g -> new ChangeGuarantorCommand.GuarantorInput(g.customerNumber(), g.guaranteePercentage()))
                .toList();
        return ChangeGuarantorCommand.builder()
                .version(request.version())
                .loanFacilityId(facilityId)
                .guarantors(guarantors)
                .build();
    }
}
