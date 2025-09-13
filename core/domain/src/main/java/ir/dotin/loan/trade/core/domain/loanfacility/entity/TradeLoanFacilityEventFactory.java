package ir.dotin.loan.trade.core.domain.loanfacility.entity;

import java.time.Clock;
import java.util.List;

import ir.dotin.platform.commons.domain.vo.Money;
import ir.dotin.loan.baseloan.core.domain.loanfacility.entity.LoanFacilityEventFactory;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.CollateralSerial;
import ir.dotin.loan.baseloan.core.domain.shared.vo.FailureReason;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanApplicationId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.SanctionSerial;
import ir.dotin.loan.baseloan.core.domain.shared.vo.SanctionedLoanId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.TransactionNumber;
import ir.dotin.loan.baseloan.core.domain.shared.vo.customer.Party;
import ir.dotin.loan.trade.core.domain.loanfacility.event.*;

import static java.util.UUID.randomUUID;

final class TradeLoanFacilityEventFactory implements LoanFacilityEventFactory<TradeLoanFacilityEvent<?, ?>> {

    @Override
    public SanctionedLoanId generateSanctionedLoanId() {
        return new SanctionedLoanId(randomUUID());
    }

    @Override
    public TradeLoanFacilityPendingApprovalEvent createPendingApprovalEvent(
            LoanFacilityId facilityId, LoanApplicationId applicationId, Clock clock) {
        return TradeLoanFacilityPendingApprovalEvent.of(facilityId, applicationId, clock);
    }

    @Override
    public TradeLoanFacilityApprovedEvent createApprovedEvent(
            LoanFacilityId facilityId, SanctionedLoanId sanctionId, SanctionSerial sanctionSerial, Clock clock) {
        return TradeLoanFacilityApprovedEvent.of(facilityId, sanctionId, sanctionSerial, clock);
    }

    @Override
    public TradeLoanFacilityRejectedEvent createRejectedEvent(
            LoanFacilityId facilityId, LoanApplicationId applicationId, Clock clock) {
        return TradeLoanFacilityRejectedEvent.of(facilityId, applicationId, clock);
    }

    @Override
    public TradeLoanFacilityContractIssuedEvent createContractIssuedEvent(
            LoanFacilityId facilityId,
            SanctionedLoanId sanctionId,
            List<TransactionNumber> transactionNumbers,
            Clock clock) {
        return TradeLoanFacilityContractIssuedEvent.of(facilityId, sanctionId, transactionNumbers, clock);
    }

    @Override
    public TradeLoanFacilityPendingDisbursementEvent createPendingDisbursementEvent(
            LoanFacilityId facilityId, SanctionedLoanId sanctionId, Clock clock) {
        return TradeLoanFacilityPendingDisbursementEvent.of(facilityId, sanctionId, clock);
    }

    @Override
    public TradeLoanFacilityDisbursementFailedEvent createDisbursementFailedEvent(
            LoanFacilityId facilityId, SanctionedLoanId sanctionId, FailureReason reason, Clock clock) {
        return TradeLoanFacilityDisbursementFailedEvent.of(facilityId, sanctionId, reason, clock);
    }

    @Override
    public TradeLoanFacilityActivatedEvent createActivatedEvent(
            LoanFacilityId facilityId, SanctionedLoanId sanctionId, Clock clock) {
        return TradeLoanFacilityActivatedEvent.of(facilityId, sanctionId, clock);
    }

    @Override
    public TradeLoanFacilityClosedPaidOffEvent createClosedPaidOffEvent(
            LoanFacilityId facilityId, SanctionedLoanId sanctionId, Clock clock) {
        return TradeLoanFacilityClosedPaidOffEvent.of(facilityId, sanctionId, clock);
    }

    @Override
    public TradeLoanFacilityClosedDefaultedEvent createClosedDefaultedEvent(
            LoanFacilityId facilityId, SanctionedLoanId sanctionId, Clock clock) {
        return TradeLoanFacilityClosedDefaultedEvent.of(facilityId, sanctionId, clock);
    }

    @Override
    public TradeLoanFacilityCancelledEvent createCancelledEvent(LoanFacilityId facilityId, Clock clock) {
        return TradeLoanFacilityCancelledEvent.of(facilityId, clock);
    }

    @Override
    public TradeLoanFacilityCollateralAddedEvent createCollateralAddedEvent(
            LoanFacilityId facilityId, SanctionedLoanId sanctionId, CollateralSerial collateralSerial, Clock clock) {
        return TradeLoanFacilityCollateralAddedEvent.of(facilityId, sanctionId, collateralSerial, clock);
    }

    @Override
    public TradeLoanFacilityCreatedEvent createCreatedEvent(
            LoanFacilityId facilityId, LoanApplicationId applicationId, Party customer, Clock clock) {
        return TradeLoanFacilityCreatedEvent.of(facilityId, applicationId, customer, clock);
    }

    @Override
    public TradeLoanFacilityIrregularDisbursementEvent createIrregularDisbursementRequestedEvent(
            LoanFacilityId facilityId, SanctionedLoanId sanctionId, Money amountToDisburse, Clock clock) {
        return TradeLoanFacilityIrregularDisbursementEvent.of(facilityId, sanctionId, amountToDisburse, clock);
    }
}
