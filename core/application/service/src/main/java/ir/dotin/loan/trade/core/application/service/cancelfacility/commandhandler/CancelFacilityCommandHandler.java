package ir.dotin.loan.trade.core.application.service.cancelfacility.commandhandler;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.platform.pangaea.commons.core.error.FailureCause;
import ir.dotin.platform.pangaea.commons.domain.event.DomainEvent;
import ir.dotin.platform.pangaea.workflow.api.command.WorkflowCommandHandler;
import ir.dotin.platform.pangaea.workflow.api.definition.Workflow;
import ir.dotin.platform.pangaea.workflow.api.engine.WorkflowEngine;
import ir.dotin.platform.pangaea.workflow.api.model.StepResult;
import ir.dotin.loan.baseloan.core.domain.shared.vo.InstallmentScheduleId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.trade.core.application.ports.inbound.command.CancelFacilityCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.InstallmentScheduleRepository;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.application.ports.outbound.query.ApplicationNumberResolver;
import ir.dotin.loan.trade.core.application.service.shared.error.TradeLoanApplicationServiceErrors;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;
import ir.dotin.loan.trade.core.domain.loanfacility.service.TradeLoanFacilityService;

@Service
public final class CancelFacilityCommandHandler extends WorkflowCommandHandler<CancelFacilityCommand, CancelFacilityCommandHandler.Data> {

    private static final Logger log = LoggerFactory.getLogger(CancelFacilityCommandHandler.class);

    record Data(CancelFacilityCommand command, Unit prepared) {}

    private final TradeLoanFacilityRepository facilityRepository;
    private final InstallmentScheduleRepository scheduleRepository;
    private final TradeLoanFacilityService domainService;
    private final ApplicationNumberResolver applicationNumberResolver;
    private final Clock clock;
    private final Workflow<Data> workflow;

    public CancelFacilityCommandHandler(
            WorkflowEngine engine,
            TradeLoanFacilityRepository facilityRepository,
            InstallmentScheduleRepository scheduleRepository,
            TradeLoanFacilityService domainService,
            ApplicationNumberResolver applicationNumberResolver,
            Clock clock) {
        super(engine);
        this.facilityRepository = facilityRepository;
        this.scheduleRepository = scheduleRepository;
        this.domainService = domainService;
        this.applicationNumberResolver = applicationNumberResolver;
        this.clock = clock;
        this.workflow = Workflow.singleWrite(
                "cancel-facility",
                ctx -> StepResult.fromWriteResult(write(ctx.data().command(), ctx.data().prepared())));
    }

    @Override
    protected Workflow<Data> workflow() {
        return workflow;
    }

    @Override
    protected Result<Data> seed(CancelFacilityCommand command) {
        return prepare(command).map(prepared -> new Data(command, prepared));
    }

    private Result<Unit> prepare(CancelFacilityCommand command) {
        return Result.success();
    }

    private Result<List<DomainEvent<?>>> write(CancelFacilityCommand command, Unit prepared) {
        List<DomainEvent<?>> domainEvents = new ArrayList<>();

        return resolveIdentifiers(command)
                .flatMap(this::loadTradeLoanFacility)
                .flatMap(facility -> cancelFacility(facility, command, domainEvents))
                .flatMap(facility -> cancelInstallmentSchedule(facility, command, domainEvents))
                .map(unused -> domainEvents);
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
