package ir.dotin.loan.trade.core.application.service.rejectfacility.step;

import java.time.Clock;
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
import ir.dotin.loan.trade.core.application.ports.inbound.command.RejectFacilityCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.application.service.rejectfacility.commandhandler.RejectFacilityCommandHandler;
import ir.dotin.loan.trade.core.application.service.shared.error.TradeLoanApplicationServiceErrors;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RejectFacilityStep implements PublishingWriteActivity<RejectFacilityCommandHandler.Data> {

    private final TradeLoanFacilityRepository repository;
    private final Clock clock;

    @Override
    public StepResult<List<DomainEvent<?>>> execute(WorkflowContext<RejectFacilityCommandHandler.Data> ctx) {
        RejectFacilityCommand command = ctx.data().command();
        LoanFacilityId loanFacilityId = LoanFacilityId.of(command.loanFacilityId());

        var result = Result.fromOptional(
                        repository.findById(loanFacilityId),
                        () -> FailureCause.notFound(Notification.ofError(
                                TradeLoanApplicationServiceErrors.FACILITY_NOT_FOUND, command.uid())))
                .onSuccess(facility -> {
                    facility.reject(clock);
                    repository.save(facility, command.version());
                    log.info("Facility rejected: {}", command.loanFacilityId());
                })
                .map(TradeLoanFacility::domainEvents);

        return StepResult.fromWriteResult(result);
    }
}
