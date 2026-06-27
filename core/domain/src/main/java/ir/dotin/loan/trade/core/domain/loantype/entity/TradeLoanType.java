package ir.dotin.loan.trade.core.domain.loantype.entity;

import java.time.Clock;

import com.google.common.collect.ImmutableSetMultimap;

import ir.dotin.platform.accounting.document.api.enumeration.RelationType;
import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.platform.pangaea.commons.domain.event.DomainEvent;
import ir.dotin.loan.baseloan.core.domain.loantype.entity.AbstractLoanType;
import ir.dotin.loan.baseloan.core.domain.shared.vo.Active;
import ir.dotin.loan.baseloan.core.domain.shared.vo.Disable;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTopic;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTypeGroupId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTypeId;
import ir.dotin.loan.trade.core.domain.loantype.enums.TradeRelationType;
import ir.dotin.loan.trade.core.domain.loantype.event.NewTradeLoanTypeVersionPrepared;
import ir.dotin.loan.trade.core.domain.loantype.event.TradeLoanTypeActivated;
import ir.dotin.loan.trade.core.domain.loantype.event.TradeLoanTypeCreated;
import ir.dotin.loan.trade.core.domain.loantype.event.TradeLoanTypeDeactivated;
import ir.dotin.loan.trade.core.domain.loantype.event.TradeLoanTypeGroupAssigned;
import ir.dotin.loan.trade.core.domain.loantype.event.TradeLoanTypeGroupRemoved;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

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

        builder.id(LoanTypeId.generate());
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

        var creationEvent =
                TradeLoanTypeCreated.of(loanType.getId(), loanType.getCode().value(), clock);
        loanType.registerEvent(creationEvent);

        return Result.success(loanType);
    }

    public static TradeLoanType reconstitute(Builder builder) {
        requireNonNull(builder, "Builder cannot be null for reconstitution.");
        return builder.buildInternal();
    }

    @Override
    protected DomainEvent<?> getLoanTypeActivatedEvent(LoanTypeId aggregateId, Clock clock) {
        return TradeLoanTypeActivated.of(aggregateId, clock);
    }

    @Override
    protected DomainEvent<?> getLoanTypeDeactivatedEvent(LoanTypeId aggregateId, Clock clock) {
        return TradeLoanTypeDeactivated.of(aggregateId, clock);
    }

    @Override
    protected DomainEvent<?> getGroupAssignedEvent(LoanTypeId aggregateId, LoanTypeGroupId groupId, Clock clock) {
        return TradeLoanTypeGroupAssigned.of(aggregateId, groupId, clock);
    }

    @Override
    protected DomainEvent<?> getGroupRemovedEvent(LoanTypeId aggregateId, Clock clock) {
        return TradeLoanTypeGroupRemoved.of(aggregateId, clock);
    }

    @Override
    protected DomainEvent<?> getNewVersionPreparedEvent(
            LoanTypeId currentAggregateId, AbstractLoanTypeBuilder<?, ?, ?> validatedBuilder, Clock clock) {

        Builder builder = (Builder) validatedBuilder;
        TradeLoanType tradeLoanType = builder.build();
        requireNonNull(tradeLoanType, "tradeLoanType cannot be null after successful build");
        LoanTypeId newAggregateId = tradeLoanType.getId();

        return NewTradeLoanTypeVersionPrepared.of(newAggregateId, currentAggregateId, clock);
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
            Builder successValue = prepareResult.unwrap();
            checkState(successValue != null, "Successful result cannot have null number");
            return Result.success(successValue);
        } else {
            return Result.failure(prepareResult.err().orElseThrow().notification());
        }
    }

    @Override
    protected Result<Unit> validateInternalState() {
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
