package ir.dotin.loan.morabehe.core.domain.loanarrangement.aggregate;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import ir.dotin.platform.domain.common.Result;
import ir.dotin.platform.domain.common.event.DomainEvent;
import ir.dotin.platform.domain.common.feature.FeatureConfig;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.aggregate.AbstractLoanArrangement;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.vo.ArrangementFeatureContext;
import ir.dotin.loan.baseloan.core.domain.shared.formula.BaseFormulaField;
import ir.dotin.loan.baseloan.core.domain.shared.vo.Active;
import ir.dotin.loan.baseloan.core.domain.shared.vo.Disable;
import ir.dotin.loan.morabehe.core.domain.loanarrangement.event.MorabeheLoanArrangementActivated;
import ir.dotin.loan.morabehe.core.domain.loanarrangement.event.MorabeheLoanArrangementCreated;
import ir.dotin.loan.morabehe.core.domain.loanarrangement.event.MorabeheLoanArrangementDeactivated;
import ir.dotin.loan.morabehe.core.domain.loanarrangement.event.NewMorabeheLoanArrangementVersionPrepared;
import ir.dotin.loan.morabehe.core.domain.loanarrangement.vo.MorabeheLoanArrangementId;

public final class MorabeheLoanArrangement
        extends AbstractLoanArrangement<MorabeheLoanArrangementId, BaseFormulaField> {

    private MorabeheLoanArrangement(Builder builder) {
        super(builder);
    }

    public static Builder newBuilder(FeatureConfig featureConfig) {
        return new Builder(featureConfig);
    }

    public static Result<MorabeheLoanArrangement> create(Builder builder, Clock clock) {

        Objects.requireNonNull(clock, "Clock cannot be null for creation");
        Objects.requireNonNull(builder, "Builder cannot be null for creation");

        builder.withId(MorabeheLoanArrangementId.generate());

        builder.withActive(new Active(true));
        builder.withDisable(new Disable(false));
        builder.withPreviousVersion(null);

        Result<MorabeheLoanArrangement> arrangementResult = builder.build();

        if (arrangementResult.isFailure()) {
            return Result.failure(arrangementResult.notification());
        }

        MorabeheLoanArrangement arrangement = arrangementResult.value();

        var payload = new MorabeheLoanArrangementCreated.Payload(
                arrangement.getCode(),
                arrangement.getTitle(),
                arrangement.getCurrencies(),
                arrangement.getAmountRange(),
                arrangement.getDurationRange(),
                arrangement.getPartyType(),
                arrangement.getDisburseType(),
                arrangement.getLifeInsurancePaymentType(),
                arrangement.getRuleDisburseType(),
                arrangement.getLoanSecondaryType(),
                arrangement.getSectionType(),
                arrangement.getInterestPolicy(),
                arrangement.getPenaltyPolicy(),
                arrangement.getInstallmentPolicy(),
                arrangement.getGracePeriodPolicy(),
                arrangement.getRepaymentPriorityPolicy(),
                arrangement.getRegulatoryCompliancePolicy(),
                arrangement.getCollateralPolicy(),
                arrangement.getGuarantorCount(),
                arrangement.isHasInstallmentCard(),
                arrangement.getConfirmType(),
                arrangement.getActive().isActive());

        MorabeheLoanArrangementCreated creationEvent =
                new MorabeheLoanArrangementCreated(UUID.randomUUID(), arrangement.getId(), payload, Instant.now(clock));

        arrangement.registerEvent(creationEvent);

        return Result.success(arrangement);
    }

    public static MorabeheLoanArrangement reconstitute(Builder builder) {
        Objects.requireNonNull(builder, "Builder cannot be null for reconstitution.");
        return builder.buildInternal();
    }

    @Override
    protected DomainEvent<?, ?> getArrangementActivatedEvent(MorabeheLoanArrangementId aggregateId, Clock clock) {
        var payload = new MorabeheLoanArrangementActivated.Payload();
        return new MorabeheLoanArrangementActivated(UUID.randomUUID(), aggregateId, payload, clock.instant());
    }

    @Override
    protected DomainEvent<?, ?> getArrangementDeactivatedEvent(MorabeheLoanArrangementId aggregateId, Clock clock) {
        var payload = new MorabeheLoanArrangementDeactivated.Payload();
        return new MorabeheLoanArrangementDeactivated(UUID.randomUUID(), aggregateId, payload, clock.instant());
    }

    @Override
    protected DomainEvent<?, ?> getNewArrangementVersionPreparedEvent(
            MorabeheLoanArrangementId currentAggregateId,
            AbstractBuilder<MorabeheLoanArrangementId, BaseFormulaField, ?, ?> validatedBuilder,
            Clock clock) {
        Builder morabeheBuilder = (Builder) validatedBuilder;
        Result<MorabeheLoanArrangement> loanArrangementResult = morabeheBuilder.build();
        MorabeheLoanArrangement loanArrangement = loanArrangementResult.value();
        MorabeheLoanArrangementId newVersionId = loanArrangement.getId();

        var payload = new NewMorabeheLoanArrangementVersionPrepared.Payload(
                newVersionId,
                currentAggregateId,
                loanArrangement.getCode(),
                loanArrangement.getTitle(),
                loanArrangement.getCurrencies(),
                loanArrangement.getAmountRange(),
                loanArrangement.getDurationRange(),
                loanArrangement.getPartyType(),
                loanArrangement.getDisburseType(),
                loanArrangement.getLifeInsurancePaymentType(),
                loanArrangement.getRuleDisburseType(),
                loanArrangement.getLoanSecondaryType(),
                loanArrangement.getSectionType(),
                loanArrangement.getInterestPolicy(),
                loanArrangement.getPenaltyPolicy(),
                loanArrangement.getInstallmentPolicy(),
                loanArrangement.getGracePeriodPolicy(),
                loanArrangement.getRepaymentPriorityPolicy(),
                loanArrangement.getRegulatoryCompliancePolicy(),
                loanArrangement.getCollateralPolicy(),
                loanArrangement.getGuarantorCount(),
                loanArrangement.isHasInstallmentCard(),
                loanArrangement.getConfirmType());

        return new NewMorabeheLoanArrangementVersionPrepared(
                UUID.randomUUID(), newVersionId, payload, Instant.now(clock));
    }

    @Override
    public Result<MorabeheLoanArrangement> activate(Clock clock) {
        Objects.requireNonNull(clock, "Clock cannot be null for activation");

        return super.activate(clock).cast(MorabeheLoanArrangement.class);
    }

    @Override
    public Result<MorabeheLoanArrangement> deactivate(Clock clock) {
        Objects.requireNonNull(clock, "Clock cannot be null for deactivation");
        return super.deactivate(clock).cast(MorabeheLoanArrangement.class);
    }

    public Result<Builder> prepareNewVersion(
            Builder updatedBuilder, ArrangementFeatureContext featureContext, Clock clock) {

        Result<Builder> prepareResult = super.prepareNewVersion(updatedBuilder, featureContext, clock);

        if (prepareResult.isSuccess()) {
            return Result.success(prepareResult.value());
        } else {
            return Result.failure(prepareResult.notification());
        }
    }

    public static final class Builder
            extends AbstractBuilder<MorabeheLoanArrangementId, BaseFormulaField, MorabeheLoanArrangement, Builder> {

        public Builder(FeatureConfig featureConfig) {
            super(featureConfig);
            Objects.requireNonNull(featureConfig, "FeatureConfig cannot be null for MorabeheLoanArrangement Builder");
        }

        public Builder(Builder other) {
            super(other);
        }

        @Override
        protected MorabeheLoanArrangement buildInternal() {
            return new MorabeheLoanArrangement(this);
        }
    }
}
