package ir.dotin.loan.trade.core.domain.loanfacility.event;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.customer.GuarantorParty;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

/**
 * Emitted when one or more guarantors are added to a facility. {@code parties} is the full resulting guarantor snapshot
 * (authoritative — FCB diff-applies off it); {@code added} is the delta (advisory/audit only).
 */
public record TradeLoanFacilityGuarantorAdded(
        UUID eventId,
        UUID aggregateId,
        String eventType,
        List<GuarantorPayload> parties,
        List<GuarantorPayload> added,
        Instant createdAt)
        implements TradeLoanFacilityEvents<TradeLoanFacilityGuarantorAdded> {

    public TradeLoanFacilityGuarantorAdded {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(eventType);
        requireNonNull(parties);
        requireNonNull(added);
        requireNonNull(createdAt);
    }

    public static TradeLoanFacilityGuarantorAdded of(
            LoanFacilityId facilityId,
            List<GuarantorParty> added,
            List<GuarantorParty> resultingSnapshot,
            Clock clock) {
        return new TradeLoanFacilityGuarantorAdded(
                randomUUID(),
                facilityId.value(),
                TradeLoanFacilityEventType.GUARANTOR_ADDED.getFullType(),
                resultingSnapshot.stream().map(GuarantorPayload::from).toList(),
                added.stream().map(GuarantorPayload::from).toList(),
                clock.instant());
    }
}
