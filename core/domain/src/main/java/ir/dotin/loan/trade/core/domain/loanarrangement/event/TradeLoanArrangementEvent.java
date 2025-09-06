package ir.dotin.loan.trade.core.domain.loanarrangement.event;

import org.jspecify.annotations.NonNull;

import ir.dotin.platform.domain.common.event.DomainEvent;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanArrangementId;

public sealed interface TradeLoanArrangementEvent<T extends Record & TradeLoanArrangementEvent<T, P>, P>
        extends DomainEvent<T, P>
        permits TradeLoanArrangementActivated,
                TradeLoanArrangementCreated,
                TradeLoanArrangementDeactivated,
                NewTradeLoanArrangementVersionPrepared {

    String EVENT_TYPE_PREFIX = "TRADE_ARRANGEMENT_";

    @Override
    @NonNull
    LoanArrangementId aggregateId();
}
