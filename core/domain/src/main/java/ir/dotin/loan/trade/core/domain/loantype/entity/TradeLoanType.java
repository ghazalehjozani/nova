package ir.dotin.loan.trade.core.domain.loantype.entity;

import java.time.Clock;

import com.google.common.collect.ImmutableSetMultimap;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.commons.domain.event.DomainEvent;
import ir.dotin.loan.baseloan.core.domain.loantype.entity.AbstractLoanType;
import ir.dotin.loan.baseloan.core.domain.shared.enums.RelationType;
import ir.dotin.loan.baseloan.core.domain.shared.vo.Active;
import ir.dotin.loan.baseloan.core.domain.shared.vo.Disable;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTopic;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTypeId;
import ir.dotin.loan.trade.core.domain.loantype.enums.TradeRelationType;
import ir.dotin.loan.trade.core.domain.loantype.event.NewTradeLoanTypeVersionPrepared;
import ir.dotin.loan.trade.core.domain.loantype.event.TradeLoanTypeActivated;
import ir.dotin.loan.trade.core.domain.loantype.event.TradeLoanTypeCreated;
import ir.dotin.loan.trade.core.domain.loantype.event.TradeLoanTypeDeactivated;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public final class TradeLoanType extends AbstractLoanType {

    private TradeLoanType(Builder builder) {
        super(builder);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Result<TradeLoanType> create(Builder builder, Clock clock) {
        requireNonNull(clock, "Clock cannot be null for creation");
        requireNonNull(builder, "Builder cannot be null for creation");

        builder.id(LoanTypeId.of(randomUUID()));
        builder.active(new Active(true));
        builder.disable(new Disable(false));

        // Suppress NullAway for this specific call since previous version is null for initial creation
        @SuppressWarnings({"nullness", "NullAway"})
        var ignored = builder.previousVersion(null);

        Notification notification = builder.validate();
        if (notification.hasErrors()) {
            return Result.failure(notification);
        }

        TradeLoanType loanType = builder.buildInternal();

        var payload = new TradeLoanTypeCreated.Payload(loanType.getCode());

        var creationEvent = new TradeLoanTypeCreated(randomUUID(), loanType.getId(), payload, clock.instant());
        loanType.registerEvent(creationEvent);

        return Result.success(loanType);
    }

    public static TradeLoanType reconstitute(Builder builder) {
        requireNonNull(builder, "Builder cannot be null for reconstitution.");
        return builder.buildInternal();
    }

    @Override
    protected DomainEvent<?, ?> getLoanTypeActivatedEvent(LoanTypeId aggregateId, Clock clock) {
        var payload = new TradeLoanTypeActivated.Payload();
        return new TradeLoanTypeActivated(randomUUID(), aggregateId, payload, clock.instant());
    }

    @Override
    protected DomainEvent<?, ?> getLoanTypeDeactivatedEvent(LoanTypeId aggregateId, Clock clock) {
        var payload = new TradeLoanTypeDeactivated.Payload();
        return new TradeLoanTypeDeactivated(randomUUID(), aggregateId, payload, clock.instant());
    }

    @Override
    protected DomainEvent<?, ?> getNewVersionPreparedEvent(
            LoanTypeId currentAggregateId, AbstractLoanTypeBuilder<?, ?, ?> validatedBuilder, Clock clock) {

        Builder builder = (Builder) validatedBuilder;
        TradeLoanType tradeLoanType = builder.build();
        requireNonNull(tradeLoanType, "tradeLoanType cannot be null after successful build");
        LoanTypeId newAggregateId = tradeLoanType.getId();

        var payload = new NewTradeLoanTypeVersionPrepared.Payload(newAggregateId, currentAggregateId);
        return new NewTradeLoanTypeVersionPrepared(randomUUID(), newAggregateId, payload, clock.instant());
    }

    @Override
    public Result<TradeLoanType> activate(Clock clock) {
        return super.activate(clock).cast(TradeLoanType.class);
    }

    @Override
    public Result<TradeLoanType> deactivate(Clock clock) {
        return super.deactivate(clock).cast(TradeLoanType.class);
    }

    @Override
    @SuppressWarnings("unchecked")
    public ImmutableSetMultimap<RelationType<TradeRelationType>, LoanTopic> getRelationTypeLoanTopics() {
        return (ImmutableSetMultimap<RelationType<TradeRelationType>, LoanTopic>)
                (ImmutableSetMultimap<?, ?>) this.relationTypeLoanTopics;
    }

    public Result<Builder> prepareNewVersion(Builder updatedBuilder, Clock clock) {
        Result<Builder> prepareResult = super.prepareNewVersion(updatedBuilder, clock);
        if (prepareResult.isSuccess()) {
            Builder successValue = prepareResult.value();
            checkState(successValue != null, "Successful result cannot have null number");
            return Result.success(successValue);
        } else {
            return Result.failure(prepareResult.notification());
        }
    }

    @Override
    protected Result<Void> validateInternalState() {
        return super.validateInternalState();
    }

    public static final class Builder
            extends AbstractLoanType.AbstractLoanTypeBuilder<TradeRelationType, TradeLoanType, Builder> {

        public Builder() {
            super();
        }

        public Builder(Builder other) {
            super(other);
        }

        @Override
        public TradeLoanType buildInternal() {
            return new TradeLoanType(this);
        }

        @Override
        public Notification validate() {
            return super.validate();
        }
    }
}
