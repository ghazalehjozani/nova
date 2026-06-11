package ir.dotin.loan.trade.core.application.service.lumpsumdisbursement.step;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Component;

import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.platform.pangaea.commons.domain.event.DomainEvent;
import ir.dotin.platform.pangaea.workflow.api.context.WorkflowContext;
import ir.dotin.platform.pangaea.workflow.api.definition.Compensable;
import ir.dotin.platform.pangaea.workflow.api.definition.WriteActivity;
import ir.dotin.platform.pangaea.workflow.api.model.StepResult;
import ir.dotin.loan.baseloan.core.domain.loanfacility.entity.AbstractSanctionedLoan;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.TrackedTransactionNumber;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.InstallmentScheduleRepository;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.application.service.lumpsumdisbursement.workflow.LumpSumData;
import ir.dotin.loan.trade.core.application.service.shared.disbursement.FacilityDependencyLoader;
import ir.dotin.loan.trade.core.application.service.shared.disbursement.PostedTransaction;
import ir.dotin.loan.trade.core.application.service.shared.disbursement.TransactionPostingSupport;
import ir.dotin.loan.trade.core.application.service.shared.error.TradeLoanApplicationServiceErrors;
import ir.dotin.loan.trade.core.domain.loanarrangement.entity.TradeLoanArrangement;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component("lumpSumApplyDisbursementStep")
@RequiredArgsConstructor
public class ApplyDisbursementStep implements WriteActivity<LumpSumData>, Compensable<LumpSumData> {

    private final FacilityDependencyLoader dependencyLoader;
    private final TransactionPostingSupport transactionPostingSupport;
    private final TradeLoanFacilityRepository facilityRepository;
    private final InstallmentScheduleRepository installmentScheduleRepository;
    private final Clock clock;

    @Override
    public StepResult<List<DomainEvent<?>>> execute(WorkflowContext<LumpSumData> ctx) {
        var data = ctx.data();

        var result = dependencyLoader
                .loadFacility(LoanFacilityId.of(data.facilityId()))
                .flatMap(facility -> dependencyLoader
                        .loadLoanArrangement(facility)
                        .flatMap(arrangement -> dependencyLoader
                                .loadInstallmentSchedule(facility)
                                .flatMap(schedule -> schedule.activateSchedule(clock)
                                        .flatMap(ignored -> disburse(facility, arrangement, data))
                                        .map(ignored -> {
                                            List<DomainEvent<?>> events = new ArrayList<>();
                                            events.addAll(schedule.domainEvents());
                                            events.addAll(facility.domainEvents());

                                            installmentScheduleRepository.save(schedule);
                                            facilityRepository.save(facility, data.expectedVersion());

                                            log.info("Lump sum disbursement applied: facilityId={}", data.facilityId());
                                            return events;
                                        }))));

        return StepResult.fromWriteResult(result);
    }

    @Override
    public StepResult<Void> compensate(WorkflowContext<LumpSumData> ctx) {
        var data = ctx.data();

        var facilityResult = dependencyLoader.loadFacility(LoanFacilityId.of(data.facilityId()));
        if (facilityResult.isFailure()) {
            return StepResult.fromResult(facilityResult);
        }

        var facility = facilityResult.unwrap();
        var revertResult = facility.revertLumpSumDisbursement(clock);
        if (revertResult.isFailure()) {
            return StepResult.fromResult(revertResult);
        }

        facilityRepository.save(facility);

        log.warn("Reverted lump sum disbursement: facilityId={}", data.facilityId());
        return new StepResult.Success<>(null);
    }

    private Result<Unit> disburse(TradeLoanFacility facility, TradeLoanArrangement arrangement, LumpSumData data) {

        List<TrackedTransactionNumber> trackedNumbers = rebuildTrackedNumbers(data);

        return facility.getSanctionedLoan()
                .map(AbstractSanctionedLoan::getApprovedAmount)
                .map(approvedAmount -> facility.lumpSumDisbursement(
                                approvedAmount,
                                trackedNumbers,
                                data.getResolvedAccounts().accountsByRelationType(),
                                Objects.requireNonNull(arrangement.getInstallmentPolicy())
                                        .installmentPaymentType(),
                                clock,
                                data.disbursementDate())
                        .map(ignored -> Unit.INSTANCE))
                .orElseGet(() -> Result.failure(Notification.ofError(
                        TradeLoanApplicationServiceErrors.SANCTIONED_LOAN_NOT_FOUND,
                        facility.getId().value())));
    }

    private List<TrackedTransactionNumber> rebuildTrackedNumbers(LumpSumData data) {
        if (data.postedTransactions() == null) {
            return List.of();
        }
        List<PostedTransaction> posted = data.postedTransactions().stream()
                .map(p -> new PostedTransaction(p.transactionNumber(), p.trackingId(), p.status()))
                .toList();
        return transactionPostingSupport.rebuildTrackedNumbers(posted);
    }
}
