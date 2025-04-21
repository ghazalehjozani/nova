package ir.dotin.loan.morabehe.core.domain.contractissuance.aggregate;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import ir.dotin.platform.domain.common.Notification;
import ir.dotin.platform.domain.common.Result;
import ir.dotin.platform.domain.common.event.DomainEvent;
import ir.dotin.loan.baseloan.core.domain.contractissuance.aggregate.AbstractContractIssuanceRecord;
import ir.dotin.loan.baseloan.core.domain.contractissuance.i18n.ContractIssuanceLocalizedMessageCodes;
import ir.dotin.loan.baseloan.core.domain.contractissuance.vo.ContractReference;
import ir.dotin.loan.baseloan.core.domain.shared.vo.FailureReason;
import ir.dotin.loan.morabehe.core.domain.contractissuance.event.MorabeheContractIssuanceCreatedEvent;
import ir.dotin.loan.morabehe.core.domain.contractissuance.event.MorabeheContractIssuanceFailedEvent;
import ir.dotin.loan.morabehe.core.domain.contractissuance.event.MorabeheContractIssuancePendingEvent;
import ir.dotin.loan.morabehe.core.domain.contractissuance.event.MorabeheContractIssuedEvent;
import ir.dotin.loan.morabehe.core.domain.contractissuance.vo.MorabeheContractIssuanceRecordId;
import ir.dotin.loan.morabehe.core.domain.loanfacility.vo.MorabeheLoanFacilityId;

import static java.util.Objects.requireNonNull;

public final class MorabeheContractIssuanceRecord
        extends AbstractContractIssuanceRecord<MorabeheContractIssuanceRecordId> {

    private final MorabeheLoanFacilityId loanFacilityId;

    private MorabeheContractIssuanceRecord(Builder builder) {
        super(builder);
        this.loanFacilityId = requireNonNull(builder.loanFacilityId);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static Result<MorabeheContractIssuanceRecord> create(Builder builder, Clock clock) {
        var record = builder.withId(MorabeheContractIssuanceRecordId.generate()).build();

        record.registerEvent(new MorabeheContractIssuanceCreatedEvent(
                UUID.randomUUID(), record.getId(), Instant.now(clock), record.getLoanFacilityId(), record.getMethod()));

        return Result.ofValue(record);
    }

    @Override
    public MorabeheLoanFacilityId getLoanFacilityId() {
        return loanFacilityId;
    }

    @Override
    protected DomainEvent<?, ?> getGenerationPendingEvent(MorabeheContractIssuanceRecordId id, Clock clock) {
        return new MorabeheContractIssuancePendingEvent(UUID.randomUUID(), id, clock.instant());
    }

    @Override
    protected DomainEvent<?, ?> getContractIssuedEvent(
            MorabeheContractIssuanceRecordId id, ContractReference reference, Clock clock) {
        return new MorabeheContractIssuedEvent(UUID.randomUUID(), id, reference, clock.instant());
    }

    @Override
    protected DomainEvent<?, ?> getContractIssuanceFailedEvent(
            MorabeheContractIssuanceRecordId id, FailureReason reason, Clock clock) {
        return new MorabeheContractIssuanceFailedEvent(UUID.randomUUID(), id, reason, clock.instant());
    }

    public static final class Builder
            extends AbstractBuilder<MorabeheContractIssuanceRecordId, MorabeheContractIssuanceRecord, Builder> {

        private MorabeheLoanFacilityId loanFacilityId;

        public Builder() {
            super();
        }

        public Builder(MorabeheContractIssuanceRecord other) {
            super(other);
            this.loanFacilityId = other.loanFacilityId;
        }

        public Builder withMethod(MorabeheLoanFacilityId loanFacilityId) {
            this.loanFacilityId = loanFacilityId;
            return this;
        }

        @Override
        protected Builder self() {
            return this;
        }

        @Override
        protected MorabeheContractIssuanceRecord buildInternal() {
            return new MorabeheContractIssuanceRecord(this);
        }

        @Override
        public Result<Void> validateBaseFields() {
            Result<Void> baseResult = super.validateBaseFields();
            if (baseResult.isFailure()) {
                return baseResult;
            }
            if (loanFacilityId == null) {
                return Result.ofNotification(
                        Notification.ofError(ContractIssuanceLocalizedMessageCodes.FIELD_REQUIRED, "loanFacilityId"));
            }
            return Result.ofNotification(Notification.empty());
        }
    }
}
