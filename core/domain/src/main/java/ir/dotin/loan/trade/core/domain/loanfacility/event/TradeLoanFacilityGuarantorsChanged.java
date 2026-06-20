package ir.dotin.loan.trade.core.domain.loanfacility.event;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.customer.CustomerName;
import ir.dotin.loan.baseloan.core.domain.shared.vo.customer.GuarantorParty;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public record TradeLoanFacilityGuarantorsChanged(
        UUID eventId, UUID aggregateId, String eventType, List<GuarantorPayload> parties, Instant createdAt)
        implements TradeLoanFacilityEvents<TradeLoanFacilityGuarantorsChanged> {

    public TradeLoanFacilityGuarantorsChanged {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(eventType);
        requireNonNull(parties);
        requireNonNull(createdAt);
    }

    public static TradeLoanFacilityGuarantorsChanged of(
            LoanFacilityId facilityId, List<GuarantorParty> guarantors, Clock clock) {
        List<GuarantorPayload> parties =
                guarantors.stream().map(GuarantorPayload::from).toList();
        return new TradeLoanFacilityGuarantorsChanged(
                randomUUID(),
                facilityId.value(),
                TradeLoanFacilityEventType.GUARANTORS_CHANGED.getFullType(),
                parties,
                clock.instant());
    }

    public record GuarantorPayload(
            String customerNumber, String partyType, String customerName, String guaranteePercent) {

        public static GuarantorPayload from(GuarantorParty party) {
            CustomerName name = party.name();
            return new GuarantorPayload(
                    party.customerNumber(),
                    party.partyType().name(),
                    name.fullName(),
                    party.guaranteePercentage().value().toPlainString());
        }
    }
}
