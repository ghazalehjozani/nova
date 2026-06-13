package ir.dotin.loan.trade.core.application.service.collectinstallment.compensation.step;

import java.time.Clock;
import java.util.List;

import org.springframework.stereotype.Component;

import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.error.FailureCause;
import ir.dotin.platform.pangaea.commons.domain.entity.AbstractAggregateRoot;
import ir.dotin.platform.pangaea.commons.domain.event.DomainEvent;
import ir.dotin.platform.pangaea.workflow.api.context.WorkflowContext;
import ir.dotin.platform.pangaea.workflow.api.definition.PublishingWriteActivity;
import ir.dotin.platform.pangaea.workflow.api.model.StepResult;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.entity.InstallmentSchedule;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.entity.InstallmentScheduleCompensationOperations;
import ir.dotin.loan.baseloan.core.domain.shared.vo.InstallmentScheduleId;
import ir.dotin.loan.trade.core.application.ports.inbound.command.CompensateCollectInstallmentCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.InstallmentScheduleRepository;
import ir.dotin.loan.trade.core.application.ports.outbound.query.ApplicationNumberResolver;
import ir.dotin.loan.trade.core.application.service.collectinstallment.compensation.commandhandler.CompensateCollectInstallmentCommandHandler;
import ir.dotin.loan.trade.core.application.service.shared.error.TradeLoanApplicationServiceErrors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RevertInstallmentCollectionStep
        implements PublishingWriteActivity<CompensateCollectInstallmentCommandHandler.Data> {

    private final ApplicationNumberResolver applicationNumberResolver;
    private final InstallmentScheduleRepository installmentScheduleRepository;
    private final Clock clock;

    public StepResult<List<DomainEvent<?>>> execute(
            WorkflowContext<CompensateCollectInstallmentCommandHandler.Data> ctx) {
        CompensateCollectInstallmentCommand command = ctx.data().command();

        var result = resolveIdentifiers(command)
                .flatMap(this::loadSchedule)
                .flatMap(compensationOperations -> {
                    compensationOperations.revertCollectInstallment(command.transactionNumbers(), clock);
                    return Result.success((InstallmentSchedule) compensationOperations);
                })
                .onSuccess(installmentScheduleRepository::save)
                .onSuccess(schedule -> log.info(
                        "Revert Installment collection completed: applicationNumber={}", command.applicationNumber()))
                .map(AbstractAggregateRoot::domainEvents);

        return StepResult.fromWriteResult(result);
    }

    private Result<ApplicationNumberResolver.LoanIdentifiers> resolveIdentifiers(
            CompensateCollectInstallmentCommand command) {
        return Result.fromOptional(
                applicationNumberResolver.resolveByApplicationNumber(command.applicationNumber()),
                () -> FailureCause.businessRule(Notification.ofError(
                        TradeLoanApplicationServiceErrors.APPLICATION_NUMBER_MISSING, command.applicationNumber())));
    }

    private Result<InstallmentScheduleCompensationOperations> loadSchedule(
            ApplicationNumberResolver.LoanIdentifiers ids) {
        return Result.fromOptional(
                installmentScheduleRepository
                        .findById(InstallmentScheduleId.of(ids.installmentScheduleId())
                                .unwrap())
                        .map(installmentSchedule -> (InstallmentScheduleCompensationOperations) installmentSchedule),
                () -> FailureCause.notFound(Notification.ofError(
                        TradeLoanApplicationServiceErrors.INSTALLMENT_SCHEDULE_NOT_FOUND,
                        ids.installmentScheduleId())));
    }
}
