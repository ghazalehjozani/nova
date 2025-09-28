package ir.dotin.loan.trade.core.domain.loantype.entity;

import java.time.Clock;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.commons.domain.event.DomainEvent;
import ir.dotin.loan.baseloan.core.domain.loantype.entity.AbstractLoanType;
import ir.dotin.loan.baseloan.core.domain.loantype.i18n.LoanTypeLocalizedMessageCodes;
import ir.dotin.loan.baseloan.core.domain.loantype.vo.LoanTopicConfiguration;
import ir.dotin.loan.baseloan.core.domain.shared.vo.Active;
import ir.dotin.loan.baseloan.core.domain.shared.vo.Disable;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanArrangementId;
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
            LoanTypeId currentAggregateId, AbstractLoanTypeBuilder<?, ?> validatedBuilder, Clock clock) {

        Builder builder = (Builder) validatedBuilder;
        Result<TradeLoanType> tradeLoanTypeResult = builder.build();

        checkState(
                !tradeLoanTypeResult.hasErrors(),
                "Failed to build loan type from validated builder: %s",
                tradeLoanTypeResult.notification().getErrorMessages());

        TradeLoanType tradeLoanType = tradeLoanTypeResult.value();
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

    @SuppressWarnings("unchecked")
    @Override
    public Set<LoanTopicConfiguration<TradeRelationType>> getLoanTopicAssignments() {
        return Collections.unmodifiableSet(
                (Set<LoanTopicConfiguration<TradeRelationType>>) (Set<?>) super.loanTopicAssignments);
    }

    public Result<Builder> prepareNewVersion(Builder updatedBuilder, Clock clock) {
        Result<Builder> prepareResult = super.prepareNewVersion(updatedBuilder, clock);
        if (prepareResult.isSuccess()) {
            Builder successValue = prepareResult.value();
            checkState(successValue != null, "Successful result cannot have null value");
            return Result.success(successValue);
        } else {
            return Result.failure(prepareResult.notification());
        }
    }

    @Override
    protected Result<Void> validateInternalState() {
        return super.validateInternalState();
    }

    private static Notification validateLoanArrangementIds(Set<LoanArrangementId> arrangementIds) {
        Notification notification = Notification.create();
        if (arrangementIds == null || arrangementIds.isEmpty()) {
            notification = notification.addError(LoanTypeLocalizedMessageCodes.LOAN_RULE_IDS_EMPTY);
        } else if (arrangementIds.stream().anyMatch(Objects::isNull)) {
            notification = notification.addError(
                    LoanTypeLocalizedMessageCodes.FIELD_INVALID, "loanArrangementIds", "contains null elements");
        }
        return notification;
    }

    public static final class Builder extends AbstractLoanType.AbstractLoanTypeBuilder<TradeLoanType, Builder> {

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
            Notification notification = super.validate();
            notification.merge(validateLoanArrangementIds(this.loanArrangementIds));
            return notification;
        }
    }
}
