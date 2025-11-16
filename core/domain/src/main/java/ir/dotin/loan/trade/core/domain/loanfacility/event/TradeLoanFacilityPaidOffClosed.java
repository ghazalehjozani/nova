package ir.dotin.loan.trade.core.domain.loanfacility.event;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.SanctionedLoanId;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public record TradeLoanFacilityPaidOffClosed(
        UUID eventId, UUID aggregateId, String eventType, UUID sanctionedLoanId, Instant createdAt)
        implements TradeLoanFacilityEvents<TradeLoanFacilityPaidOffClosed> {

    public TradeLoanFacilityPaidOffClosed {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(eventType);
        requireNonNull(sanctionedLoanId);
        requireNonNull(createdAt);
    }

    public static TradeLoanFacilityPaidOffClosed of(LoanFacilityId id, SanctionedLoanId sanId, Clock clock) {
        return new TradeLoanFacilityPaidOffClosed(
                randomUUID(),
                id.value(),
                TradeLoanFacilityEventType.CLOSED_PAID_OFF.getFullType(),
                sanId.value(),
                clock.instant());
    }
}
