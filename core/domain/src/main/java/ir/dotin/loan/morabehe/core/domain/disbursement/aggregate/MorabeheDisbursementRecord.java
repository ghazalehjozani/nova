package ir.dotin.loan.morabehe.core.domain.disbursement.aggregate;

import java.time.Clock;
import java.util.List;
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

    @Override
    public MorabeheLoanFacilityId getLoanApplicationId() {
        return loanFacilityId;
    }

    @Override
    protected DomainEvent<?, ?> getTransactionPostedEvent(MorabeheDisbursementRecordId id, Clock clock) {
        return new MorabeheDisbursementTransactionPostedEvent(UUID.randomUUID(), id, clock.instant());
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
        protected MorabeheDisbursementRecord.Builder self() {
            return this;
        }

        @Override
        protected MorabeheDisbursementRecord buildInternal() {
            return new MorabeheDisbursementRecord(this);
        }

        @Override
        public Result<Void> validateBaseFields() {
            Result<Void> baseResult = super.validateBaseFields();
            if (baseResult.isFailure()) {
                return baseResult;
            }
            if (loanFacilityId == null) {
                return Result.ofNotification(Notification.ofError(
                        MorabeheDisbursementLocalizedMessageCodes.FIELD_REQUIRED, "loanFacilityId"));
            }
            return Result.ofNotification(Notification.empty());
        }
    }
}
