package ir.dotin.loan.trade.core.application.service.irregularprogressivedisbursement.step;

import org.springframework.stereotype.Component;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.platform.pangaea.commons.domain.vo.CurrencyType;
import ir.dotin.platform.pangaea.commons.domain.vo.Money;
import ir.dotin.platform.pangaea.workflow.api.context.WorkflowContext;
import ir.dotin.platform.pangaea.workflow.api.definition.ReadActivity;
import ir.dotin.platform.pangaea.workflow.api.model.StepResult;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.entity.InstallmentSchedule;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.enums.InstallmentScheduleStatus;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.trade.core.application.service.irregularprogressivedisbursement.workflow.IrregularDisbursementData;
import ir.dotin.loan.trade.core.application.service.shared.disbursement.FacilityDependencyLoader;
import ir.dotin.loan.trade.core.application.service.shared.error.TradeLoanApplicationServiceErrors;

import lombok.RequiredArgsConstructor;

import static java.util.Objects.requireNonNull;

@Component
@RequiredArgsConstructor
public class ValidateFacilityStep implements ReadActivity<IrregularDisbursementData> {

    private final FacilityDependencyLoader dependencyLoader;

    public StepResult<Void> execute(WorkflowContext<IrregularDisbursementData> ctx) {
        var data = ctx.data();

        var validationResult = dependencyLoader
                .loadFacility(LoanFacilityId.of(data.facilityId()))
                .flatMap(facility -> dependencyLoader
                        .loadInstallmentSchedule(facility)
                        .flatMap(schedule -> validateScheduleStatus(schedule)
                                .flatMap(ignored -> facility.validateDisbursementDate(
                                        requireNonNull(data.disbursementDate(), "disbursementDate"),
                                        schedule.getInstallments().getFirst().getDueDate()))
                                .flatMap(
                                        ignored -> facility.validateIrregularTrancheDisbursement(trancheMoney(data)))));

        return StepResult.fromResult(validationResult);
    }

    private Result<Unit> validateScheduleStatus(InstallmentSchedule schedule) {
        boolean isFirstDisbursement = schedule.getScheduleHistory().count() == 0;
        InstallmentScheduleStatus currentStatus = schedule.getStatus();

        if (isFirstDisbursement) {
            if (currentStatus != InstallmentScheduleStatus.DRAFT) {
                return Result.failure(
                        TradeLoanApplicationServiceErrors.INVALID_SCHEDULE_STATUS_FOR_FIRST_DISBURSEMENT,
                        currentStatus);
            }
        } else {
            if (currentStatus != InstallmentScheduleStatus.ACTIVE) {
                return Result.failure(
                        TradeLoanApplicationServiceErrors.INVALID_SCHEDULE_STATUS_FOR_SUBSEQUENT_DISBURSEMENT,
                        currentStatus);
            }
        }

        return Result.success();
    }

    private Money trancheMoney(IrregularDisbursementData data) {
        CurrencyType currencyType = CurrencyType.valueOf(data.currencyCode()).unwrap();
        return Money.valueOf(data.trancheAmount(), currencyType).unwrap();
    }
}
