package ir.dotin.loan.morabehe.core.domain.contractissuance.event;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import ir.dotin.loan.baseloan.core.domain.contractissuance.enums.IssuanceMethod;
import ir.dotin.loan.morabehe.core.domain.contractissuance.vo.MorabeheContractIssuanceRecordId;
import ir.dotin.loan.morabehe.core.domain.loanfacility.vo.MorabeheLoanFacilityId;

public record MorabeheTransactionPostedEvent(
        UUID eventId,
        MorabeheContractIssuanceRecordId aggregateId,
        Instant createdAt,
        MorabeheLoanFacilityId loanFacilityId,
        IssuanceMethod method)
        implements MorabeheContractIssuanceEvent<
        MorabeheTransactionPostedEvent, MorabeheTransactionPostedEvent.Payload> {

    public MorabeheTransactionPostedEvent {
        Objects.requireNonNull(eventId, "eventId cannot be null");
        Objects.requireNonNull(aggregateId, "aggregateId cannot be null");
        Objects.requireNonNull(createdAt, "createdAt cannot be null");
        Objects.requireNonNull(loanFacilityId, "loanFacilityId cannot be null");
        Objects.requireNonNull(method, "method cannot be null");
    }

    @Override
    public Payload payload() {
        return new Payload(loanFacilityId, method);
    }

    public record Payload(MorabeheLoanFacilityId loanFacilityId, IssuanceMethod method) {

        public Payload {
            Objects.requireNonNull(loanFacilityId, "loanFacilityId cannot be null");
            Objects.requireNonNull(method, "method cannot be null");
        }
    }

    @Override
    public String eventType() {
        return EVENT_TYPE_PREFIX + "CONTRACT_ISSUANCE_CREATED";
    }
}
