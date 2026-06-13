package ir.dotin.loan.trade.core.application.service.approvefacility.commandhandler;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.workflow.api.command.WorkflowCommandHandler;
import ir.dotin.platform.pangaea.workflow.api.definition.Workflow;
import ir.dotin.loan.baseloan.core.domain.shared.vo.ConfirmType;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.trade.core.application.ports.inbound.command.ApproveFacilityCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.SanctionDetails;
import ir.dotin.loan.trade.core.application.service.approvefacility.component.SanctionDetailsLoader;
import ir.dotin.loan.trade.core.application.service.approvefacility.step.ApproveFacilityStep;
import ir.dotin.loan.trade.core.application.service.shared.authz.BranchAccessValidator;

import lombok.RequiredArgsConstructor;

import static ir.dotin.platform.pangaea.workflow.api.definition.Steps.writePublishing;

@Service
@RequiredArgsConstructor
public final class ApproveFacilityCommandHandler
        implements WorkflowCommandHandler<ApproveFacilityCommand, ApproveFacilityCommandHandler.Data> {

    @Override
    public Workflow<Data> definition() {
        // @formatter:off
        return Workflow.singleWrite("approve-facility", writePublishing(approveFacilityStep));
        // @formatter:on
    }

    @Override
    public Result<Data> seed(ApproveFacilityCommand command) {
        ConfirmType confirmType = ConfirmType.of(command.confirmType()).unwrap();

        return branchAccessValidator
                .verifyCallerCoversFacility(command.branchCode(), LoanFacilityId.of(command.loanFacilityId()))
                .flatMap(ignored -> {
                    if (command.sanctionSerial() == null) {
                        return Result.success(new ApprovalPreparation(confirmType, null));
                    }
                    return sanctionDetailsLoader
                            .loadForManualApproval(command.loanFacilityId())
                            .map(details -> new ApprovalPreparation(confirmType, details));
                })
                .map(prepared -> new Data(command, prepared));
    }

    public record ApprovalPreparation(
            ConfirmType confirmType, @Nullable SanctionDetails sanctionDetails) {}

    public record Data(ApproveFacilityCommand command, ApprovalPreparation prepared) {}

    private final BranchAccessValidator branchAccessValidator;
    private final SanctionDetailsLoader sanctionDetailsLoader;
    private final ApproveFacilityStep approveFacilityStep;
}
