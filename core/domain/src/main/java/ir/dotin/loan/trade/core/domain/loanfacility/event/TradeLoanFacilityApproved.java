package ir.dotin.loan.trade.core.domain.loanfacility.event;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.SanctionType;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.SanctionedLoanId;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public record TradeLoanFacilityApproved(
        UUID eventId,
        UUID aggregateId,
        String eventType,
        UUID sanctionedLoanId,
        String sanctionSerial,
        SanctionType sanctionType,
        Instant createdAt)
        implements TradeLoanFacilityEvents<TradeLoanFacilityApproved> {

    public TradeLoanFacilityApproved {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(eventType);
        requireNonNull(sanctionedLoanId);
        requireNonNull(sanctionSerial);
        requireNonNull(sanctionType);
        requireNonNull(createdAt);
    }

    public static TradeLoanFacilityApproved of(
            LoanFacilityId id, SanctionedLoanId sanId, String sanctionSerial, SanctionType sanctionType, Clock clock) {
        return new TradeLoanFacilityApproved(
                randomUUID(),
                id.value(),
                TradeLoanFacilityEventType.APPROVED.getFullType(),
                sanId.value(),
                sanctionSerial,
                sanctionType,
                clock.instant());
    }
}
