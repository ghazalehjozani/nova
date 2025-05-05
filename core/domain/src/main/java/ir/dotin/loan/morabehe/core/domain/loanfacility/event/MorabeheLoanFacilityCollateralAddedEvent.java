package ir.dotin.loan.morabehe.core.domain.loanfacility.event;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.CollateralSerial;
import ir.dotin.loan.morabehe.core.domain.loanfacility.vo.MorabeheLoanFacilityId;
import ir.dotin.loan.morabehe.core.domain.loanfacility.vo.MorabeheSanctionedLoanId;

import static java.util.Objects.requireNonNull;

public record MorabeheLoanFacilityCollateralAddedEvent(
        UUID eventId, MorabeheLoanFacilityId aggregateId, Payload payload, Instant createdAt)
        implements MorabeheLoanFacilityEvent<
                MorabeheLoanFacilityCollateralAddedEvent, MorabeheLoanFacilityCollateralAddedEvent.Payload> {

    public record Payload(MorabeheSanctionedLoanId sanctionedLoanId, CollateralSerial collateralSerial) {
        public Payload {
            requireNonNull(sanctionedLoanId);
            requireNonNull(collateralSerial);
        }
    }

    public MorabeheLoanFacilityCollateralAddedEvent {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(payload);
        requireNonNull(createdAt);
    }

    public static MorabeheLoanFacilityCollateralAddedEvent of(
            MorabeheLoanFacilityId id, MorabeheSanctionedLoanId sanId, CollateralSerial collateralSerial, Clock clock) {
        return new MorabeheLoanFacilityCollateralAddedEvent(
                UUID.randomUUID(), id, new Payload(sanId, collateralSerial), Instant.now(clock));
    }

    @Override
    public String eventType() {
        return EVENT_TYPE_PREFIX + "COLLATERAL_ADDED";
    }
}
