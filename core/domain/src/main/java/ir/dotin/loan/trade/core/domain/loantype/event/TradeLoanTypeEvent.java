package ir.dotin.loan.trade.core.domain.loantype.event;

import org.jspecify.annotations.NonNull;

import ir.dotin.platform.domain.common.event.DomainEvent;
import ir.dotin.loan.trade.core.domain.loantype.vo.TradeLoanTypeId;

public sealed interface TradeLoanTypeEvent<T extends Record & TradeLoanTypeEvent<T, P>, P> extends DomainEvent<T, P>
        permits TradeLoanTypeActivated,
                TradeLoanTypeCreated,
                TradeLoanTypeDeactivated,
                NewTradeLoanTypeVersionPrepared {

    String EVENT_TYPE_PREFIX = "TRADE_LOAN_TYPE_";

    @Override
    @NonNull
    TradeLoanTypeId aggregateId();
}
