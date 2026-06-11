package ir.dotin.loan.trade.core.application.service.planequalinstallmentschedule.commandhandler;

import java.time.Clock;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.platform.pangaea.commons.core.error.FailureCause;
import ir.dotin.platform.pangaea.commons.domain.entity.AbstractAggregateRoot;
import ir.dotin.platform.pangaea.commons.domain.event.DomainEvent;
import ir.dotin.platform.pangaea.workflow.api.command.WorkflowCommandHandler;
import ir.dotin.platform.pangaea.workflow.api.definition.Workflow;
import ir.dotin.platform.pangaea.workflow.api.definition.WorkflowRoute;
import ir.dotin.platform.pangaea.workflow.api.engine.WorkflowEngine;
import ir.dotin.platform.pangaea.workflow.api.model.StepResult;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.entity.InstallmentSchedule;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.vo.InstallmentScheduleCreationContext;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.trade.core.application.ports.inbound.command.PlanEqualInstallmentScheduleCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.InstallmentScheduleRepository;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanArrangementRepository;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.application.service.shared.error.TradeLoanApplicationServiceErrors;
import ir.dotin.loan.trade.core.domain.installmentschedule.service.TradeRepaymentSchedulingService;
import ir.dotin.loan.trade.core.domain.loanarrangement.entity.TradeLoanArrangement;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;

@Service
public final class PlanEqualInstallmentScheduleCommandHandler
        extends WorkflowCommandHandler<
                PlanEqualInstallmentScheduleCommand, PlanEqualInstallmentScheduleCommandHandler.Data> {

    private static final Logger log = LoggerFactory.getLogger(PlanEqualInstallmentScheduleCommandHandler.class);

    @Override
    protected Workflow<Data> route(WorkflowRoute<Data> route) {
        return route.singleWrite(
                "plan-equal-installment-schedule",
                ctx -> StepResult.fromWriteResult(
                        write(ctx.data().command(), ctx.data().prepared())));
    }

    @Override
    protected Result<Data> seed(PlanEqualInstallmentScheduleCommand command) {
        return prepare(command).map(prepared -> new Data(command, prepared));
    }

    private Result<Unit> prepare(PlanEqualInstallmentScheduleCommand command) {
        return Result.success();
    }

    private Result<List<DomainEvent<?>>> write(PlanEqualInstallmentScheduleCommand command, Unit prepared) {
        return loadDependencies(command)
                .flatMap(this::planSchedule)
                .onSuccess(installmentScheduleRepository::save)
                .onSuccess(installmentSchedule ->
                        log.info("Equal installment schedule created: {}", installmentSchedule.getId()))
                .map(AbstractAggregateRoot::domainEvents);
    }

    private Result<ScheduleCreationDependencies> loadDependencies(PlanEqualInstallmentScheduleCommand command) {
        Result<TradeLoanFacility> facility = Result.fromOptional(
                tradeLoanFacilityRepository.findById(LoanFacilityId.of(command.loanFacilityId())),
                FailureCause.notFound(Notification.ofError(
                        TradeLoanApplicationServiceErrors.FACILITY_NOT_FOUND, command.loanFacilityId())));

        return facility.flatMap(f -> {
            Result<TradeLoanArrangement> arrangement = Result.fromOptional(
                    tradeLoanArrangementRepository.findById(f.getLoanArrangementId()),
                    FailureCause.notFound(Notification.ofError(
                            TradeLoanApplicationServiceErrors.LOAN_ARRANGEMENT_NOT_FOUND,
                            f.getLoanArrangementId().value())));

            return arrangement.map(a -> new ScheduleCreationDependencies(f, a));
        });
    }

    private Result<InstallmentSchedule> planSchedule(ScheduleCreationDependencies dependencies) {
        InstallmentScheduleCreationContext context =
                new InstallmentScheduleCreationContext(dependencies.facility(), dependencies.arrangement(), clock);
        return schedulingService.planEqualInstallmentSchedule(context);
    }

    record Data(PlanEqualInstallmentScheduleCommand command, Unit prepared) {}

    private record ScheduleCreationDependencies(TradeLoanFacility facility, TradeLoanArrangement arrangement) {}

    private final InstallmentScheduleRepository installmentScheduleRepository;
    private final TradeLoanFacilityRepository tradeLoanFacilityRepository;
    private final TradeLoanArrangementRepository tradeLoanArrangementRepository;
    private final TradeRepaymentSchedulingService schedulingService;
    private final Clock clock;

    public PlanEqualInstallmentScheduleCommandHandler(
            WorkflowEngine engine,
            InstallmentScheduleRepository installmentScheduleRepository,
            TradeLoanFacilityRepository tradeLoanFacilityRepository,
            TradeLoanArrangementRepository tradeLoanArrangementRepository,
            TradeRepaymentSchedulingService schedulingService,
            Clock clock) {
        super(engine);
        this.installmentScheduleRepository = installmentScheduleRepository;
        this.tradeLoanFacilityRepository = tradeLoanFacilityRepository;
        this.tradeLoanArrangementRepository = tradeLoanArrangementRepository;
        this.schedulingService = schedulingService;
        this.clock = clock;
    }
}
