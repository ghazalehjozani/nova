package ir.dotin.loan.trade.core.application.service.closefacilitydefaulted.commandhandler;

import org.springframework.stereotype.Service;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.platform.pangaea.workflow.api.command.WorkflowCommandHandler;
import ir.dotin.platform.pangaea.workflow.api.definition.Workflow;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.trade.core.application.ports.inbound.command.CloseFacilityDefaultedCommand;
import ir.dotin.loan.trade.core.application.service.closefacilitydefaulted.step.CloseDefaultedFacilityStep;
import ir.dotin.loan.trade.core.application.service.shared.authz.BranchAccessValidator;

import lombok.RequiredArgsConstructor;

import static ir.dotin.platform.pangaea.workflow.api.definition.Steps.writePublishing;

@Service
@RequiredArgsConstructor
public final class CloseFacilityDefaultedCommandHandler
        implements WorkflowCommandHandler<CloseFacilityDefaultedCommand, CloseFacilityDefaultedCommandHandler.Data> {

    @Override
    public Workflow<Data> definition() {
        // @formatter:off
        return Workflow.singleWrite("close-facility-defaulted", writePublishing(closeDefaultedFacilityStep));
        // @formatter:on
    }

    @Override
    public Result<Data> seed(CloseFacilityDefaultedCommand command) {
        return branchAccessValidator
                .verifyCallerCoversFacility(command.branchCode(), LoanFacilityId.of(command.loanFacilityId()))
                .map(prepared -> new Data(command, prepared));
    }

    public record Data(CloseFacilityDefaultedCommand command, Unit prepared) {}

    private final BranchAccessValidator branchAccessValidator;
    private final CloseDefaultedFacilityStep closeDefaultedFacilityStep;
}
