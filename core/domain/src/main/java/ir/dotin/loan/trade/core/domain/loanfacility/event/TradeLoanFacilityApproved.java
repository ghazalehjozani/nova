package ir.dotin.loan.trade.core.domain.loanfacility.event;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import org.jspecify.annotations.NonNull;

import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.SanctionType;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.SanctionedLoanId;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public record TradeLoanFacilityApproved(UUID eventId, LoanFacilityId aggregateId, Payload payload, Instant createdAt)
        implements TradeLoanFacilityEvents<TradeLoanFacilityApproved, TradeLoanFacilityApproved.Payload> {

    public record Payload(SanctionedLoanId sanctionedLoanId, String sanctionSerial, SanctionType sanctionType) {
        public Payload {
            requireNonNull(sanctionedLoanId);
            requireNonNull(sanctionSerial);
        }
    }

    public TradeLoanFacilityApproved {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(payload);
        requireNonNull(createdAt);
    }

    public static TradeLoanFacilityApproved of(
            LoanFacilityId id, SanctionedLoanId sanId, String sanctionSerial, SanctionType sanctionType, Clock clock) {
        return new TradeLoanFacilityApproved(
                randomUUID(), id, new Payload(sanId, sanctionSerial, sanctionType), clock.instant());
    }

    @Override
    @NonNull
    public String eventType() {
        return EVENT_TYPE_PREFIX + "APPROVED";
    }
}
