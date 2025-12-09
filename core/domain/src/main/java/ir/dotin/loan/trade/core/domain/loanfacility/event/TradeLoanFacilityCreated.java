package ir.dotin.loan.trade.core.domain.loanfacility.event;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.ApplicationNumber;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public record TradeLoanFacilityCreated(
        UUID eventId, UUID aggregateId, String eventType, String applicationNumber, Instant createdAt)
        implements TradeLoanFacilityEvents<TradeLoanFacilityCreated> {

    public TradeLoanFacilityCreated {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(eventType);
        requireNonNull(applicationNumber);
        requireNonNull(createdAt);
    }

    public static Builder builder(Clock clock) {
        return new Builder(clock);
    }

    public static class Builder {
        private final Clock clock;
        private UUID facilityId;
        private ApplicationNumber applicationNumber;
        private Instant occurredAt;

        public Builder(Clock clock) {
            this.clock = clock;
        }

        public Builder facilityId(UUID facilityId) {
            this.facilityId = facilityId;
            return this;
        }

        public Builder applicationNumber(ApplicationNumber applicationNumber) {
            this.applicationNumber = applicationNumber;
            return this;
        }

        public Builder occurredAt(Instant occurredAt) {
            this.occurredAt = occurredAt;
            return this;
        }

        public TradeLoanFacilityCreated build() {
            return new TradeLoanFacilityCreated(
                    UUID.randomUUID(),
                    facilityId,
                    TradeLoanFacilityEventType.CREATED.getFullType(),
                    applicationNumber.formattedApplicationNumber(),
                    occurredAt != null ? occurredAt : clock.instant());
        }
    }

    public static TradeLoanFacilityCreated of(LoanFacilityId id, ApplicationNumber applicationNumber, Clock clock) {
        return new TradeLoanFacilityCreated(
                randomUUID(),
                id.value(),
                TradeLoanFacilityEventType.CREATED.getFullType(),
                applicationNumber.formattedApplicationNumber(),
                clock.instant());
    }
}
