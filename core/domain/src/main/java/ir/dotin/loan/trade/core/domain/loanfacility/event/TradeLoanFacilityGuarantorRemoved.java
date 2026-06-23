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
 * Emitted when a single guarantor is removed from a facility. {@code parties} is the full resulting guarantor snapshot
 * (authoritative — FCB diff-applies off it); {@code removedCustomerNumber} is the delta (advisory/audit only).
 */
public record TradeLoanFacilityGuarantorRemoved(
        UUID eventId,
        UUID aggregateId,
        String eventType,
        List<GuarantorPayload> parties,
        String removedCustomerNumber,
        Instant createdAt)
        implements TradeLoanFacilityEvents<TradeLoanFacilityGuarantorRemoved> {

    public TradeLoanFacilityGuarantorRemoved {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(eventType);
        requireNonNull(parties);
        requireNonNull(removedCustomerNumber);
        requireNonNull(createdAt);
    }

    public static TradeLoanFacilityGuarantorRemoved of(
            LoanFacilityId facilityId,
            String removedCustomerNumber,
            List<GuarantorParty> resultingSnapshot,
            Clock clock) {
        return new TradeLoanFacilityGuarantorRemoved(
                randomUUID(),
                facilityId.value(),
                TradeLoanFacilityEventType.GUARANTOR_REMOVED.getFullType(),
                resultingSnapshot.stream().map(GuarantorPayload::from).toList(),
                removedCustomerNumber,
                clock.instant());
    }
}
