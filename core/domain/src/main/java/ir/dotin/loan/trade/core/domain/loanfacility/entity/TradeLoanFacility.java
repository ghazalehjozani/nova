package ir.dotin.loan.trade.core.domain.loanfacility.entity;

import java.time.Clock;

import org.jspecify.annotations.Nullable;

import ir.dotin.platform.domain.common.Result;
import ir.dotin.platform.domain.common.event.DomainEvent;
import ir.dotin.loan.baseloan.core.domain.loanfacility.entity.AbstractLoanFacility;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.FacilityStatus;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.CollateralSerial;
import ir.dotin.loan.baseloan.core.domain.shared.vo.FailureReason;
import ir.dotin.loan.baseloan.core.domain.shared.vo.SanctionSerial;
import ir.dotin.loan.baseloan.core.domain.shared.vo.customer.Party;
import ir.dotin.loan.trade.core.domain.loanarrangement.vo.TradeLoanArrangementId;
import ir.dotin.loan.trade.core.domain.loanfacility.event.*;
import ir.dotin.loan.trade.core.domain.loanfacility.vo.TradeLoanApplicationId;
import ir.dotin.loan.trade.core.domain.loanfacility.vo.TradeLoanFacilityId;
import ir.dotin.loan.trade.core.domain.loanfacility.vo.TradeSanctionedLoanId;
import ir.dotin.loan.trade.core.domain.loantype.vo.TradeLoanTypeId;

import static java.util.Objects.requireNonNull;

