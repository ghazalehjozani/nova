package ir.dotin.loan.trade.core.application.service.removeguarantor.commandhandler;

import java.util.UUID;

import org.springframework.stereotype.Service;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.workflow.api.command.WorkflowCommandHandler;
import ir.dotin.platform.pangaea.workflow.api.definition.Workflow;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.trade.core.application.ports.inbound.command.RemoveGuarantorCommand;
import ir.dotin.loan.trade.core.application.service.removeguarantor.step.RemoveGuarantorStep;
import ir.dotin.loan.trade.core.application.service.shared.authz.BranchAccessValidator;

import lombok.RequiredArgsConstructor;

import static ir.dotin.platform.pangaea.workflow.api.definition.Steps.writePublishing;

@Service
@RequiredArgsConstructor
public final class RemoveGuarantorCommandHandler
        implements WorkflowCommandHandler<RemoveGuarantorCommand, RemoveGuarantorCommandHandler.Data> {

    @Override
    public Workflow<Data> definition() {
        // @formatter:off
        return Workflow.singleWrite("remove-guarantor", writePublishing(removeGuarantorStep));
        // @formatter:on
    }

    @Override
    public Result<Data> seed(RemoveGuarantorCommand command) {
        return branchAccessValidator
                .verifyCallerCoversFacility(command.branchCode(), LoanFacilityId.of(command.loanFacilityId()))
                .map(ignored -> new Data(command.loanFacilityId(), command.version(), command.customerNumber()));
    }

    public record Data(UUID facilityId, long expectedVersion, String customerNumber) {}

    private final BranchAccessValidator branchAccessValidator;
    private final RemoveGuarantorStep removeGuarantorStep;
}
