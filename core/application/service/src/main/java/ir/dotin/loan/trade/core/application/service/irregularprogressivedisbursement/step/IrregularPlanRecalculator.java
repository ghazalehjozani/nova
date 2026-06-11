package ir.dotin.loan.trade.core.application.service.irregularprogressivedisbursement.step;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.domain.vo.CurrencyType;
import ir.dotin.platform.pangaea.commons.domain.vo.Money;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.entity.Installment;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.entity.InstallmentSchedule;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.service.InstallmentRecalculationService;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.vo.InstallmentSpec;
import ir.dotin.loan.trade.core.application.service.irregularprogressivedisbursement.workflow.IrregularDisbursementData;
import ir.dotin.loan.trade.core.application.service.irregularprogressivedisbursement.workflow.IrregularDisbursementData.InstallmentSpecData;
import ir.dotin.loan.trade.core.application.service.shared.error.TradeLoanApplicationServiceErrors;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class IrregularPlanRecalculator {

    private final InstallmentRecalculationService recalculationService;

    public Result<List<Installment>> recalculateAndVerify(
            TradeLoanFacility facility, InstallmentSchedule schedule, IrregularDisbursementData data) {

        Result<List<InstallmentSpec>> customPlanResult = toInstallmentSpecs(data.customPlanSpecs(), data);
        if (customPlanResult.isFailure()) {
            return Result.failure(customPlanResult.err().orElseThrow());
        }
        List<InstallmentSpec> customPlan = customPlanResult.unwrap();

        return recalculationService
                .recalculateForIrregularDisbursement(
                        schedule, facility, trancheMoney(data), customPlan.isEmpty() ? null : customPlan)
                .flatMap(installments -> matchesApprovedPlan(installments, data.approvedPlanSpecs())
                        ? Result.success(installments)
                        : Result.failure(Notification.ofError(
                                TradeLoanApplicationServiceErrors.DISBURSEMENT_PLAN_DRIFT,
                                facility.getId().value())));
    }

    public Money trancheMoney(IrregularDisbursementData data) {
        CurrencyType currencyType = CurrencyType.valueOf(data.currencyCode()).unwrap();
        return Money.valueOf(data.trancheAmount(), currencyType).unwrap();
    }

    private boolean matchesApprovedPlan(List<Installment> installments, List<InstallmentSpecData> approvedPlan) {
        if (installments.size() != approvedPlan.size()) {
            return false;
        }
        List<Installment> sorted = installments.stream()
                .sorted(Comparator.comparingInt(Installment::getSequenceNumber))
                .toList();
        List<InstallmentSpecData> approved = approvedPlan.stream()
                .sorted(Comparator.comparingInt(InstallmentSpecData::sequenceNumber))
                .toList();
        for (int i = 0; i < sorted.size(); i++) {
            Installment installment = sorted.get(i);
            InstallmentSpecData spec = approved.get(i);
            if (installment.getSequenceNumber() != spec.sequenceNumber()
                    || !installment.getDueDate().isEqual(spec.dueDate())
                    || installment
                                    .getScheduledAmount()
                                    .principalAmount()
                                    .value()
                                    .compareTo(spec.principalAmount())
                            != 0
                    || installment.getScheduledAmount().interestAmount().value().compareTo(spec.interestAmount())
                            != 0) {
                return false;
            }
        }
        return true;
    }

    private Result<List<InstallmentSpec>> toInstallmentSpecs(
            @Nullable List<InstallmentSpecData> specs, IrregularDisbursementData data) {
        if (specs == null) {
            return Result.success(List.of());
        }
        CurrencyType currencyType = CurrencyType.valueOf(data.currencyCode()).unwrap();
        List<InstallmentSpec> result = new ArrayList<>();
        for (InstallmentSpecData spec : specs) {
            Result<Money> principal = Money.valueOf(spec.principalAmount(), currencyType);
            Result<Money> interest = Money.valueOf(spec.interestAmount(), currencyType);
            if (principal.isFailure()) {
                return Result.failure(principal.err().orElseThrow());
            }
            if (interest.isFailure()) {
                return Result.failure(interest.err().orElseThrow());
            }
            Result<InstallmentSpec> specResult =
                    InstallmentSpec.of(spec.sequenceNumber(), principal.unwrap(), interest.unwrap(), spec.dueDate());
            if (specResult.isFailure()) {
                return Result.failure(specResult.err().orElseThrow());
            }
            result.add(specResult.unwrap());
        }
        return Result.success(result);
    }
}
