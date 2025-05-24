package ir.dotin.loan.trade.core.domain.disbursement.aggregate;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import ir.dotin.platform.domain.common.Notification;
import ir.dotin.platform.domain.common.Result;
import ir.dotin.platform.domain.common.event.DomainEvent;
import ir.dotin.loan.baseloan.core.domain.disbursement.aggregate.AbstractDisbursementRecord;
import ir.dotin.loan.baseloan.core.domain.shared.vo.FailureReason;
import ir.dotin.loan.baseloan.core.domain.shared.vo.transaction.TransactionNumber;
import ir.dotin.loan.trade.core.domain.disbursement.event.TradeDisbursementCompletedEvent;
import ir.dotin.loan.trade.core.domain.disbursement.event.TradeDisbursementFailedEvent;
import ir.dotin.loan.trade.core.domain.disbursement.event.TradeDisbursementTransactionPostedEvent;
import ir.dotin.loan.trade.core.domain.disbursement.i18n.TradeDisbursementLocalizedMessageCodes;
import ir.dotin.loan.trade.core.domain.disbursement.vo.TradeDisbursementRecordId;
import ir.dotin.loan.trade.core.domain.loanfacility.vo.TradeLoanFacilityId;

public final class TradeDisbursementRecord extends AbstractDisbursementRecord<TradeDisbursementRecordId> {

    private final TradeLoanFacilityId loanFacilityId;

    private TradeDisbursementRecord(TradeDisbursementRecord.Builder builder) {
        super(builder);
        this.loanFacilityId = builder.loanFacilityId;
    }

    public static TradeDisbursementRecord.Builder newBuilder() {
        return new TradeDisbursementRecord.Builder();
    }

    public static Result<TradeDisbursementRecord> create(Builder builder) {
        Objects.requireNonNull(builder, "Builder cannot be null for create.");
        return builder.withId(TradeDisbursementRecordId.generate()).build();
    }

    public static TradeDisbursementRecord reconstitute(Builder builder) {
        Objects.requireNonNull(builder, "Builder cannot be null for reconstitution.");
        return builder.buildInternal();
    }

    @Override
    public TradeLoanFacilityId getLoanFacilityId() {
        return loanFacilityId;
    }

    @Override
    protected DomainEvent<?, ?> getTransactionPostedEvent(TradeDisbursementRecordId id, Clock clock) {
        return TradeDisbursementTransactionPostedEvent.create(
                UUID.randomUUID(), getId(), Instant.now(clock), getLoanFacilityId());
    }

    @Override
    protected DomainEvent<?, ?> getDisbursementCompletedEvent(
            TradeDisbursementRecordId id, List<TransactionNumber> transactionNumbers, Clock clock) {
        return new TradeDisbursementCompletedEvent(UUID.randomUUID(), id, transactionNumbers, clock.instant());
    }

    @Override
    protected DomainEvent<?, ?> getDisbursementFailedEvent(
            TradeDisbursementRecordId id, FailureReason reason, Clock clock) {
        return new TradeDisbursementFailedEvent(UUID.randomUUID(), id, reason, clock.instant());
    }

    public static class Builder
            extends AbstractDisbursementRecord.AbstractBuilder<
                    TradeDisbursementRecordId, TradeDisbursementRecord, TradeDisbursementRecord.Builder> {

        private TradeLoanFacilityId loanFacilityId;

        public Builder withLoanFacilityId(TradeLoanFacilityId loanFacilityId) {
            this.loanFacilityId = loanFacilityId;
            return this;
        }

        @Override
        protected TradeDisbursementRecord buildInternal() {
            return new TradeDisbursementRecord(this);
        }

        @Override
        public Notification validate() {
            Notification baseResult = super.validate();
            if (loanFacilityId == null) {
                return baseResult.addError(TradeDisbursementLocalizedMessageCodes.FIELD_REQUIRED, "loanFacilityId");
            }
            return baseResult;
        }
    }
}
