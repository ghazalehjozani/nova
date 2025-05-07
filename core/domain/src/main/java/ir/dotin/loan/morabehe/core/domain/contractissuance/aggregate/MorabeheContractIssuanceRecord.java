package ir.dotin.loan.morabehe.core.domain.contractissuance.aggregate;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import ir.dotin.platform.domain.common.Notification;
import ir.dotin.platform.domain.common.Result;
import ir.dotin.platform.domain.common.event.DomainEvent;
import ir.dotin.loan.baseloan.core.domain.contractissuance.aggregate.AbstractContractIssuanceRecord;
import ir.dotin.loan.baseloan.core.domain.contractissuance.i18n.ContractIssuanceLocalizedMessageCodes;
import ir.dotin.loan.baseloan.core.domain.contractissuance.vo.ContractReference;
import ir.dotin.loan.baseloan.core.domain.shared.vo.FailureReason;
import ir.dotin.loan.morabehe.core.domain.contractissuance.event.MorabeheContractIssuanceFailedEvent;
import ir.dotin.loan.morabehe.core.domain.contractissuance.event.MorabeheContractIssuanceTransactionPostedEvent;
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

    public static Result<MorabeheContractIssuanceRecord> create(Builder builder) {
        Objects.requireNonNull(builder, "Builder cannot be null for create.");
        return builder.withId(MorabeheContractIssuanceRecordId.generate()).build();
    }

    public static MorabeheContractIssuanceRecord reconstitute(Builder builder) {
        Objects.requireNonNull(builder, "Builder cannot be null for reconstitution.");
        return builder.buildInternal();
    }

    @Override
    public MorabeheLoanFacilityId getLoanFacilityId() {
        return loanFacilityId;
    }

    @Override
    protected DomainEvent<?, ?> getTransactionPostedEvent(MorabeheContractIssuanceRecordId id, Clock clock) {
        return MorabeheContractIssuanceTransactionPostedEvent.create(
                UUID.randomUUID(), getId(), Instant.now(clock), getLoanFacilityId());
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
        protected MorabeheContractIssuanceRecord buildInternal() {
            return new MorabeheContractIssuanceRecord(this);
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
