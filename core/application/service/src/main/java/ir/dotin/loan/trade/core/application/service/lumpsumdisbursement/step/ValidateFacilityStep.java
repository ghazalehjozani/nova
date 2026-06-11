package ir.dotin.loan.trade.core.application.service.lumpsumdisbursement.step;

import org.springframework.stereotype.Component;

import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.platform.pangaea.workflow.api.context.WorkflowContext;
import ir.dotin.platform.pangaea.workflow.api.definition.ReadActivity;
import ir.dotin.platform.pangaea.workflow.api.model.StepResult;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.entity.InstallmentSchedule;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.DisbursementMethod;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.trade.core.application.service.lumpsumdisbursement.workflow.LumpSumData;
import ir.dotin.loan.trade.core.application.service.shared.disbursement.FacilityDependencyLoader;
import ir.dotin.loan.trade.core.application.service.shared.error.TradeLoanApplicationServiceErrors;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;

import lombok.RequiredArgsConstructor;

@Component("lumpSumValidateFacilityStep")
@RequiredArgsConstructor
public class ValidateFacilityStep implements ReadActivity<LumpSumData> {

    private final FacilityDependencyLoader dependencyLoader;

    @Override
    public StepResult<Void> execute(WorkflowContext<LumpSumData> ctx) {
        var data = ctx.data();

        var validationResult = dependencyLoader
                .loadFacility(LoanFacilityId.of(data.facilityId()))
                .flatMap(this::validateDisbursementMethod)
                .flatMap(facility -> dependencyLoader
                        .loadInstallmentSchedule(facility)
                        .flatMap(schedule -> validateDisbursement(facility, schedule, data)));

        return StepResult.fromResult(validationResult);
    }

    private Result<TradeLoanFacility> validateDisbursementMethod(TradeLoanFacility facility) {
        return facility.getSanctionedLoan()
                .filter(sl -> sl.getDisbursementMethod() == DisbursementMethod.LUMP_SUM)
                .map(ignored -> Result.success(facility))
                .orElseGet(() -> Result.failure(Notification.ofError(
                        TradeLoanApplicationServiceErrors.INVALID_DISBURSEMENT_METHOD,
                        facility.getSanctionedLoan()
                                .map(sl -> sl.getDisbursementMethod() != null
                                        ? sl.getDisbursementMethod().name()
                                        : "null")
                                .orElse("UNKNOWN"))));
    }

    private Result<Unit> validateDisbursement(
            TradeLoanFacility facility, InstallmentSchedule schedule, LumpSumData data) {
        if (facility.getSanctionedLoan().isEmpty()) {
            return Result.failure(Notification.ofError(
                    TradeLoanApplicationServiceErrors.SANCTIONED_LOAN_NOT_FOUND,
                    facility.getId().value()));
        }
        var sanctionedLoan = facility.getSanctionedLoan().get();
        return facility.validateLumpSumDisbursement(sanctionedLoan.getApprovedAmount())
                .flatMap(ignored -> facility.validateDisbursementDate(
                        data.disbursementDate(),
                        schedule.getInstallments().getFirst().getDueDate()));
    }
}
