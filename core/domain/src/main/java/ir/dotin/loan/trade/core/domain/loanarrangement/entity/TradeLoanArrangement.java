package ir.dotin.loan.trade.core.domain.loanarrangement.entity;

import java.time.Clock;

import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.commons.domain.event.DomainEvent;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.entity.AbstractLoanArrangement;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.vo.ArrangementFeatureContext;
import ir.dotin.loan.baseloan.core.domain.shared.vo.Active;
import ir.dotin.loan.baseloan.core.domain.shared.vo.Disable;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanArrangementId;
import ir.dotin.loan.trade.core.domain.loanarrangement.event.NewTradeLoanArrangementVersionPrepared;
import ir.dotin.loan.trade.core.domain.loanarrangement.event.TradeLoanArrangementActivated;
import ir.dotin.loan.trade.core.domain.loanarrangement.event.TradeLoanArrangementCreated;
import ir.dotin.loan.trade.core.domain.loanarrangement.event.TradeLoanArrangementDeactivated;
import ir.dotin.loan.trade.core.domain.shared.formula.TradeLoanFacilityFormulaField;
import ir.dotin.loan.trade.core.domain.shared.formula.TradeLoanParameterProvider;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public final class TradeLoanArrangement
        extends AbstractLoanArrangement<TradeLoanParameterProvider, TradeLoanFacilityFormulaField> {

    private TradeLoanArrangement(Builder builder) {
        super(builder);
    }

    @Override
    protected Result<Void> validateInternalState() {
        return super.validateInternalState();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Result<TradeLoanArrangement> create(Builder builder, Clock clock) {

        requireNonNull(clock, "Clock cannot be null for creation");
        requireNonNull(builder, "Builder cannot be null for creation");

        builder.id(LoanArrangementId.generate());

        builder.active(new Active(true));
        builder.disable(new Disable(false));
        builder.previousVersion(null);

        TradeLoanArrangement arrangement = builder.build();
        requireNonNull(arrangement, "arrangement cannot be null after successful build");

        var payload = new TradeLoanArrangementCreated.Payload(arrangement.getId());

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
    protected DomainEvent<?, ?> getArrangementActivatedEvent(LoanArrangementId aggregateId, Clock clock) {
        var payload = new TradeLoanArrangementActivated.Payload();
        return new TradeLoanArrangementActivated(randomUUID(), aggregateId, payload, clock.instant());
    }

    @Override
    protected DomainEvent<?, ?> getArrangementDeactivatedEvent(LoanArrangementId aggregateId, Clock clock) {
        var payload = new TradeLoanArrangementDeactivated.Payload();
        return new TradeLoanArrangementDeactivated(randomUUID(), aggregateId, payload, clock.instant());
    }

    @Override
    protected DomainEvent<?, ?> getNewArrangementVersionPreparedEvent(
            LoanArrangementId currentAggregateId,
            AbstractBuilder<TradeLoanParameterProvider, TradeLoanFacilityFormulaField, ?, ?> validatedBuilder,
            Clock clock) {
        Builder morabeheBuilder = (Builder) validatedBuilder;
        TradeLoanArrangement loanArrangement = morabeheBuilder.build();

        requireNonNull(loanArrangement, "loanArrangement cannot be null after successful build");
        LoanArrangementId newVersionId = loanArrangement.getId();

        return new NewTradeLoanArrangementVersionPrepared(randomUUID(), newVersionId, clock.instant());
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
            extends AbstractBuilder<
                    TradeLoanParameterProvider, TradeLoanFacilityFormulaField, TradeLoanArrangement, Builder> {

        public Builder() {
            super();
        }

        public Builder(Builder other) {
            super(other);
        }

        @Override
        public TradeLoanArrangement buildInternal() {
            return new TradeLoanArrangement(this);
        }
    }
}
