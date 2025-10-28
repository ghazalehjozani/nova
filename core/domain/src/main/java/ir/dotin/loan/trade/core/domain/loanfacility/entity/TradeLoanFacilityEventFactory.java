package ir.dotin.loan.trade.core.domain.loanfacility.entity;

import java.time.Clock;
import java.util.List;

import ir.dotin.platform.commons.domain.vo.Money;
import ir.dotin.loan.baseloan.core.domain.loanfacility.entity.LoanFacilityEventFactory;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.ApplicationNumber;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.CollateralSerial;
import ir.dotin.loan.baseloan.core.domain.shared.vo.FailureReason;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanApplicationId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.SanctionSerial;
import ir.dotin.loan.baseloan.core.domain.shared.vo.SanctionedLoanId;
import ir.dotin.loan.trade.core.domain.loanfacility.event.*;

import static java.util.UUID.randomUUID;

final class TradeLoanFacilityEventFactory implements LoanFacilityEventFactory<TradeLoanFacilityEvents<?, ?>> {

    @Override
    public SanctionedLoanId generateSanctionedLoanId() {
        return new SanctionedLoanId(randomUUID());
    }

    @Override
    public TradeLoanFacilityApprovalSubmitted createPendingApprovalEvent(
            LoanFacilityId facilityId, LoanApplicationId applicationId, Clock clock) {
        return TradeLoanFacilityApprovalSubmitted.of(facilityId, applicationId, clock);
    }

    @Override
    public TradeLoanFacilityApproved createApprovedEvent(
            LoanFacilityId facilityId, SanctionedLoanId sanctionId, SanctionSerial sanctionSerial, Clock clock) {
        return TradeLoanFacilityApproved.of(facilityId, sanctionId, sanctionSerial, clock);
    }

    @Override
    public TradeLoanFacilityRejected createRejectedEvent(
            LoanFacilityId facilityId, LoanApplicationId applicationId, Clock clock) {
        return TradeLoanFacilityRejected.of(facilityId, applicationId, clock);
    }

    @Override
    public TradeLoanFacilityContractIssued createContractIssuedEvent(
            LoanFacilityId facilityId, SanctionedLoanId sanctionId, List<String> transactionNumbers, Clock clock) {
        return TradeLoanFacilityContractIssued.of(facilityId, sanctionId, transactionNumbers, clock);
    }

    @Override
    public TradeLoanFacilityDisbursementFailed createDisbursementFailedEvent(
            LoanFacilityId facilityId, SanctionedLoanId sanctionId, FailureReason reason, Clock clock) {
        return TradeLoanFacilityDisbursementFailed.of(facilityId, sanctionId, reason, clock);
    }

    @Override
    public TradeLoanFacilityActivated createActivatedEvent(
            LoanFacilityId facilityId, SanctionedLoanId sanctionId, Clock clock) {
        return TradeLoanFacilityActivated.of(facilityId, sanctionId, clock);
    }

    @Override
    public TradeLoanFacilityPaidOffClosed createClosedPaidOffEvent(
            LoanFacilityId facilityId, SanctionedLoanId sanctionId, Clock clock) {
        return TradeLoanFacilityPaidOffClosed.of(facilityId, sanctionId, clock);
    }

    @Override
    public TradeLoanFacilityClosedDefaulted createClosedDefaultedEvent(
            LoanFacilityId facilityId, SanctionedLoanId sanctionId, Clock clock) {
        return TradeLoanFacilityClosedDefaulted.of(facilityId, sanctionId, clock);
    }

    @Override
    public TradeLoanFacilityCancelled createCancelledEvent(LoanFacilityId facilityId, Clock clock) {
        return TradeLoanFacilityCancelled.of(facilityId, clock);
    }

    @Override
    public TradeLoanFacilityCollateralAdded createCollateralAddedEvent(
            LoanFacilityId facilityId, SanctionedLoanId sanctionId, CollateralSerial collateralSerial, Clock clock) {
        return TradeLoanFacilityCollateralAdded.of(facilityId, sanctionId, collateralSerial, clock);
    }

    @Override
    public TradeLoanFacilityCreated createCreatedEvent(
            LoanFacilityId facilityId, ApplicationNumber applicationNumber, Clock clock) {
        return TradeLoanFacilityCreated.of(
                facilityId, facilityId, applicationNumber.formattedApplicationNumber(), clock);
    }

    @Override
    public TradeLoanFacilityIrregularlyDisbursed createIrregularDisbursementRequestedEvent(
            LoanFacilityId facilityId, SanctionedLoanId sanctionId, Money amountToDisburse, Clock clock) {
        return TradeLoanFacilityIrregularlyDisbursed.of(facilityId, sanctionId, amountToDisburse, clock);
    }

    @Override
    public TradeLoanFacilityPartiallyDisbursed createPartiallyDisbursedEvent(
            LoanFacilityId loanFacilityId, SanctionedLoanId sanctionedLoanId, Money totalDisbursedAmount, Clock clock) {
        return TradeLoanFacilityPartiallyDisbursed.of(loanFacilityId, sanctionedLoanId, totalDisbursedAmount, clock);
    }

    @Override
    public TradeLoanFacilityAdditionalDisbursementCompleted createAdditionalDisbursementCompletedEvent(
            LoanFacilityId facilityId, SanctionedLoanId sanctionId, Money totalDisbursedAmount, Clock clock) {
        return TradeLoanFacilityAdditionalDisbursementCompleted.of(facilityId, sanctionId, totalDisbursedAmount, clock);
    }
}
