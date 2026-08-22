package ir.dotin.loan.trade.core.application.service.plansingleinstallmentschedule.step;

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
import ir.dotin.loan.baseloan.core.domain.installmentschedule.enums.InstallmentScheduleType;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.vo.InstallmentScheduleCreationContext;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.vo.SchedulePlanRequest;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.vo.ProductProfile;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.trade.core.application.ports.inbound.command.PlanSingleInstallmentScheduleCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.InstallmentScheduleRepository;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanArrangementRepository;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.application.service.shared.error.TradeLoanApplicationServiceErrors;
import ir.dotin.loan.trade.core.domain.installmentschedule.service.TradeRepaymentSchedulingService;
import ir.dotin.loan.trade.core.domain.loanarrangement.entity.TradeLoanArrangement;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static java.util.Objects.requireNonNull;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlanSingleScheduleStep implements PublishingWriteActivity<PlanSingleScheduleData> {

    private final InstallmentScheduleRepository installmentScheduleRepository;
    private final TradeLoanFacilityRepository tradeLoanFacilityRepository;
    private final TradeLoanArrangementRepository tradeLoanArrangementRepository;
    private final TradeRepaymentSchedulingService schedulingService;
    private final Clock clock;

    @Override
    public StepResult<List<DomainEvent<?>>> execute(WorkflowContext<PlanSingleScheduleData> ctx) {
        return StepResult.fromWriteResult(write(ctx.data().command()));
    }

    // why: one aggregate, one short transaction — the write touches only InstallmentSchedule and publishes
    // through the existing outbox, per pangaea ADR-0025.
    private Result<List<DomainEvent<?>>> write(PlanSingleInstallmentScheduleCommand command) {
        return loadDependencies(command)
                .flatMap(this::planSchedule)
                .onSuccess(installmentScheduleRepository::save)
                .onSuccess(schedule -> log.info("Single installment schedule created: {}", schedule.getId()))
                .map(AbstractAggregateRoot::domainEvents);
    }

    private Result<ScheduleCreationDependencies> loadDependencies(PlanSingleInstallmentScheduleCommand command) {
        Result<TradeLoanFacility> facility = Result.fromOptional(
                tradeLoanFacilityRepository.findById(LoanFacilityId.of(command.loanFacilityId())),
                FailureCause.notFound(Notification.ofError(
                        TradeLoanApplicationServiceErrors.FACILITY_NOT_FOUND, command.loanFacilityId())));

        return facility.flatMap(f -> Result.fromOptional(
                        tradeLoanArrangementRepository.findById(f.getLoanArrangementId()),
                        FailureCause.notFound(Notification.ofError(
                                TradeLoanApplicationServiceErrors.LOAN_ARRANGEMENT_NOT_FOUND,
                                f.getLoanArrangementId().value())))
                .map(a -> new ScheduleCreationDependencies(f, a)));
    }

    private Result<InstallmentSchedule> planSchedule(ScheduleCreationDependencies dependencies) {
        InstallmentScheduleCreationContext context =
                new InstallmentScheduleCreationContext(dependencies.facility(), dependencies.arrangement(), clock);

        return ProductProfile.from(
                        dependencies.arrangement().getDisbursementType(),
                        requireNonNull(dependencies.arrangement().getInstallmentPolicy())
                                .installmentPaymentType())
                .flatMap(profile -> schedulingService.planSchedule(
                        context, profile, SchedulePlanRequest.of(InstallmentScheduleType.SINGLE_INSTALLMENT)));
    }

    private record ScheduleCreationDependencies(TradeLoanFacility facility, TradeLoanArrangement arrangement) {}
}
