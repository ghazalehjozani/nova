package ir.dotin.loan.trade.core.application.service.submitfacilityforapproval.step;

import java.util.List;

import org.springframework.stereotype.Component;

import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.error.FailureCause;
import ir.dotin.platform.pangaea.commons.domain.event.DomainEvent;
import ir.dotin.platform.pangaea.workflow.api.context.WorkflowContext;
import ir.dotin.platform.pangaea.workflow.api.definition.PublishingWriteActivity;
import ir.dotin.platform.pangaea.workflow.api.model.StepResult;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.trade.core.application.ports.inbound.command.SubmitFacilityForApprovalCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.application.service.submitfacilityforapproval.commandhandler.SubmitFacilityForApprovalCommandHandler;
import ir.dotin.loan.trade.core.application.service.submitfacilityforapproval.i18n.SubmitFacilityForApprovalErrorCodes;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;
import ir.dotin.loan.trade.core.domain.loanfacility.service.TradeLoanFacilityService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubmitFacilityStep implements PublishingWriteActivity<SubmitFacilityForApprovalCommandHandler.Data> {

    private final TradeLoanFacilityRepository repository;
    private final TradeLoanFacilityService domainService;

    public StepResult<List<DomainEvent<?>>> execute(WorkflowContext<SubmitFacilityForApprovalCommandHandler.Data> ctx) {
        SubmitFacilityForApprovalCommand command = ctx.data().command();
        LoanFacilityId loanFacilityId = LoanFacilityId.of(command.loanFacilityId());

        var result = Result.fromOptional(
                        repository.findById(loanFacilityId),
                        () -> FailureCause.notFound(Notification.ofError(
                                SubmitFacilityForApprovalErrorCodes.FACILITY_NOT_FOUND, command.loanFacilityId())))
                .flatMap(facility -> domainService.submitForApproval(facility).map(v -> facility))
                .onSuccess(facility -> {
                    repository.save(facility, command.version());
                    log.info("Facility submitted for approval: {}", command.loanFacilityId());
                })
                .map(TradeLoanFacility::domainEvents);

        return StepResult.fromWriteResult(result);
    }
}
