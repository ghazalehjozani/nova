package ir.dotin.loan.trade.core.domain.loantype.entity;

import java.time.Clock;
import java.util.*;

import com.google.common.collect.ImmutableSet;

import ir.dotin.platform.domain.common.Notification;
import ir.dotin.platform.domain.common.Result;
import ir.dotin.platform.domain.common.entity.Identity;
import ir.dotin.platform.domain.common.event.DomainEvent;
import ir.dotin.platform.domain.common.feature.FeatureConfig;
import ir.dotin.loan.baseloan.core.domain.loantype.entity.AbstractLoanType;
import ir.dotin.loan.baseloan.core.domain.loantype.i18n.LoanTypeLocalizedMessageCodes;
import ir.dotin.loan.baseloan.core.domain.shared.vo.Active;
import ir.dotin.loan.baseloan.core.domain.shared.vo.Disable;
import ir.dotin.loan.trade.core.domain.loanarrangement.vo.TradeLoanArrangementId;
import ir.dotin.loan.trade.core.domain.loantype.event.NewTradeLoanTypeVersionPrepared;
import ir.dotin.loan.trade.core.domain.loantype.event.TradeLoanTypeActivated;
import ir.dotin.loan.trade.core.domain.loantype.event.TradeLoanTypeCreated;
import ir.dotin.loan.trade.core.domain.loantype.event.TradeLoanTypeDeactivated;
import ir.dotin.loan.trade.core.domain.loantype.vo.TradeLoanTypeId;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;
import static java.util.UUID.randomUUID;

public final class TradeLoanType extends AbstractLoanType<TradeLoanTypeId> {

    private final Set<TradeLoanArrangementId> loanArrangementIds;

    private TradeLoanType(Builder builder) {
        super(builder);
        this.loanArrangementIds = requireNonNullElse(builder.loanArrangementIds, ImmutableSet.of());
        validateMorabeheState();
    }

    public static Builder newBuilder(FeatureConfig featureConfig) {
        return new Builder(featureConfig);
    }

    public static Result<TradeLoanType> create(Builder builder, Clock clock) {
        requireNonNull(clock, "Clock cannot be null for creation");
        requireNonNull(builder, "Builder cannot be null for creation");

        builder.withId(TradeLoanTypeId.generate());
        builder.withActive(new Active(true));
        builder.withDisable(new Disable(false));

        // Suppress NullAway for this specific call since previous version is null for initial creation
        @SuppressWarnings({"nullness", "NullAway"})
        var ignored = builder.withPreviousVersion(null);

        Notification notification = builder.validate();
        if (notification.hasErrors()) {
            return Result.failure(notification);
        }

        TradeLoanType loanType = builder.buildInternal();

        var payload = new TradeLoanTypeCreated.Payload(
                loanType.getCode(),
                loanType.getTitle(),
                loanType.getGatewayType(),
                loanType.getLoanApplicationAllowed(),
                loanType.getSegmentType(),
                loanType.getEconomicSectors(),
                loanType.getLoanTopicAssignments(),
                loanType.getIncomeIds(),
                loanType.getAttributes(),
                loanType.getGroupId(),
                loanType.getLoanArrangementIds(),
                loanType.getActive().isActive());

        var creationEvent = new TradeLoanTypeCreated(randomUUID(), loanType.getId(), payload, clock.instant());
        loanType.registerEvent(creationEvent);

        return Result.success(loanType);
    }

    public static TradeLoanType reconstitute(Builder builder) {
        requireNonNull(builder, "Builder cannot be null for reconstitution.");
        return builder.buildInternal();
    }

    @Override
    public Set<Identity> getLoanRuleIds() {
        return Set.copyOf(this.loanArrangementIds);
    }

    @Override
    protected DomainEvent<?, ?> getLoanTypeActivatedEvent(TradeLoanTypeId aggregateId, Clock clock) {
        var payload = new TradeLoanTypeActivated.Payload();
        return new TradeLoanTypeActivated(randomUUID(), aggregateId, payload, clock.instant());
    }

    @Override
    protected DomainEvent<?, ?> getLoanTypeDeactivatedEvent(TradeLoanTypeId aggregateId, Clock clock) {
        var payload = new TradeLoanTypeDeactivated.Payload();
        return new TradeLoanTypeDeactivated(randomUUID(), aggregateId, payload, clock.instant());
    }

