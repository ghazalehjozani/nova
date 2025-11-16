package ir.dotin.loan.trade.core.domain.loantype.event;

import ir.dotin.platform.commons.domain.event.DomainEvent;
import ir.dotin.loan.trade.core.domain.loantype.entity.TradeLoanType;

public sealed interface TradeLoanTypeEvents<T extends Record & TradeLoanTypeEvents<T>> extends DomainEvent<T>
        permits TradeLoanTypeActivated,
                TradeLoanTypeCreated,
                TradeLoanTypeDeactivated,
                NewTradeLoanTypeVersionPrepared {

    @Override
    default Class<TradeLoanType> aggregateType() {
        return TradeLoanType.class;
    }
}
