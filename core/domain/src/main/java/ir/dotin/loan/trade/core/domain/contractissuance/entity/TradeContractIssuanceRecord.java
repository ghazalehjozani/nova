package ir.dotin.loan.trade.core.domain.contractissuance.entity;

import java.time.Clock;

import org.jspecify.annotations.Nullable;

import ir.dotin.platform.domain.common.Notification;
import ir.dotin.platform.domain.common.Result;
import ir.dotin.platform.domain.common.event.DomainEvent;
import ir.dotin.loan.baseloan.core.domain.contractissuance.entity.AbstractContractIssuanceRecord;
import ir.dotin.loan.baseloan.core.domain.contractissuance.i18n.ContractIssuanceLocalizedMessageCodes;
import ir.dotin.loan.baseloan.core.domain.contractissuance.vo.ContractReference;
import ir.dotin.loan.baseloan.core.domain.shared.vo.FailureReason;
import ir.dotin.loan.trade.core.domain.contractissuance.event.TradeContractIssuanceFailedEvent;
import ir.dotin.loan.trade.core.domain.contractissuance.event.TradeContractIssuanceTransactionPostedEvent;
import ir.dotin.loan.trade.core.domain.contractissuance.event.TradeContractIssuedEvent;
import ir.dotin.loan.trade.core.domain.contractissuance.vo.TradeContractIssuanceRecordId;
import ir.dotin.loan.trade.core.domain.loanfacility.vo.TradeLoanFacilityId;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public final class TradeContractIssuanceRecord extends AbstractContractIssuanceRecord<TradeContractIssuanceRecordId> {

    private final TradeLoanFacilityId loanFacilityId;

    private TradeContractIssuanceRecord(Builder builder) {
        super(builder);
        this.loanFacilityId = requireNonNull(builder.loanFacilityId);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static Result<TradeContractIssuanceRecord> create(Builder builder) {
        requireNonNull(builder, "Builder cannot be null for create.");
        return builder.withId(TradeContractIssuanceRecordId.generate()).build();
    }

    public static TradeContractIssuanceRecord reconstitute(Builder builder) {
        requireNonNull(builder, "Builder cannot be null for reconstitution.");
        return builder.buildInternal();
    }

    @Override
    public TradeLoanFacilityId getLoanFacilityId() {
        return loanFacilityId;
    }

    @Override
    protected DomainEvent<?, ?> getTransactionPostedEvent(TradeContractIssuanceRecordId id, Clock clock) {
        return TradeContractIssuanceTransactionPostedEvent.create(
                randomUUID(), getId(), clock.instant(), getLoanFacilityId());
    }

    @Override
    protected DomainEvent<?, ?> getContractIssuedEvent(
            TradeContractIssuanceRecordId id, ContractReference reference, Clock clock) {
        return new TradeContractIssuedEvent(randomUUID(), id, reference, clock.instant());
    }

    @Override
    protected DomainEvent<?, ?> getContractIssuanceFailedEvent(
            TradeContractIssuanceRecordId id, FailureReason reason, Clock clock) {
        return new TradeContractIssuanceFailedEvent(randomUUID(), id, reason, clock.instant());
    }

    public static final class Builder
            extends AbstractBuilder<TradeContractIssuanceRecordId, TradeContractIssuanceRecord, Builder> {

        @Nullable
        private TradeLoanFacilityId loanFacilityId;

        public Builder() {
            super();
        }

        public Builder withLoanFacilityId(TradeLoanFacilityId loanFacilityId) {
            this.loanFacilityId = requireNonNull(loanFacilityId);
            return this;
        }

        public Builder(TradeContractIssuanceRecord other) {
            super(other);
            this.loanFacilityId = other.loanFacilityId;
        }

        public Builder withMethod(TradeLoanFacilityId loanFacilityId) {
            return this.withLoanFacilityId(loanFacilityId);
        }

        @Override
        protected TradeContractIssuanceRecord buildInternal() {
            requireNonNull(loanFacilityId, "loanFacilityId must be set before building");
            return new TradeContractIssuanceRecord(this);
        }

        @Override
        public Notification validate() {
            Notification notification = super.validate();
            if (loanFacilityId == null) {
                return notification.addError(ContractIssuanceLocalizedMessageCodes.FIELD_REQUIRED, "loanFacilityId");
            }
            return notification;
        }
    }
}
