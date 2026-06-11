package ir.dotin.loan.trade.core.application.service.originateloanfacility.compensation.commandhandler;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;

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
import ir.dotin.loan.baseloan.core.domain.installmentschedule.entity.InstallmentSchedule;
import ir.dotin.loan.baseloan.core.domain.shared.vo.InstallmentScheduleId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.trade.core.application.ports.inbound.command.CompensateOriginationCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.InstallmentScheduleRepository;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.application.service.shared.error.TradeLoanApplicationServiceErrors;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
final class CompensateOriginationCommandHandler
        extends WorkflowCommandHandler<CompensateOriginationCommand, CompensateOriginationCommandHandler.Data> {

    record Data(CompensateOriginationCommand command, Unit prepared) {}

    private final TradeLoanFacilityRepository facilityRepository;
    private final InstallmentScheduleRepository scheduleRepository;
    private final Clock clock;
    private final Workflow<Data> workflow;

    CompensateOriginationCommandHandler(
            WorkflowEngine engine,
            TradeLoanFacilityRepository facilityRepository,
            InstallmentScheduleRepository scheduleRepository,
            Clock clock) {
        super(engine);
        this.facilityRepository = facilityRepository;
        this.scheduleRepository = scheduleRepository;
        this.clock = clock;
        this.workflow = Workflow.singleWrite(
                "compensate-origination",
                ctx -> StepResult.fromWriteResult(write(ctx.data().command(), ctx.data().prepared())));
    }

    @Override
    protected Workflow<Data> workflow() {
        return workflow;
    }

    @Override
    protected Result<Data> seed(CompensateOriginationCommand command) {
        return prepare(command).map(prepared -> new Data(command, prepared));
    }

    private Result<Unit> prepare(CompensateOriginationCommand command) {
        log.warn("Compensating origination for facility: {}", command.loanFacilityId());
        return Result.success();
    }

    private Result<List<DomainEvent<?>>> write(CompensateOriginationCommand command, Unit prepared) {
        return Result.fromOptional(
                        facilityRepository.findById(LoanFacilityId.of(command.loanFacilityId())),
                        () -> FailureCause.notFound(Notification.ofError(
                                TradeLoanApplicationServiceErrors.FACILITY_NOT_FOUND, command.loanFacilityId())))
                .flatMap(facility -> revertOriginationAndSchedule(facility, command));
    }

    private Result<List<DomainEvent<?>>> revertOriginationAndSchedule(
            TradeLoanFacility facility, CompensateOriginationCommand command) {

        List<DomainEvent<?>> allEvents = new ArrayList<>();

        return facility.revertOrigination(clock, command.reason()).flatMap(ignored -> {
            facilityRepository.save(facility);
            allEvents.addAll(facility.domainEvents());

            return revertInstallmentScheduleIfPresent(
                    facility.getInstallmentScheduleId().get(), allEvents);
        });
    }

    private Result<List<DomainEvent<?>>> revertInstallmentScheduleIfPresent(
            InstallmentScheduleId installmentScheduleId, List<DomainEvent<?>> allEvents) {

        if (installmentScheduleId == null) {
            return Result.success(allEvents);
        }

        return scheduleRepository
                .findById(installmentScheduleId)
                .map(schedule -> revertSchedule(schedule, allEvents))
                .orElseGet(() -> {
                    log.warn("InstallmentSchedule not found for compensation: {}", installmentScheduleId);
                    return Result.success(allEvents);
                });
    }

    private Result<List<DomainEvent<?>>> revertSchedule(InstallmentSchedule schedule, List<DomainEvent<?>> allEvents) {

        return schedule.revertScheduleCreation(clock, "Origination compensation")
                .flatMap(ignored -> {
                    scheduleRepository.save(schedule);
                    allEvents.addAll(schedule.domainEvents());
                    log.info(
                            "Reverted installment schedule: {}",
                            schedule.getId().value());
                    return Result.success(allEvents);
                })
                .recover(notification -> {
                    log.warn("Failed to revert schedule during origination compensation: {}", notification);
                    return Result.success(allEvents);
                });
    }
}
