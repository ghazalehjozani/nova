package ir.dotin.loan.trade.core.domain.loanfacility.service;

import java.time.Clock;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.commons.domain.annotation.DomainService;
import ir.dotin.loan.baseloan.core.domain.loanfacility.entity.AbstractSanctionedLoan;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.FacilityStatus;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.SanctionType;
import ir.dotin.loan.baseloan.core.domain.loanfacility.service.AbstractLoanFacilityService;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.CollateralSerial;
import ir.dotin.loan.baseloan.core.domain.shared.vo.SanctionSerial;
import ir.dotin.loan.baseloan.core.domain.shared.vo.TrackedTransactionNumber;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanApplication;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeSanctionedLoan;
import ir.dotin.loan.trade.core.domain.loanfacility.i18n.TradeLoanFacilityLocalizedMessageCodes;

@DomainService
public class TradeLoanFacilityService
        extends AbstractLoanFacilityService<TradeLoanApplication, TradeSanctionedLoan, TradeLoanFacility> {

    public TradeLoanFacilityService(Clock clock) {
        super(clock);
    }

    @Override
    protected Result<Void> validateTransactionNumbers(
            TradeLoanFacility facility, TrackedTransactionNumber transactionNumbers) {

        if (transactionNumbers == null) {
            return Result.failure(Notification.ofError(
                    TradeLoanFacilityLocalizedMessageCodes.BUILDER_VALIDATION_FAILED,
                    "Transaction numbers cannot be null for trade loans"));
        }

        return Result.success();
    }

    @Override
    protected boolean canAddCollateral(TradeLoanFacility facility, CollateralSerial collateralSerial) {
        // Trade loans allow collateral in approved or contract issued states
        var status = facility.getCurrentState();
        return status == FacilityStatus.APPROVED || status == FacilityStatus.ISSUE_CONTRACT;
    }

    @Override
    protected boolean canCancel(TradeLoanFacility facility) {
        // Trade loans can be cancelled if not yet disbursed
        var status = facility.getCurrentState();
        return status != FacilityStatus.FULLY_DISBURSED
                && status != FacilityStatus.CLOSED_PAID_OFF
                && status != FacilityStatus.CLOSED_DEFAULTED;
    }

    @Override
    protected Result<Void> verifyZeroBalance(TradeLoanFacility facility) {
        // Trade-specific balance verification
        // For now, return success as a placeholder
        return Result.success();
    }

    @Override
    protected Result<Void> verifyDefaultConditions(TradeLoanFacility facility) {
        // Trade-specific default condition verification
        // For now, return success as a placeholder
        return Result.success();
    }

    @Override
    protected Result<Void> performPreApprovalChecks(
            TradeLoanFacility facility, AbstractSanctionedLoan.AbstractSanctionedLoanBuilder<?, ?> builder) {
        return Result.success();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Result<TradeSanctionedLoan.Builder> createSanctionedLoanFromApplication(
            TradeLoanApplication loanApplication) {

        // Create a TradeSanctionedLoan.Builder from the loan application data
        var builder = TradeSanctionedLoan.builder()
                .sanctionSerial(SanctionSerial.of("AUTO_GENERATED-" + System.currentTimeMillis(), SanctionType.GENERAL)
                        .orElseThrow())
                .approvedAmount(loanApplication.getRequestedAmount())
                .gracePeriod(loanApplication.getGracePeriod())
                .installmentCount(loanApplication.getInstallmentCount())
                .loanDuration(loanApplication.getRequestedLoanDuration())
                .disbursementMethod(loanApplication.getDisbursementMethod());

        return Result.success(builder);
    }
}