public final class TradeLoanFacility
        extends AbstractLoanFacility<
                TradeLoanFacilityId,
                TradeLoanApplicationId,
                TradeSanctionedLoanId,
                TradeLoanApplication,
                TradeSanctionedLoan> {

    private final TradeLoanTypeId morabeheLoanTypeId;
    private final TradeLoanArrangementId morabeheLoanArrangementId;

    private TradeLoanFacility(
            TradeLoanFacilityId id,
            TradeLoanApplication application,
            @Nullable TradeSanctionedLoan sanctionedLoan,
            FacilityStatus status,
            TradeLoanTypeId loanTypeId,
            TradeLoanArrangementId loanArrangementId) {
        super(id, application, sanctionedLoan, status);
        this.morabeheLoanTypeId = requireNonNull(loanTypeId, "morabeheLoanTypeId cannot be null");
        this.morabeheLoanArrangementId = requireNonNull(loanArrangementId, "morabeheLoanArrangementId cannot be null");
    }

    public static Result<TradeLoanFacility> create(
            TradeLoanApplication.Builder applicationBuilder,
            TradeLoanTypeId loanTypeId,
            TradeLoanArrangementId loanArrangementId,
            Clock clock) {
        requireNonNull(applicationBuilder, "applicationBuilder cannot be null");
        requireNonNull(loanTypeId, "loanTypeId cannot be null");
        requireNonNull(loanArrangementId, "loanArrangementId cannot be null");
        requireNonNull(clock, "clock cannot be null");

        TradeLoanFacilityId facilityId = TradeLoanFacilityId.generate();
        Result<TradeLoanApplication> applicationResult = TradeLoanApplication.create(applicationBuilder);

        if (applicationResult.isFailure()) {
            return Result.failure(applicationResult.notification());
        }
        FacilityStatus facilityStatus = FacilityStatus.APPLICATION_SUBMITTED;
        @SuppressWarnings("nullness")
        TradeLoanApplication application = applicationResult.value();
        @SuppressWarnings("nullness")
        TradeSanctionedLoan sanctionedLoan = null;
        TradeLoanFacility facility = new TradeLoanFacility(
                facilityId, application, sanctionedLoan, facilityStatus, loanTypeId, loanArrangementId);

        facility.registerEvent(TradeLoanFacilityCreatedEvent.of(
                facilityId, application.getId(), facility.getLoanApplication().getCustomer(), clock));

        return Result.success(facility);
    }

    public static TradeLoanFacility reconstitute(
            TradeLoanFacilityId id,
            TradeLoanApplication application,
            TradeSanctionedLoan sanctionedLoan,
            FacilityStatus status,
            TradeLoanTypeId loanTypeId,
            TradeLoanArrangementId loanRuleId) {
        return new TradeLoanFacility(id, application, sanctionedLoan, status, loanTypeId, loanRuleId);
    }

    @Override
    public TradeLoanTypeId loanTypeId() {
        return morabeheLoanTypeId;
    }

    @Override
    public TradeLoanArrangementId getLoanArrangementId() {
        return morabeheLoanArrangementId;
    }

    @Override
    public String getLoanFacilityType() {
        return "MORABEHE";
    }

    @Override
    protected TradeSanctionedLoanId generateSanctionedLoanId() {
        return TradeSanctionedLoanId.generate();
    }

    @Override
    protected DomainEvent<?, ?> getFacilityCreatedEvent(
            TradeLoanFacilityId id, TradeLoanApplicationId appId, Party customer, Clock clock) {
        return TradeLoanFacilityCreatedEvent.of(id, appId, customer, clock);
    }

    @Override
    protected DomainEvent<?, ?> getFacilityPendingApprovalEvent(
            TradeLoanFacilityId id, TradeLoanApplicationId appId, Clock clock) {
        return TradeLoanFacilityPendingApprovalEvent.of(id, appId, clock);
    }

    @Override
    protected DomainEvent<?, ?> getFacilityApprovedEvent(
            TradeLoanFacilityId id, TradeSanctionedLoanId sanctionId, SanctionSerial sanctionSerial, Clock clock) {
        return TradeLoanFacilityApprovedEvent.of(id, sanctionId, sanctionSerial, clock);
    }

    @Override
    protected DomainEvent<?, ?> getFacilityRejectedEvent(
            TradeLoanFacilityId id, TradeLoanApplicationId appId, Clock clock) {
        return TradeLoanFacilityRejectedEvent.of(id, appId, clock);
    }

    @Override
    protected DomainEvent<?, ?> getFacilityContractIssuedEvent(
            TradeLoanFacilityId id, TradeSanctionedLoanId sanctionId, Clock clock) {
        return TradeLoanFacilityContractIssuedEvent.of(id, sanctionId, clock);
    }

    @Override
    protected DomainEvent<?, ?> getFacilityPendingDisbursementEvent(
            TradeLoanFacilityId id, TradeSanctionedLoanId sanctionId, Clock clock) {
        return TradeLoanFacilityPendingDisbursementEvent.of(id, sanctionId, clock);
    }

    @Override
    protected DomainEvent<?, ?> getFacilityDisbursementFailedEvent(
            TradeLoanFacilityId id, TradeSanctionedLoanId sanctionId, FailureReason reason, Clock clock) {
        return TradeLoanFacilityDisbursementFailedEvent.of(id, sanctionId, reason, clock);
    }

    @Override
    protected DomainEvent<?, ?> getFacilityActivatedEvent(
            TradeLoanFacilityId id, TradeSanctionedLoanId sanctionId, Clock clock) {
        return TradeLoanFacilityActivatedEvent.of(id, sanctionId, clock);
    }

    @Override
    protected DomainEvent<?, ?> getFacilityClosedPaidOffEvent(
            TradeLoanFacilityId id, TradeSanctionedLoanId sanctionId, Clock clock) {
        return TradeLoanFacilityClosedPaidOffEvent.of(id, sanctionId, clock);
    }

    @Override
    protected DomainEvent<?, ?> getFacilityClosedDefaultedEvent(
            TradeLoanFacilityId id, TradeSanctionedLoanId sanctionId, Clock clock) {
        return TradeLoanFacilityClosedDefaultedEvent.of(id, sanctionId, clock);
    }

    @Override
    protected DomainEvent<?, ?> getFacilityCancelledEvent(TradeLoanFacilityId id, Clock clock) {
        return TradeLoanFacilityCancelledEvent.of(id, clock);
    }

    @Override
    protected DomainEvent<?, ?> getFacilityCollateralAddedEvent(
            TradeLoanFacilityId id, TradeSanctionedLoanId sanctionId, CollateralSerial collateralSerial, Clock clock) {
        return TradeLoanFacilityCollateralAddedEvent.of(id, sanctionId, collateralSerial, clock);
    }
}
