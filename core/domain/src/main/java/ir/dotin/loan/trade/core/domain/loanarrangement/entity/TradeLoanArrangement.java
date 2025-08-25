package ir.dotin.loan.trade.core.domain.loanarrangement.entity;

import java.time.Clock;

import ir.dotin.platform.domain.common.Result;
import ir.dotin.platform.domain.common.event.DomainEvent;
import ir.dotin.platform.domain.common.feature.FeatureConfig;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.entity.AbstractLoanArrangement;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.vo.ArrangementFeatureContext;
import ir.dotin.loan.baseloan.core.domain.shared.formula.BaseFormulaField;
import ir.dotin.loan.baseloan.core.domain.shared.vo.Active;
import ir.dotin.loan.baseloan.core.domain.shared.vo.Disable;
import ir.dotin.loan.trade.core.domain.loanarrangement.event.NewTradeLoanArrangementVersionPrepared;
import ir.dotin.loan.trade.core.domain.loanarrangement.event.TradeLoanArrangementActivated;
import ir.dotin.loan.trade.core.domain.loanarrangement.event.TradeLoanArrangementCreated;
import ir.dotin.loan.trade.core.domain.loanarrangement.event.TradeLoanArrangementDeactivated;
import ir.dotin.loan.trade.core.domain.loanarrangement.vo.TradeLoanArrangementId;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public final class TradeLoanArrangement extends AbstractLoanArrangement<TradeLoanArrangementId, BaseFormulaField> {

    private TradeLoanArrangement(Builder builder) {
        super(builder);
    }

    @Override
    protected Result<Void> validateInternalState() {
        return super.validateInternalState();
    }

    public static Builder newBuilder(FeatureConfig featureConfig) {
        return new Builder(featureConfig);
    }

    public static Result<TradeLoanArrangement> create(Builder builder, Clock clock) {

        requireNonNull(clock, "Clock cannot be null for creation");
        requireNonNull(builder, "Builder cannot be null for creation");

        builder.withId(TradeLoanArrangementId.generate());

        builder.withActive(new Active(true));
        builder.withDisable(new Disable(false));
        builder.withPreviousVersion(null);

        Result<TradeLoanArrangement> arrangementResult = builder.build();

        if (arrangementResult.isFailure()) {
            return Result.failure(arrangementResult.notification());
        }

        TradeLoanArrangement arrangement = arrangementResult.value();
        requireNonNull(arrangement, "arrangement cannot be null after successful build");

        var payload = new TradeLoanArrangementCreated.Payload(
                arrangement.getCode(),
                arrangement.getTitle(),
                arrangement.getCurrencies(),
                arrangement.getAmountRange(),
                arrangement.getDurationRange(),
                arrangement.getPartyType(),
                arrangement.getLifeInsurancePaymentType(),
                arrangement.getLoanSecondaryType(),
                arrangement.getSectionType(),
                arrangement.getInterestPolicy(),
                arrangement.getPenaltyPolicy(),
                arrangement.getInstallmentPolicy(),
                arrangement.getGracePeriodPolicy(),
                arrangement.getRepaymentPriorityPolicy(),
                arrangement.getRegulatoryCompliancePolicy(),
                arrangement.getCollateralPolicy(),
                requireNonNull(arrangement.getGuarantorCount(), "guarantorCount cannot be null"),
                arrangement.isHasInstallmentCard(),
                requireNonNull(arrangement.getConfirmType(), "confirmType cannot be null"),
                requireNonNull(arrangement.getDisbursementMethod(), "disbursementType cannot be null"),
                arrangement.getActive().isActive());

        TradeLoanArrangementCreated creationEvent =
                new TradeLoanArrangementCreated(randomUUID(), arrangement.getId(), payload, clock.instant());

        arrangement.registerEvent(creationEvent);

        return Result.success(arrangement);
    }

    public static TradeLoanArrangement reconstitute(Builder builder) {
        requireNonNull(builder, "Builder cannot be null for reconstitution.");
        return builder.buildInternal();
    }

    @Override
    protected DomainEvent<?, ?> getArrangementActivatedEvent(TradeLoanArrangementId aggregateId, Clock clock) {
        var payload = new TradeLoanArrangementActivated.Payload();
        return new TradeLoanArrangementActivated(randomUUID(), aggregateId, payload, clock.instant());
    }

    @Override
    protected DomainEvent<?, ?> getArrangementDeactivatedEvent(TradeLoanArrangementId aggregateId, Clock clock) {
        var payload = new TradeLoanArrangementDeactivated.Payload();
        return new TradeLoanArrangementDeactivated(randomUUID(), aggregateId, payload, clock.instant());
    }

    @Override
    protected DomainEvent<?, ?> getNewArrangementVersionPreparedEvent(
            TradeLoanArrangementId currentAggregateId,
            AbstractBuilder<TradeLoanArrangementId, BaseFormulaField, ?, ?> validatedBuilder,
            Clock clock) {
        Builder morabeheBuilder = (Builder) validatedBuilder;
        Result<TradeLoanArrangement> loanArrangementResult = morabeheBuilder.build();

        checkState(
                !loanArrangementResult.hasErrors(),
                "Failed to build loan arrangement from validated builder: %s",
                loanArrangementResult.notification().getErrorMessages());

        TradeLoanArrangement loanArrangement = loanArrangementResult.value();
        requireNonNull(loanArrangement, "loanArrangement cannot be null after successful build");
        TradeLoanArrangementId newVersionId = loanArrangement.getId();

        var payload = new NewTradeLoanArrangementVersionPrepared.Payload(
                newVersionId,
                currentAggregateId,
                loanArrangement.getCode(),
                loanArrangement.getTitle(),
                loanArrangement.getCurrencies(),
                loanArrangement.getAmountRange(),
                loanArrangement.getDurationRange(),
                loanArrangement.getPartyType(),
                loanArrangement.getLifeInsurancePaymentType(),
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
                loanArrangement.getConfirmType(),
                loanArrangement.getDisbursementMethod());

        return new NewTradeLoanArrangementVersionPrepared(randomUUID(), newVersionId, payload, clock.instant());
    }

    @Override
    public Result<TradeLoanArrangement> activate(Clock clock) {
        requireNonNull(clock, "Clock cannot be null for activation");

        return super.activate(clock).cast(TradeLoanArrangement.class);
    }

    @Override
    public Result<TradeLoanArrangement> deactivate(Clock clock) {
        requireNonNull(clock, "Clock cannot be null for deactivation");
        return super.deactivate(clock).cast(TradeLoanArrangement.class);
    }

    public Result<Builder> prepareNewVersion(
            Builder updatedBuilder, ArrangementFeatureContext featureContext, Clock clock) {

        Result<Builder> prepareResult = super.prepareNewVersion(updatedBuilder, featureContext, clock);

        if (prepareResult.isSuccess()) {
            return Result.success(prepareResult.orElseThrow());
        } else {
            return Result.failure(prepareResult.notification());
        }
    }

    public static final class Builder
            extends AbstractBuilder<TradeLoanArrangementId, BaseFormulaField, TradeLoanArrangement, Builder> {

        public Builder(FeatureConfig featureConfig) {
            super(featureConfig);
            requireNonNull(featureConfig, "FeatureConfig cannot be null for MorabeheLoanArrangement Builder");
        }

        public Builder(Builder other) {
            super(other);
        }

        @Override
        protected TradeLoanArrangement buildInternal() {
            return new TradeLoanArrangement(this);
        }
    }
}