    @Override
    protected DomainEvent<?, ?> getNewVersionPreparedEvent(
            TradeLoanTypeId currentAggregateId,
            AbstractLoanTypeBuilder<TradeLoanTypeId, ?, ?> validatedBuilder,
            Clock clock) {

        Builder builder = (Builder) validatedBuilder;
        Result<TradeLoanType> morabeheLoanTypeResult = builder.build();

        checkState(
                !morabeheLoanTypeResult.hasErrors(),
                "Failed to build loan type from validated builder: %s",
                morabeheLoanTypeResult.notification().getErrorMessages());

        TradeLoanType morabeheLoanType = morabeheLoanTypeResult.value();
        requireNonNull(morabeheLoanType, "morabeheLoanType cannot be null after successful build");
        TradeLoanTypeId newAggregateId = morabeheLoanType.getId();

        var payload = new NewTradeLoanTypeVersionPrepared.Payload(
                newAggregateId,
                currentAggregateId,
                morabeheLoanType.getCode(),
                morabeheLoanType.getTitle(),
                morabeheLoanType.getEditReason(),
                morabeheLoanType.getGatewayType(),
                morabeheLoanType.getLoanApplicationAllowed(),
                morabeheLoanType.getSegmentType(),
                morabeheLoanType.getEconomicSectors(),
                morabeheLoanType.getLoanTopicAssignments(),
                morabeheLoanType.getIncomeIds(),
                morabeheLoanType.getAttributes(),
                morabeheLoanType.getGroupId(),
                morabeheLoanType.loanArrangementIds);
        return new NewTradeLoanTypeVersionPrepared(randomUUID(), newAggregateId, payload, clock.instant());
    }

    public Set<TradeLoanArrangementId> getLoanArrangementIds() {
        return loanArrangementIds;
    }

    @Override
    public Result<TradeLoanType> activate(Clock clock) {
        return super.activate(clock).cast(TradeLoanType.class);
    }

    @Override
    public Result<TradeLoanType> deactivate(Clock clock) {
        return super.deactivate(clock).cast(TradeLoanType.class);
    }

    public Result<Builder> prepareNewVersion(Builder updatedBuilder, Clock clock) {
        Notification specificValidation = validateMorabeheNewVersionData(updatedBuilder);
        if (specificValidation.hasErrors()) {
            return Result.failure(specificValidation);
        }
        Result<Builder> prepareResult = super.prepareNewVersion(updatedBuilder, clock);
        if (prepareResult.isSuccess()) {
            Builder successValue = prepareResult.value();
            checkState(successValue != null, "Successful result cannot have null value");
            return Result.success(successValue);
        } else {
            return Result.failure(prepareResult.notification());
        }
    }

    private void validateMorabeheState() {
        Notification notification = validateMorabeheLoanArrangementIds(this.loanArrangementIds);
        checkState(
                !notification.hasErrors(),
                "Internal MorabeheLoanType state validation failed: %s",
                notification.getErrorMessages());
    }

    private Notification validateMorabeheNewVersionData(Builder builder) {
        return validateMorabeheLoanArrangementIds(builder.loanArrangementIds);
    }

    private static Notification validateMorabeheLoanArrangementIds(Set<TradeLoanArrangementId> arrangementIds) {
        Notification notification = Notification.create();
        if (arrangementIds == null || arrangementIds.isEmpty()) {
            notification = notification.addError(LoanTypeLocalizedMessageCodes.LOAN_RULE_IDS_EMPTY);
        } else if (arrangementIds.stream().anyMatch(Objects::isNull)) {
            notification = notification.addError(
                    LoanTypeLocalizedMessageCodes.FIELD_INVALID, "loanArrangementIds", "contains null elements");
        }
        return notification;
    }

    public static final class Builder
            extends AbstractLoanType.AbstractLoanTypeBuilder<TradeLoanTypeId, TradeLoanType, Builder> {

        private Set<TradeLoanArrangementId> loanArrangementIds = new HashSet<>();

        public Builder(FeatureConfig featureConfig) {
            super(featureConfig);
        }

        private Builder(Builder other) {
            super(other);
            this.loanArrangementIds =
                    (other.loanArrangementIds != null) ? new HashSet<>(other.loanArrangementIds) : new HashSet<>();
        }

        @Override
        public Builder withLoanArrangementIds(Set<Identity> val) {
            if (val == null) {
                this.loanArrangementIds = new HashSet<>();
                return self();
            }
            Set<TradeLoanArrangementId> specificIds = new HashSet<>();
            for (Identity id : val) {
                if (id instanceof TradeLoanArrangementId specificId) {
                    specificIds.add(specificId);
                } else if (id != null) {
                    throw new IllegalArgumentException(
                            "Invalid Identity type provided for LoanArrangementIds. Expected MorabeheLoanArrangementId, got "
                                    + id.getClass().getName());
                }
            }
            this.loanArrangementIds = specificIds;
            return self();
        }

        public Builder withMorabeheLoanArrangementIds(Set<TradeLoanArrangementId> val) {
            this.loanArrangementIds = (val != null) ? new HashSet<>(val) : new HashSet<>();
            return self();
        }

        @Override
        protected TradeLoanType buildInternal() {
            return new TradeLoanType(this);
        }

        @Override
        public Notification validate() {
            Notification notification = super.validate();
            notification.merge(validateMorabeheLoanArrangementIds(this.loanArrangementIds));
            return notification;
        }
    }
}
