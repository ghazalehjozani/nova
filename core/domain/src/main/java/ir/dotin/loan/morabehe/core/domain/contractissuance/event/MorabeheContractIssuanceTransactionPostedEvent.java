package ir.dotin.loan.morabehe.core.domain.contractissuance.event;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import ir.dotin.loan.morabehe.core.domain.contractissuance.vo.MorabeheContractIssuanceRecordId;
import ir.dotin.loan.morabehe.core.domain.loanfacility.vo.MorabeheLoanFacilityId;

public record MorabeheContractIssuanceTransactionPostedEvent(
        UUID eventId, MorabeheContractIssuanceRecordId aggregateId, Instant createdAt, Payload payload)
        implements MorabeheContractIssuanceEvent<
                MorabeheContractIssuanceTransactionPostedEvent,
                MorabeheContractIssuanceTransactionPostedEvent.Payload> {

    public MorabeheContractIssuanceTransactionPostedEvent {
        Objects.requireNonNull(eventId, "eventId cannot be null");
        Objects.requireNonNull(aggregateId, "aggregateId cannot be null");
        Objects.requireNonNull(createdAt, "createdAt cannot be null");
    }

    public record Payload(MorabeheLoanFacilityId loanFacilityId) {

        public Payload {
            Objects.requireNonNull(loanFacilityId, "loanFacilityId cannot be null");
        }
    }

    @Override
    public String eventType() {
        return EVENT_TYPE_PREFIX + "CONTRACT_ISSUANCE_CREATED";
    }

    public static MorabeheContractIssuanceTransactionPostedEvent create(
            UUID eventId,
            MorabeheContractIssuanceRecordId aggregateId,
            Instant createdAt,
            MorabeheLoanFacilityId loanFacilityId) {
        return new MorabeheContractIssuanceTransactionPostedEvent(
                eventId,
                aggregateId,
                createdAt,
                new MorabeheContractIssuanceTransactionPostedEvent.Payload(loanFacilityId));
    }
}
