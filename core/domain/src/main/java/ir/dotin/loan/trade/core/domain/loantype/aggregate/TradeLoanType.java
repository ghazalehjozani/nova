package ir.dotin.loan.trade.core.domain.loantype.aggregate;

import java.time.Clock;
import java.time.Instant;
import java.util.*;

import ir.dotin.platform.domain.common.Notification;
import ir.dotin.platform.domain.common.Result;
import ir.dotin.platform.domain.common.entity.Identity;
import ir.dotin.platform.domain.common.event.DomainEvent;
import ir.dotin.platform.domain.common.feature.FeatureConfig;
import ir.dotin.loan.baseloan.core.domain.loantype.aggregate.AbstractLoanType;
import ir.dotin.loan.baseloan.core.domain.loantype.i18n.LoanTypeLocalizedMessageCodes;
import ir.dotin.loan.baseloan.core.domain.shared.vo.Active;
import ir.dotin.loan.baseloan.core.domain.shared.vo.Disable;
import ir.dotin.loan.trade.core.domain.loanarrangement.vo.TradeLoanArrangementId;
import ir.dotin.loan.trade.core.domain.loantype.event.NewTradeLoanTypeVersionPrepared;
import ir.dotin.loan.trade.core.domain.loantype.event.TradeLoanTypeActivated;
import ir.dotin.loan.trade.core.domain.loantype.event.TradeLoanTypeCreated;
import ir.dotin.loan.trade.core.domain.loantype.event.TradeLoanTypeDeactivated;
import ir.dotin.loan.trade.core.domain.loantype.vo.TradeLoanTypeId;

public final class TradeLoanType extends AbstractLoanType<TradeLoanTypeId> {

    private final Set<TradeLoanArrangementId> loanArrangementIds;

    private TradeLoanType(Builder builder) {
        super(builder);
        this.loanArrangementIds =
                Collections.unmodifiableSet(Objects.requireNonNullElse(builder.loanArrangementIds, Set.of()));
        validateMorabeheState();
    }

    public static Builder newBuilder(FeatureConfig featureConfig) {
        return new Builder(featureConfig);
    }

    public static Result<TradeLoanType> create(Builder builder, Clock clock) {
        Objects.requireNonNull(clock, "Clock cannot be null for creation");
        Objects.requireNonNull(builder, "Builder cannot be null for creation");

        builder.withId(TradeLoanTypeId.generate());
        builder.withActive(new Active(true));
        builder.withDisable(new Disable(false));
        builder.withPreviousVersion(null);

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

        var creationEvent = new TradeLoanTypeCreated(UUID.randomUUID(), loanType.getId(), payload, Instant.now(clock));
        loanType.registerEvent(creationEvent);

        return Result.success(loanType);
    }

    public static TradeLoanType reconstitute(Builder builder) {
        Objects.requireNonNull(builder, "Builder cannot be null for reconstitution.");
        return builder.buildInternal();
    }

    @Override
    public Set<Identity> getLoanRuleIds() {
        return Set.copyOf(this.loanArrangementIds);
    }

    @Override
    protected DomainEvent<?, ?> getLoanTypeActivatedEvent(TradeLoanTypeId aggregateId, Clock clock) {
        var payload = new TradeLoanTypeActivated.Payload();
        return new TradeLoanTypeActivated(UUID.randomUUID(), aggregateId, payload, Instant.now(clock));
    }

    @Override
    protected DomainEvent<?, ?> getLoanTypeDeactivatedEvent(TradeLoanTypeId aggregateId, Clock clock) {
        var payload = new TradeLoanTypeDeactivated.Payload();
        return new TradeLoanTypeDeactivated(UUID.randomUUID(), aggregateId, payload, Instant.now(clock));
    }

    @Override
    protected DomainEvent<?, ?> getNewVersionPreparedEvent(
            TradeLoanTypeId currentAggregateId,
            AbstractLoanTypeBuilder<TradeLoanTypeId, ?, ?> validatedBuilder,
            Clock clock) {

        Builder builder = (Builder) validatedBuilder;
        Result<TradeLoanType> morabeheLoanTypeResult = builder.build();
        TradeLoanType morabeheLoanType = morabeheLoanTypeResult.value();
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
        return new NewTradeLoanTypeVersionPrepared(UUID.randomUUID(), newAggregateId, payload, Instant.now(clock));
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
            return Result.success(prepareResult.value());
        } else {
            return Result.failure(prepareResult.notification());
        }
    }

    private void validateMorabeheState() {
        Notification notification = validateMorabeheLoanArrangementIds(this.loanArrangementIds);
        if (notification.hasErrors()) {
            throw new IllegalStateException(
                    "Internal MorabeheLoanType state validation failed: " + notification.getErrorMessages());
        }
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
