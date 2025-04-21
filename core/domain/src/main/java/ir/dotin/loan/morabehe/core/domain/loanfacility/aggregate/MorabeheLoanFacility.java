package ir.dotin.loan.morabehe.core.domain.loanfacility.aggregate;

import java.time.Clock;
import java.util.Objects;

import ir.dotin.platform.domain.common.Notification;
import ir.dotin.platform.domain.common.Result;
import ir.dotin.platform.domain.common.event.DomainEvent;
import ir.dotin.loan.baseloan.core.domain.loanfacility.aggregate.AbstractLoanFacility;
import ir.dotin.loan.baseloan.core.domain.loanfacility.aggregate.AbstractSanctionedLoan;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.FacilityStatus;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.CollateralSerial;
import ir.dotin.loan.baseloan.core.domain.shared.vo.FailureReason;
import ir.dotin.loan.baseloan.core.domain.shared.vo.SanctionSerial;
import ir.dotin.loan.baseloan.core.domain.shared.vo.customer.Party;
import ir.dotin.loan.morabehe.core.domain.loanarrangement.vo.MorabeheLoanArrangementId;
import ir.dotin.loan.morabehe.core.domain.loanfacility.event.*;
import ir.dotin.loan.morabehe.core.domain.loanfacility.i18n.MorabeheLoanFacilityLocalizedMessageCodes;
import ir.dotin.loan.morabehe.core.domain.loanfacility.vo.MorabeheLoanApplicationId;
import ir.dotin.loan.morabehe.core.domain.loanfacility.vo.MorabeheLoanFacilityId;
import ir.dotin.loan.morabehe.core.domain.loanfacility.vo.MorabeheSanctionedLoanId;
import ir.dotin.loan.morabehe.core.domain.loantype.vo.MorabeheLoanTypeId;

