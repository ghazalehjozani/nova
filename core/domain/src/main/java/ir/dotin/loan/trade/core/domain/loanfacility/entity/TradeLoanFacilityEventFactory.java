package ir.dotin.loan.trade.core.domain.loanfacility.entity;

import java.time.Clock;
import java.util.List;

import ir.dotin.platform.domain.common.entity.Identity;
import ir.dotin.platform.domain.common.vo.Money;
import ir.dotin.loan.baseloan.core.domain.loanfacility.entity.LoanFacilityEventFactory;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.CollateralSerial;
import ir.dotin.loan.baseloan.core.domain.shared.vo.FailureReason;
import ir.dotin.loan.baseloan.core.domain.shared.vo.SanctionSerial;
import ir.dotin.loan.baseloan.core.domain.shared.vo.TransactionNumber;
import ir.dotin.loan.baseloan.core.domain.shared.vo.customer.Party;
import ir.dotin.loan.trade.core.domain.loanfacility.event.*;
import ir.dotin.loan.trade.core.domain.loanfacility.vo.TradeLoanApplicationId;
import ir.dotin.loan.trade.core.domain.loanfacility.vo.TradeLoanFacilityId;
import ir.dotin.loan.trade.core.domain.loanfacility.vo.TradeSanctionedLoanId;

final class TradeLoanFacilityEventFactory implements LoanFacilityEventFactory<TradeLoanFacilityEvent<?, ?>> {

    @Override
    public Identity generateSanctionedLoanId() {
        return TradeSanctionedLoanId.generate();
    }

    @Override
    public TradeLoanFacilityPendingApprovalEvent createPendingApprovalEvent(
            Identity facilityId, Identity applicationId, Clock clock) {
        return TradeLoanFacilityPendingApprovalEvent.of(
                (TradeLoanFacilityId) facilityId, (TradeLoanApplicationId) applicationId, clock);
    }

    @Override
    public TradeLoanFacilityApprovedEvent createApprovedEvent(
            Identity facilityId, Identity sanctionId, SanctionSerial sanctionSerial, Clock clock) {
        return TradeLoanFacilityApprovedEvent.of(
                (TradeLoanFacilityId) facilityId, (TradeSanctionedLoanId) sanctionId, sanctionSerial, clock);
    }

    @Override
    public TradeLoanFacilityRejectedEvent createRejectedEvent(
            Identity facilityId, Identity applicationId, Clock clock) {
        return TradeLoanFacilityRejectedEvent.of(
                (TradeLoanFacilityId) facilityId, (TradeLoanApplicationId) applicationId, clock);
    }

    @Override
    public TradeLoanFacilityContractIssuedEvent createContractIssuedEvent(
            Identity facilityId, Identity sanctionId, List<TransactionNumber> transactionNumbers, Clock clock) {
        return TradeLoanFacilityContractIssuedEvent.of(
                (TradeLoanFacilityId) facilityId, (TradeSanctionedLoanId) sanctionId, transactionNumbers, clock);
    }

    @Override
    public TradeLoanFacilityPendingDisbursementEvent createPendingDisbursementEvent(
            Identity facilityId, Identity sanctionId, Clock clock) {
        return TradeLoanFacilityPendingDisbursementEvent.of(
                (TradeLoanFacilityId) facilityId, (TradeSanctionedLoanId) sanctionId, clock);
    }

    @Override
    public TradeLoanFacilityDisbursementFailedEvent createDisbursementFailedEvent(
            Identity facilityId, Identity sanctionId, FailureReason reason, Clock clock) {
        return TradeLoanFacilityDisbursementFailedEvent.of(
                (TradeLoanFacilityId) facilityId, (TradeSanctionedLoanId) sanctionId, reason, clock);
    }

    @Override
    public TradeLoanFacilityActivatedEvent createActivatedEvent(Identity facilityId, Identity sanctionId, Clock clock) {
        return TradeLoanFacilityActivatedEvent.of(
                (TradeLoanFacilityId) facilityId, (TradeSanctionedLoanId) sanctionId, clock);
    }

    @Override
    public TradeLoanFacilityClosedPaidOffEvent createClosedPaidOffEvent(
            Identity facilityId, Identity sanctionId, Clock clock) {
        return TradeLoanFacilityClosedPaidOffEvent.of(
                (TradeLoanFacilityId) facilityId, (TradeSanctionedLoanId) sanctionId, clock);
    }

    @Override
    public TradeLoanFacilityClosedDefaultedEvent createClosedDefaultedEvent(
            Identity facilityId, Identity sanctionId, Clock clock) {
        return TradeLoanFacilityClosedDefaultedEvent.of(
                (TradeLoanFacilityId) facilityId, (TradeSanctionedLoanId) sanctionId, clock);
    }

    @Override
    public TradeLoanFacilityCancelledEvent createCancelledEvent(Identity facilityId, Clock clock) {
        return TradeLoanFacilityCancelledEvent.of((TradeLoanFacilityId) facilityId, clock);
    }

    @Override
    public TradeLoanFacilityCollateralAddedEvent createCollateralAddedEvent(
            Identity facilityId, Identity sanctionId, CollateralSerial collateralSerial, Clock clock) {
        return TradeLoanFacilityCollateralAddedEvent.of(
                (TradeLoanFacilityId) facilityId, (TradeSanctionedLoanId) sanctionId, collateralSerial, clock);
    }

    @Override
    public TradeLoanFacilityCreatedEvent createCreatedEvent(
            Identity facilityId, Identity applicationId, Party customer, Clock clock) {
        return TradeLoanFacilityCreatedEvent.of(
                (TradeLoanFacilityId) facilityId, (TradeLoanApplicationId) applicationId, customer, clock);
    }

    @Override
    public TradeLoanFacilityIrregularDisbursementEvent createIrregularDisbursementRequestedEvent(
            Identity facilityId, Identity sanctionId, Money amountToDisburse, Clock clock) {
        return TradeLoanFacilityIrregularDisbursementEvent.of(
                (TradeLoanFacilityId) facilityId, (TradeSanctionedLoanId) sanctionId, amountToDisburse, clock);
    }
}
