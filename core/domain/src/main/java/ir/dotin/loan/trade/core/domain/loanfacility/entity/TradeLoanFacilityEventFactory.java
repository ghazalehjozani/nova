package ir.dotin.loan.trade.core.domain.loanfacility.entity;

import java.time.Clock;
import java.util.List;

import org.jspecify.annotations.Nullable;

import ir.dotin.platform.pangaea.commons.domain.vo.Money;
import ir.dotin.loan.baseloan.core.domain.loanfacility.entity.LoanFacilityEventFactory;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.SanctionType;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.ApplicationNumber;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.CollateralSerial;
import ir.dotin.loan.baseloan.core.domain.shared.enums.InstallmentPaymentType;
import ir.dotin.loan.baseloan.core.domain.shared.vo.FailureReason;
import ir.dotin.loan.baseloan.core.domain.shared.vo.InstallmentScheduleId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanApplicationId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.SanctionedLoanId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.TrackedTransactionNumber;
import ir.dotin.loan.baseloan.core.domain.shared.vo.customer.GuarantorParty;
import ir.dotin.loan.trade.core.domain.loanfacility.event.*;

final class TradeLoanFacilityEventFactory implements LoanFacilityEventFactory<TradeLoanFacilityEvents<?>> {

    @Override
    public TradeLoanFacilityApprovalSubmitted createPendingApprovalEvent(
            LoanFacilityId facilityId, LoanApplicationId applicationId, Clock clock) {
        return TradeLoanFacilityApprovalSubmitted.of(facilityId, applicationId, clock);
    }

    @Override
    public TradeLoanFacilityApproved createApprovedEvent(
            LoanFacilityId facilityId,
            SanctionedLoanId sanctionId,
            String sanctionSerial,
            SanctionType sanctionType,
            Clock clock) {
        return TradeLoanFacilityApproved.of(facilityId, sanctionId, sanctionSerial, sanctionType, clock);
    }

    @Override
    public TradeLoanFacilityRejected createRejectedEvent(
            LoanFacilityId facilityId, LoanApplicationId applicationId, Clock clock) {
        return TradeLoanFacilityRejected.of(facilityId, applicationId, clock);
    }

    @Override
    public TradeLoanFacilityContractIssued createContractIssuedEvent(
            LoanFacilityId facilityId, SanctionedLoanId sanctionId, String transactionNumber, Clock clock) {
        return TradeLoanFacilityContractIssued.of(facilityId, sanctionId, transactionNumber, clock);
    }

    @Override
    public TradeLoanFacilityContractIssuanceReverted createContractIssuanceRevertedEvent(
            LoanFacilityId facilityId, Clock clock) {
        return TradeLoanFacilityContractIssuanceReverted.of(facilityId, clock);
    }

    @Override
    public TradeLoanFacilityDisbursementFailed createDisbursementFailedEvent(
            LoanFacilityId facilityId, SanctionedLoanId sanctionId, FailureReason reason, Clock clock) {
        return TradeLoanFacilityDisbursementFailed.of(facilityId, sanctionId, reason, clock);
    }

    @Override
    public TradeLoanFacilityLumpSumDisbursed createLumpSumDisbursedEvent(
            LoanFacilityId facilityId,
            SanctionedLoanId sanctionId,
            InstallmentPaymentType installmentPaymentType,
            ApplicationNumber applicationNumber,
            List<TrackedTransactionNumber> trackedTransactionNumbers,
            @Nullable InstallmentScheduleId installmentScheduleId,
            Clock clock) {
        return TradeLoanFacilityLumpSumDisbursed.of(
                facilityId,
                sanctionId,
                installmentPaymentType,
                applicationNumber,
                trackedTransactionNumbers,
                installmentScheduleId,
                clock);
    }

    @Override
    public TradeLoanFacilityFullyDisbursed createFullyDisbursedEvent(
            LoanFacilityId facilityId,
            SanctionedLoanId sanctionedLoanId,
            String totalDisbursed,
            int totalTranches,
            List<TrackedTransactionNumber> trackedNumbers,
            Clock clock) {
        return TradeLoanFacilityFullyDisbursed.of(
                facilityId, sanctionedLoanId, totalDisbursed, totalTranches, trackedNumbers, clock);
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
            LoanFacilityId facilityId,
            SanctionedLoanId sanctionId,
            List<CollateralSerial> collateralSerials,
            Clock clock) {
        return TradeLoanFacilityCollateralAdded.of(facilityId, sanctionId, collateralSerials, clock);
    }

