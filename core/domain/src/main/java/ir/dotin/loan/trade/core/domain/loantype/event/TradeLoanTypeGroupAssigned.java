package ir.dotin.loan.trade.core.domain.loantype.event;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTypeGroupId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTypeId;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public record TradeLoanTypeGroupAssigned(
        UUID eventId, UUID aggregateId, String eventType, UUID groupId, Instant createdAt)
        implements TradeLoanTypeEvents<TradeLoanTypeGroupAssigned> {

    public TradeLoanTypeGroupAssigned {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(eventType);
        requireNonNull(groupId);
        requireNonNull(createdAt);
    }

    public static TradeLoanTypeGroupAssigned of(LoanTypeId loanTypeId, LoanTypeGroupId groupId, Clock clock) {
        return new TradeLoanTypeGroupAssigned(
                randomUUID(),
                loanTypeId.value(),
                TradeLoanTypeEventType.GROUP_ASSIGNED.getFullType(),
                requireNonNull(groupId.value()),
                clock.instant());
    }
}
