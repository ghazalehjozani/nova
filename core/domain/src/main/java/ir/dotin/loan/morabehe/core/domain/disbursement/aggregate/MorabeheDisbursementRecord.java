package ir.dotin.loan.morabehe.core.domain.disbursement.aggregate;

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
import ir.dotin.loan.morabehe.core.domain.disbursement.event.MorabeheDisbursementCompletedEvent;
import ir.dotin.loan.morabehe.core.domain.disbursement.event.MorabeheDisbursementFailedEvent;
import ir.dotin.loan.morabehe.core.domain.disbursement.event.MorabeheDisbursementTransactionPostedEvent;
import ir.dotin.loan.morabehe.core.domain.disbursement.i18n.MorabeheDisbursementLocalizedMessageCodes;
import ir.dotin.loan.morabehe.core.domain.disbursement.vo.MorabeheDisbursementRecordId;
import ir.dotin.loan.morabehe.core.domain.loanfacility.vo.MorabeheLoanFacilityId;

public final class MorabeheDisbursementRecord extends AbstractDisbursementRecord<MorabeheDisbursementRecordId> {

    private final MorabeheLoanFacilityId loanFacilityId;

    private MorabeheDisbursementRecord(MorabeheDisbursementRecord.Builder builder) {
        super(builder);
        this.loanFacilityId = builder.loanFacilityId;
    }

    public static MorabeheDisbursementRecord.Builder newBuilder() {
        return new MorabeheDisbursementRecord.Builder();
    }

    public static Result<MorabeheDisbursementRecord> create(Builder builder) {
        Objects.requireNonNull(builder, "Builder cannot be null for create.");
        return builder.withId(MorabeheDisbursementRecordId.generate()).build();
    }

    public static MorabeheDisbursementRecord reconstitute(Builder builder) {
        Objects.requireNonNull(builder, "Builder cannot be null for reconstitution.");
        return builder.buildInternal();
    }

    @Override
    public MorabeheLoanFacilityId getLoanFacilityId() {
        return loanFacilityId;
    }

    @Override
    protected DomainEvent<?, ?> getTransactionPostedEvent(MorabeheDisbursementRecordId id, Clock clock) {
        return MorabeheDisbursementTransactionPostedEvent.create(
                UUID.randomUUID(), getId(), Instant.now(clock), getLoanFacilityId());
    }

    @Override
    protected DomainEvent<?, ?> getDisbursementCompletedEvent(
            MorabeheDisbursementRecordId id, List<TransactionNumber> transactionNumbers, Clock clock) {
        return new MorabeheDisbursementCompletedEvent(UUID.randomUUID(), id, transactionNumbers, clock.instant());
    }

    @Override
    protected DomainEvent<?, ?> getDisbursementFailedEvent(
            MorabeheDisbursementRecordId id, FailureReason reason, Clock clock) {
        return new MorabeheDisbursementFailedEvent(UUID.randomUUID(), id, reason, clock.instant());
    }

    public static class Builder
            extends AbstractDisbursementRecord.AbstractBuilder<
                    MorabeheDisbursementRecordId, MorabeheDisbursementRecord, MorabeheDisbursementRecord.Builder> {

        private MorabeheLoanFacilityId loanFacilityId;

        public Builder withLoanFacilityId(MorabeheLoanFacilityId loanFacilityId) {
            this.loanFacilityId = loanFacilityId;
            return this;
        }

        @Override
        protected MorabeheDisbursementRecord buildInternal() {
            return new MorabeheDisbursementRecord(this);
        }

        @Override
        public Notification validate() {
            Notification baseResult = super.validate();
            if (loanFacilityId == null) {
                return baseResult.addError(MorabeheDisbursementLocalizedMessageCodes.FIELD_REQUIRED, "loanFacilityId");
            }
            return baseResult;
        }
    }
}
