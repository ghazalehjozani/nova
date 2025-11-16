package ir.dotin.loan.trade.core.domain.loanarrangement.event;

import ir.dotin.platform.commons.domain.event.DomainEvent;
import ir.dotin.loan.trade.core.domain.loanarrangement.entity.TradeLoanArrangement;

public sealed interface TradeLoanArrangementEvents<T extends Record & TradeLoanArrangementEvents<T>>
        extends DomainEvent<T>
        permits TradeLoanArrangementActivated,
                TradeLoanArrangementCreated,
                TradeLoanArrangementDeactivated,
                NewTradeLoanArrangementVersionPrepared {

    @Override
    default Class<TradeLoanArrangement> aggregateType() {
        return TradeLoanArrangement.class;
    }
}
