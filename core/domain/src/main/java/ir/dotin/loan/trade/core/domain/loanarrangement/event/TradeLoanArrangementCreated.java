package ir.dotin.loan.trade.core.domain.loanarrangement.event;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import ir.dotin.loan.baseloan.core.domain.loanarrangement.vo.LoanArrangementCode;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanArrangementId;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public record TradeLoanArrangementCreated(
        UUID eventId, UUID aggregateId, String eventType, Instant createdAt, String code)
        implements TradeLoanArrangementEvents<TradeLoanArrangementCreated> {

    public TradeLoanArrangementCreated {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(eventType);
        requireNonNull(createdAt);
    }

    public static TradeLoanArrangementCreated of(
            LoanArrangementId loanArrangementId, Clock clock, LoanArrangementCode code) {
        return new TradeLoanArrangementCreated(
                randomUUID(),
                loanArrangementId.value(),
                TradeLoanArrangementEventType.CREATED.getFullType(),
                clock.instant(),
                code.value());
    }
}
