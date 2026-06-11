package ir.dotin.loan.trade.core.application.service.irregularprogressivedisbursement.step;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.platform.pangaea.commons.domain.event.DomainEvent;
import ir.dotin.platform.pangaea.commons.domain.vo.Money;
import ir.dotin.platform.pangaea.workflow.api.context.WorkflowContext;
import ir.dotin.platform.pangaea.workflow.api.definition.Compensable;
import ir.dotin.platform.pangaea.workflow.api.definition.WriteActivity;
import ir.dotin.platform.pangaea.workflow.api.model.StepResult;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.entity.Installment;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.entity.InstallmentSchedule;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.TrackedTransactionNumber;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.InstallmentScheduleRepository;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.application.service.irregularprogressivedisbursement.workflow.IrregularDisbursementData;
import ir.dotin.loan.trade.core.application.service.shared.disbursement.FacilityDependencyLoader;
import ir.dotin.loan.trade.core.application.service.shared.disbursement.PostedTransaction;
import ir.dotin.loan.trade.core.application.service.shared.disbursement.TransactionPostingSupport;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static java.util.Objects.requireNonNull;

@Slf4j
@Component("irregularApplyDisbursementStep")
@RequiredArgsConstructor
public class ApplyDisbursementStep
        implements WriteActivity<IrregularDisbursementData>, Compensable<IrregularDisbursementData> {

    private final FacilityDependencyLoader dependencyLoader;
    private final TransactionPostingSupport transactionPostingSupport;
    private final IrregularPlanRecalculator planRecalculator;
    private final TradeLoanFacilityRepository facilityRepository;
    private final InstallmentScheduleRepository installmentScheduleRepository;
    private final Clock clock;

    public StepResult<List<DomainEvent<?>>> execute(WorkflowContext<IrregularDisbursementData> ctx) {
        var data = ctx.data();

        var result = dependencyLoader
                .loadFacility(LoanFacilityId.of(data.facilityId()))
                .flatMap(facility -> dependencyLoader
                        .loadInstallmentSchedule(facility)
                        .flatMap(schedule -> planRecalculator
                                .recalculateAndVerify(facility, schedule, data)
                                .flatMap(installments -> restructureAndActivate(facility, schedule, installments, data)
                                        .flatMap(newSchedule -> disburse(facility, newSchedule, data)
                                                .map(ignored -> {
                                                    installmentScheduleRepository.save(schedule);
                                                    installmentScheduleRepository.save(newSchedule);
                                                    facilityRepository.save(facility, data.expectedVersion());

                                                    List<DomainEvent<?>> events = new ArrayList<>();
                                                    events.addAll(schedule.domainEvents());
                                                    events.addAll(newSchedule.domainEvents());
                                                    events.addAll(facility.domainEvents());

                                                    log.info(
                                                            "Irregular disbursement applied: facilityId={}",
                                                            data.facilityId());
                                                    return events;
                                                })))));

        return StepResult.fromWriteResult(result);
    }

    public StepResult<Void> compensate(WorkflowContext<IrregularDisbursementData> ctx) {
        var data = ctx.data();

        var facilityResult = dependencyLoader.loadFacility(LoanFacilityId.of(data.facilityId()));
        if (facilityResult.isFailure()) {
            return StepResult.fromResult(facilityResult);
        }

        var facility = facilityResult.unwrap();
        var revertResult = facility.revertIrregularTrancheDisbursement(clock);
        if (revertResult.isFailure()) {
            return StepResult.fromResult(revertResult);
        }

        facilityRepository.save(facility);

        log.warn("Reverted irregular disbursement: facilityId={}", data.facilityId());
        return new StepResult.Success<>(null);
    }

    private Result<InstallmentSchedule> restructureAndActivate(
            TradeLoanFacility facility,
            InstallmentSchedule schedule,
            List<Installment> recalculatedInstallments,
            IrregularDisbursementData data) {

        Money tranche = planRecalculator.trancheMoney(data);
        String reason = String.format("Tranche %d disbursement: %s", data.trancheNumber(), tranche.value());
        Money totalTranche = facility.getTotalDisbursedAmount()
                .add(tranche)
                .unwrapOrThrow(c -> new IllegalStateException("Creating zero Money failed unexpectedly."));

        return schedule.restructureSchedule(
                        recalculatedInstallments,
                        reason,
                        totalTranche,
                        requireNonNull(
                                facility.getSanctionedLoan().orElseThrow().getApprovedAmount()),
                        clock,
                        requireNonNull(data.transactionConfig().userId(), "userId"))
                .flatMap(newSchedule -> newSchedule.activateSchedule(clock).map(ignored -> newSchedule));
    }

    private Result<Unit> disburse(
            TradeLoanFacility facility, InstallmentSchedule newSchedule, IrregularDisbursementData data) {

        return facility.disburseIrregularTranche(
                        planRecalculator.trancheMoney(data),
                        rebuildTrackedNumbers(data),
                        data.getPostedAccountIds(),
                        newSchedule.getId(),
                        clock,
                        requireNonNull(data.transactionConfig().userId(), "userId"),
                        requireNonNull(data.disbursementDate(), "disbursementDate"))
                .map(ignored -> Unit.INSTANCE);
    }

    private List<TrackedTransactionNumber> rebuildTrackedNumbers(IrregularDisbursementData data) {
        if (data.postedTransactions() == null) {
            return List.of();
        }
        List<PostedTransaction> posted = data.postedTransactions().stream()
                .map(p -> new PostedTransaction(p.transactionNumber(), p.trackingId(), p.status()))
                .toList();
        return transactionPostingSupport.rebuildTrackedNumbers(posted);
    }
}
