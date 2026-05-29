package ir.dotin.loan.trade.core.domain.loanfacility.service;

import java.time.Clock;
import java.util.List;

import org.jspecify.annotations.Nullable;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.platform.pangaea.commons.domain.annotation.DomainService;
import ir.dotin.loan.baseloan.core.domain.loanfacility.entity.AbstractSanctionedLoan;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.FacilityStatus;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.SanctionType;
import ir.dotin.loan.baseloan.core.domain.loanfacility.service.AbstractLoanFacilityService;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.Collateral;
import ir.dotin.loan.baseloan.core.domain.shared.vo.ConfirmType;
import ir.dotin.loan.baseloan.core.domain.shared.vo.SanctionSerial;
import ir.dotin.loan.baseloan.core.domain.shared.vo.TrackedTransactionNumber;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanApplication;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeSanctionedLoan;
import ir.dotin.loan.trade.core.domain.loanfacility.error.TradeLoanFacilityErrors;

@DomainService
public class TradeLoanFacilityService
        extends AbstractLoanFacilityService<TradeLoanApplication, TradeSanctionedLoan, TradeLoanFacility> {

    public TradeLoanFacilityService(Clock clock) {
        super(clock);
    }

    @Override
    protected Result<Unit> validateTransactionNumbers(
            TradeLoanFacility facility, TrackedTransactionNumber transactionNumbers) {

        if (transactionNumbers == null) {
            return Result.failure(
                    TradeLoanFacilityErrors.BUILDER_VALIDATION_FAILED,
                    "Transaction numbers cannot be null for trade loans");
        }

        return Result.success();
    }

    @Override
    protected boolean canAddCollateral(TradeLoanFacility facility, List<Collateral> collaterals) {
        // Trade loans allow collateral in approved or contract issued states
        var status = facility.getCurrentState();
        return status == FacilityStatus.APPROVED
                || status == FacilityStatus.ISSUE_CONTRACT
                || status == FacilityStatus.FULLY_DISBURSED;
    }

    @Override
    protected boolean canCancel(TradeLoanFacility facility) {
        var status = facility.getCurrentState();
        return status != FacilityStatus.CLOSED_PAID_OFF
                && status != FacilityStatus.CANCELLED
                && status != FacilityStatus.CLOSED_DEFAULTED;
    }

    @Override
    protected Result<Unit> verifyZeroBalance(TradeLoanFacility facility) {
        // Trade-specific balance verification
        // For now, return success as a placeholder
        return Result.success();
    }

    @Override
    protected Result<Unit> verifyDefaultConditions(TradeLoanFacility facility) {
        // Trade-specific default condition verification
        // For now, return success as a placeholder
        return Result.success();
    }

    @Override
    protected Result<Unit> performPreApprovalChecks(
            TradeLoanFacility facility, AbstractSanctionedLoan.@Nullable AbstractSanctionedLoanBuilder<?, ?> builder) {
        return Result.success();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Result<TradeSanctionedLoan.Builder> createSanctionedLoanFromApplication(
            TradeLoanApplication loanApplication, ConfirmType confirmType) {

        // Create a TradeSanctionedLoan.Builder from the loan application data
        var builder = TradeSanctionedLoan.builder()
                .sanctionSerial(SanctionSerial.of("AUTO_GENERATED-" + System.currentTimeMillis(), SanctionType.GENERAL)
                        .unwrap())
                .approvedAmount(loanApplication.getRequestedAmount())
                .gracePeriod(loanApplication.getGracePeriod())
                .installmentCount(loanApplication.getInstallmentCount())
                .loanDuration(loanApplication.getRequestedLoanDuration())
                .disbursementMethod(loanApplication.getDisbursementMethod())
                .confirmType(confirmType);

        return Result.success(builder);
    }
}