    @Override
    public TradeLoanFacilityCollateralUpdated createCollateralUpdatedEvent(
            LoanFacilityId facilityId,
            SanctionedLoanId sanctionId,
            List<CollateralSerial> collateralSerials,
            Clock clock) {
        return TradeLoanFacilityCollateralUpdated.of(facilityId, sanctionId, collateralSerials, clock);
    }

    @Override
    public TradeLoanFacilityCollateralDeleted createCollateralDeletedEvent(
            LoanFacilityId facilityId,
            SanctionedLoanId sanctionId,
            List<CollateralSerial> deletedSerials,
            Clock clock) {
        return TradeLoanFacilityCollateralDeleted.of(facilityId, sanctionId, deletedSerials, clock);
    }

    @Override
    public TradeLoanFacilityCreated createCreatedEvent(
            LoanFacilityId facilityId, ApplicationNumber applicationNumber, Clock clock) {
        return TradeLoanFacilityCreated.of(facilityId, applicationNumber, clock);
    }

    @Override
    public TradeLoanFacilityIrregularTrancheDisbursed createIrregularTrancheDisbursedEvent(
            LoanFacilityId facilityId,
            SanctionedLoanId sanctionedLoanId,
            List<TrackedTransactionNumber> trackedNumbers,
            Money trancheAmount,
            String totalDisbursed,
            String remainingCapacity,
            int trancheNumber,
            InstallmentScheduleId scheduleId,
            Clock clock) {
        return TradeLoanFacilityIrregularTrancheDisbursed.of(
                facilityId,
                sanctionedLoanId,
                trackedNumbers,
                trancheAmount,
                totalDisbursed,
                remainingCapacity,
                trancheNumber,
                scheduleId,
                clock);
    }

    @Override
    public TradeLoanFacilityApprovalSubmissionReverted createApprovalSubmissionRevertedEvent(
            LoanFacilityId facilityId, Clock clock) {
        return TradeLoanFacilityApprovalSubmissionReverted.of(facilityId, clock);
    }

    @Override
    public TradeLoanFacilityApprovalReverted createApprovalRevertedEvent(LoanFacilityId facilityId, Clock clock) {
        return TradeLoanFacilityApprovalReverted.of(facilityId, clock);
    }

    @Override
    public TradeLoanFacilityDisbursementReverted createDisbursementRevertedEvent(
            LoanFacilityId facilityId, Clock clock) {
        return TradeLoanFacilityDisbursementReverted.of(facilityId, clock);
    }

    @Override
    public TradeLoanFacilityIrregularTrancheDisbursementReverted createIrregularTrancheDisbursementRevertedEvent(
            LoanFacilityId facilityId, Clock clock) {
        return TradeLoanFacilityIrregularTrancheDisbursementReverted.of(facilityId, clock);
    }

    @Override
    public TradeLoanFacilityOriginationReverted createOriginationRevertedEvent(
            LoanFacilityId facilityId, String reason, Clock clock) {
        return TradeLoanFacilityOriginationReverted.of(facilityId, reason, clock);
    }

    @Override
    public TradeLoanFacilityAddCollateralReverted createAddCollateralRevertedEvent(
            LoanFacilityId facilityId, Clock clock) {
        return TradeLoanFacilityAddCollateralReverted.of(facilityId, clock);
    }

    @Override
    public TradeLoanFacilityClosePaidOffReverted createClosedPaidOffRevertedEvent(
            LoanFacilityId facilityId, Clock clock) {
        return TradeLoanFacilityClosePaidOffReverted.of(facilityId, clock);
    }

    @Override
    public TradeLoanFacilityRestructuring createRestructuringEvent(
            LoanFacilityId facilityId,
            String restructuringTransaction,
            InstallmentScheduleId scheduleId,
            Integer newDuration,
            Clock clock) {
        return TradeLoanFacilityRestructuring.of(facilityId, restructuringTransaction, scheduleId, newDuration, clock);
    }

    @Override
    public TradeLoanFacilityGuarantorAdded createGuarantorAddedEvent(
            LoanFacilityId facilityId,
            List<GuarantorParty> added,
            List<GuarantorParty> resultingSnapshot,
            Clock clock) {
        return TradeLoanFacilityGuarantorAdded.of(facilityId, added, resultingSnapshot, clock);
    }

    @Override
    public TradeLoanFacilityGuarantorRemoved createGuarantorRemovedEvent(
            LoanFacilityId facilityId,
            String removedCustomerNumber,
            List<GuarantorParty> resultingSnapshot,
            Clock clock) {
        return TradeLoanFacilityGuarantorRemoved.of(facilityId, removedCustomerNumber, resultingSnapshot, clock);
    }
}
