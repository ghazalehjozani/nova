package ir.dotin.loan.trade.core.domain.loanfacility.event;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public record TradeLoanFacilityContractIssuanceReverted(
        UUID eventId, UUID aggregateId, String eventType, Instant createdAt)
        implements TradeLoanFacilityEvents<TradeLoanFacilityContractIssuanceReverted> {

    public TradeLoanFacilityContractIssuanceReverted {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(eventType);
        requireNonNull(createdAt);
    }

    public static TradeLoanFacilityContractIssuanceReverted of(LoanFacilityId id, Clock clock) {
        return new TradeLoanFacilityContractIssuanceReverted(
                randomUUID(),
                id.value(),
                TradeLoanFacilityEventType.CONTRACT_ISSUANCE_REVERTED.getFullType(),
                clock.instant());
    }
}