public final class MorabeheLoanFacility
        extends AbstractLoanFacility<
                MorabeheLoanFacilityId,
                MorabeheLoanApplicationId,
                MorabeheSanctionedLoanId,
                MorabeheLoanApplication,
                MorabeheSanctionedLoan> {

    private final MorabeheLoanTypeId morabeheLoanTypeId;
    private final MorabeheLoanArrangementId morabeheLoanArrangementId;

    private MorabeheLoanFacility(
            MorabeheLoanFacilityId id,
            MorabeheLoanApplication initialApplication,
            MorabeheLoanTypeId loanTypeId,
            MorabeheLoanArrangementId loanRuleId,
            Clock clock) {
        super(id, initialApplication, clock);
        this.morabeheLoanTypeId = Objects.requireNonNull(loanTypeId, "morabeheLoanTypeId cannot be null");
        this.morabeheLoanArrangementId = Objects.requireNonNull(loanRuleId, "morabeheLoanRuleId cannot be null");
    }

    private MorabeheLoanFacility(
            MorabeheLoanFacilityId id,
            MorabeheLoanApplication application,
            MorabeheSanctionedLoan sanctionedLoan,
            FacilityStatus status,
            MorabeheLoanTypeId loanTypeId,
            MorabeheLoanArrangementId loanRuleId) {
        super(id, application, sanctionedLoan, status);
        this.morabeheLoanTypeId = Objects.requireNonNull(loanTypeId, "morabeheLoanTypeId cannot be null");
        this.morabeheLoanArrangementId = Objects.requireNonNull(loanRuleId, "morabeheLoanRuleId cannot be null");
    }

    public static Result<MorabeheLoanFacility> create(
            MorabeheLoanApplication.Builder applicationBuilder,
            MorabeheLoanTypeId loanTypeId,
            MorabeheLoanArrangementId loanRuleId,
            Clock clock) {

        Objects.requireNonNull(applicationBuilder, "applicationBuilder cannot be null");
        Objects.requireNonNull(loanTypeId, "loanTypeId cannot be null");
        Objects.requireNonNull(loanRuleId, "loanRuleId cannot be null");
        Objects.requireNonNull(clock, "clock cannot be null");

        try {
            MorabeheLoanApplicationId appId = MorabeheLoanApplicationId.generate();
            MorabeheLoanFacilityId facilityId = MorabeheLoanFacilityId.generate();

            MorabeheLoanApplication application = applicationBuilder.id(appId).build();

            MorabeheLoanFacility facility =
                    new MorabeheLoanFacility(facilityId, application, loanTypeId, loanRuleId, clock);

            return Result.ofValue(facility);
        } catch (IllegalArgumentException e) {
            return Result.ofNotification(Notification.ofError(
                    MorabeheLoanFacilityLocalizedMessageCodes.BUILDER_VALIDATION_FAILED, e.getMessage()));
        }
    }

    public static MorabeheLoanFacility reconstitute(
            MorabeheLoanFacilityId id,
            MorabeheLoanApplication application,
            MorabeheSanctionedLoan sanctionedLoan,
            FacilityStatus status,
            MorabeheLoanTypeId loanTypeId,
            MorabeheLoanArrangementId loanRuleId) {
        return new MorabeheLoanFacility(id, application, sanctionedLoan, status, loanTypeId, loanRuleId);
    }

    @Override
    public MorabeheLoanTypeId loanTypeId() {
        return morabeheLoanTypeId;
    }

    @Override
    public MorabeheLoanArrangementId getLoanArrangementId() {
        return morabeheLoanArrangementId;
    }

    @Override
    public String getLoanFacilityType() {
        return "MORABEHE";
    }

    @Override
    protected MorabeheSanctionedLoanId generateSanctionedLoanId() {
        return MorabeheSanctionedLoanId.generate();
    }

    @Override
    protected AbstractSanctionedLoan.AbstractBuilder<MorabeheSanctionedLoanId, MorabeheSanctionedLoan, ?>
            getSanctionedLoanBuilder() {
        return MorabeheSanctionedLoan.newBuilder();
    }

    @Override
    protected DomainEvent<?, ?> getFacilityCreatedEvent(
            MorabeheLoanFacilityId id, MorabeheLoanApplicationId appId, Party customer, Clock clock) {
        return MorabeheLoanFacilityCreatedEvent.of(id, appId, customer, clock);
    }

    @Override
    protected DomainEvent<?, ?> getFacilityPendingApprovalEvent(
            MorabeheLoanFacilityId id, MorabeheLoanApplicationId appId, Clock clock) {
        return MorabeheLoanFacilityPendingApprovalEvent.of(id, appId, clock);
    }

    @Override
    protected DomainEvent<?, ?> getFacilityApprovedEvent(
            MorabeheLoanFacilityId id,
            MorabeheSanctionedLoanId sanctionId,
            SanctionSerial sanctionSerial,
            Clock clock) {
        return MorabeheLoanFacilityApprovedEvent.of(id, sanctionId, sanctionSerial, clock);
    }

    @Override
    protected DomainEvent<?, ?> getFacilityRejectedEvent(
            MorabeheLoanFacilityId id, MorabeheLoanApplicationId appId, Clock clock) {
        return MorabeheLoanFacilityRejectedEvent.of(id, appId, clock);
    }

    @Override
    protected DomainEvent<?, ?> getFacilityContractIssuedEvent(
            MorabeheLoanFacilityId id, MorabeheSanctionedLoanId sanctionId, Clock clock) {
        return MorabeheLoanFacilityContractIssuedEvent.of(id, sanctionId, clock);
    }

    @Override
    protected DomainEvent<?, ?> getFacilityPendingDisbursementEvent(
            MorabeheLoanFacilityId id, MorabeheSanctionedLoanId sanctionId, Clock clock) {
        return MorabeheLoanFacilityPendingDisbursementEvent.of(id, sanctionId, clock);
    }

    @Override
    protected DomainEvent<?, ?> getFacilityDisbursementFailedEvent(
            MorabeheLoanFacilityId id, MorabeheSanctionedLoanId sanctionId, FailureReason reason, Clock clock) {
        return MorabeheLoanFacilityDisbursementFailedEvent.of(id, sanctionId, reason, clock);
    }

    @Override
    protected DomainEvent<?, ?> getFacilityActivatedEvent(
            MorabeheLoanFacilityId id, MorabeheSanctionedLoanId sanctionId, Clock clock) {
        return MorabeheLoanFacilityActivatedEvent.of(id, sanctionId, clock);
    }

    @Override
    protected DomainEvent<?, ?> getFacilityClosedPaidOffEvent(
            MorabeheLoanFacilityId id, MorabeheSanctionedLoanId sanctionId, Clock clock) {
        return MorabeheLoanFacilityClosedPaidOffEvent.of(id, sanctionId, clock);
    }

    @Override
    protected DomainEvent<?, ?> getFacilityClosedDefaultedEvent(
            MorabeheLoanFacilityId id, MorabeheSanctionedLoanId sanctionId, Clock clock) {
        return MorabeheLoanFacilityClosedDefaultedEvent.of(id, sanctionId, clock);
    }

    @Override
    protected DomainEvent<?, ?> getFacilityCancelledEvent(MorabeheLoanFacilityId id, Clock clock) {
        return MorabeheLoanFacilityCancelledEvent.of(id, clock);
    }

    @Override
    protected DomainEvent<?, ?> getFacilityCollateralAddedEvent(
            MorabeheLoanFacilityId id,
            MorabeheSanctionedLoanId sanctionId,
            CollateralSerial collateralSerial,
            Clock clock) {
        return MorabeheLoanFacilityCollateralAddedEvent.of(id, sanctionId, collateralSerial, clock);
    }
}
