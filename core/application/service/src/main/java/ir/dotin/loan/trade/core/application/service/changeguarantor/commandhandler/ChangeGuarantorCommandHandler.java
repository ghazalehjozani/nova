package ir.dotin.loan.trade.core.application.service.changeguarantor.commandhandler;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.workflow.api.command.WorkflowCommandHandler;
import ir.dotin.platform.pangaea.workflow.api.definition.Workflow;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.customer.GuarantorParty;
import ir.dotin.loan.trade.core.application.ports.inbound.command.ChangeGuarantorCommand;
import ir.dotin.loan.trade.core.application.service.changeguarantor.component.GuarantorResolver;
import ir.dotin.loan.trade.core.application.service.changeguarantor.step.ChangeGuarantorStep;
import ir.dotin.loan.trade.core.application.service.shared.authz.BranchAccessValidator;

import lombok.RequiredArgsConstructor;

import static ir.dotin.platform.pangaea.workflow.api.definition.Steps.writePublishing;

@Service
@RequiredArgsConstructor
public final class ChangeGuarantorCommandHandler
        implements WorkflowCommandHandler<ChangeGuarantorCommand, ChangeGuarantorCommandHandler.Data> {

    @Override
    public Workflow<Data> definition() {
        // @formatter:off
        return Workflow.singleWrite("change-guarantor", writePublishing(changeGuarantorStep));
        // @formatter:on
    }

    @Override
    public Result<Data> seed(ChangeGuarantorCommand command) {
        return branchAccessValidator
                .verifyCallerCoversFacility(command.branchCode(), LoanFacilityId.of(command.loanFacilityId()))
                .flatMap(ignored -> guarantorResolver.resolve(command.guarantors()))
                .map(guarantors -> new Data(command.loanFacilityId(), command.version(), guarantors));
    }

    public record Data(UUID facilityId, long expectedVersion, List<GuarantorParty> guarantors) {}

    private final BranchAccessValidator branchAccessValidator;
    private final GuarantorResolver guarantorResolver;
    private final ChangeGuarantorStep changeGuarantorStep;
}
