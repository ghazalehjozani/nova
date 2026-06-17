package ir.dotin.loan.trade.core.application.service.cancelfacility.step;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Component;

import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.error.FailureCause;
import ir.dotin.platform.pangaea.commons.domain.event.DomainEvent;
import ir.dotin.platform.pangaea.workflow.api.context.WorkflowContext;
import ir.dotin.platform.pangaea.workflow.api.definition.PublishingWriteActivity;
import ir.dotin.platform.pangaea.workflow.api.model.StepResult;
import ir.dotin.loan.baseloan.core.domain.shared.vo.InstallmentScheduleId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.trade.core.application.ports.inbound.command.CancelFacilityCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.InstallmentScheduleRepository;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.application.ports.outbound.query.ApplicationNumberResolver;
import ir.dotin.loan.trade.core.application.service.cancelfacility.commandhandler.CancelFacilityCommandHandler;
import ir.dotin.loan.trade.core.application.service.shared.error.TradeLoanApplicationServiceErrors;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;
import ir.dotin.loan.trade.core.domain.loanfacility.service.TradeLoanFacilityService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CancelFacilityStep implements PublishingWriteActivity<CancelFacilityCommandHandler.Data> {

    private final TradeLoanFacilityRepository facilityRepository;
    private final InstallmentScheduleRepository scheduleRepository;
    private final TradeLoanFacilityService domainService;
    private final ApplicationNumberResolver applicationNumberResolver;
    private final Clock clock;

    @Override
    public StepResult<List<DomainEvent<?>>> execute(WorkflowContext<CancelFacilityCommandHandler.Data> ctx) {
        CancelFacilityCommand command = ctx.data().command();
        List<DomainEvent<?>> domainEvents = new ArrayList<>();

        var result = resolveIdentifiers(command)
                .flatMap(this::loadTradeLoanFacility)
                .flatMap(facility -> cancelFacility(facility, command, domainEvents))
                .flatMap(facility -> cancelInstallmentSchedule(facility, command, domainEvents))
                .map(_ -> domainEvents);

        return StepResult.fromWriteResult(result);
    }

    private Result<TradeLoanFacility> cancelFacility(
            TradeLoanFacility facility, CancelFacilityCommand command, List<DomainEvent<?>> domainEvents) {
        return domainService
                .cancel(
                        facility,
                        command.cancelDescription(),
                        command.cancelReason(),
                        command.cancelDate(),
                        Objects.requireNonNull(command.cancelLoanTransactionNumber(), "cancelLoanTransactionNumber"))
                .map(v -> {
                    facilityRepository.save(facility);
                    log.debug("Facility cancelled: {}", facility.getId().value());
                    domainEvents.addAll(facility.domainEvents());
                    return facility;
                });
    }

    private Result<TradeLoanFacility> cancelInstallmentSchedule(
            TradeLoanFacility facility, CancelFacilityCommand command, List<DomainEvent<?>> domainEvents) {
        Optional<InstallmentScheduleId> scheduleId = facility.getInstallmentScheduleId();
        if (scheduleId.isEmpty()) {
            return Result.failure(
                    Notification.ofError(TradeLoanApplicationServiceErrors.INSTALLMENT_SCHEDULE_NOT_FOUND));
        }

        return Result.fromOptional(
                        scheduleRepository.findById(scheduleId.orElseThrow()),
                        () -> FailureCause.notFound(Notification.ofError(
                                TradeLoanApplicationServiceErrors.INSTALLMENT_SCHEDULE_NOT_FOUND,
                                facility.getId().value())))
                .flatMap(schedule -> schedule.cancelSchedule(command.cancelReason(), clock)
                        .map(v -> {
                            scheduleRepository.save(schedule);
                            domainEvents.addAll(schedule.domainEvents());
                            return facility;
                        }));
    }

    private Result<ApplicationNumberResolver.LoanIdentifiers> resolveIdentifiers(CancelFacilityCommand command) {
        return Result.fromOptional(
                applicationNumberResolver.resolveByApplicationNumber(command.applicationNumber()),
                () -> FailureCause.businessRule(Notification.ofError(
                        TradeLoanApplicationServiceErrors.APPLICATION_NUMBER_MISSING, command.applicationNumber())));
    }

    private Result<TradeLoanFacility> loadTradeLoanFacility(ApplicationNumberResolver.LoanIdentifiers ids) {
        return Result.fromOptional(
                facilityRepository.findById(LoanFacilityId.of(ids.loanFacilityId())),
                () -> FailureCause.notFound(Notification.ofError(
                        TradeLoanApplicationServiceErrors.INSTALLMENT_SCHEDULE_NOT_FOUND, ids.loanFacilityId())));
    }
}
