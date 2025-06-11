package ir.dotin.loan.trade.core.domain.disbursement.entity;

import java.time.Clock;
import java.util.List;

import org.jspecify.annotations.Nullable;

import ir.dotin.platform.domain.common.Notification;
import ir.dotin.platform.domain.common.Result;
import ir.dotin.platform.domain.common.event.DomainEvent;
import ir.dotin.loan.baseloan.core.domain.disbursement.entity.AbstractDisbursementRecord;
import ir.dotin.loan.baseloan.core.domain.shared.vo.FailureReason;
import ir.dotin.loan.baseloan.core.domain.shared.vo.TransactionNumber;
import ir.dotin.loan.trade.core.domain.disbursement.event.TradeDisbursementCompletedEvent;
import ir.dotin.loan.trade.core.domain.disbursement.event.TradeDisbursementFailedEvent;
import ir.dotin.loan.trade.core.domain.disbursement.event.TradeDisbursementTransactionPostedEvent;
import ir.dotin.loan.trade.core.domain.disbursement.i18n.TradeDisbursementLocalizedMessageCodes;
import ir.dotin.loan.trade.core.domain.disbursement.vo.TradeDisbursementRecordId;
import ir.dotin.loan.trade.core.domain.loanfacility.vo.TradeLoanFacilityId;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public final class TradeDisbursementRecord extends AbstractDisbursementRecord<TradeDisbursementRecordId> {

    private final TradeLoanFacilityId loanFacilityId;

    private TradeDisbursementRecord(TradeDisbursementRecord.Builder builder) {
        super(builder);
        this.loanFacilityId = requireNonNull(builder.loanFacilityId);
    }

    public static TradeDisbursementRecord.Builder newBuilder() {
        return new TradeDisbursementRecord.Builder();
    }

    public static Result<TradeDisbursementRecord> create(Builder builder) {
        requireNonNull(builder, "Builder cannot be null for create.");
        return builder.withId(TradeDisbursementRecordId.generate()).build();
    }

    public static TradeDisbursementRecord reconstitute(Builder builder) {
        requireNonNull(builder, "Builder cannot be null for reconstitution.");
        return builder.buildInternal();
    }

    @Override
    public TradeLoanFacilityId getLoanFacilityId() {
        return loanFacilityId;
    }

    @Override
    protected DomainEvent<?, ?> getTransactionPostedEvent(TradeDisbursementRecordId id, Clock clock) {
        return TradeDisbursementTransactionPostedEvent.create(
                randomUUID(), getId(), clock.instant(), getLoanFacilityId());
    }

    @Override
    protected DomainEvent<?, ?> getDisbursementCompletedEvent(
            TradeDisbursementRecordId id, List<TransactionNumber> transactionNumbers, Clock clock) {
        return new TradeDisbursementCompletedEvent(randomUUID(), id, transactionNumbers, clock.instant());
    }

    @Override
    protected DomainEvent<?, ?> getDisbursementFailedEvent(
            TradeDisbursementRecordId id, FailureReason reason, Clock clock) {
        return new TradeDisbursementFailedEvent(randomUUID(), id, reason, clock.instant());
    }

    public static class Builder
            extends AbstractDisbursementRecord.AbstractBuilder<
                    TradeDisbursementRecordId, TradeDisbursementRecord, TradeDisbursementRecord.Builder> {

        @Nullable
        private TradeLoanFacilityId loanFacilityId;

        public Builder withLoanFacilityId(TradeLoanFacilityId loanFacilityId) {
            this.loanFacilityId = requireNonNull(loanFacilityId);
            return this;
        }

        @Override
        protected TradeDisbursementRecord buildInternal() {
            requireNonNull(loanFacilityId, "loanFacilityId must be set before building");
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
