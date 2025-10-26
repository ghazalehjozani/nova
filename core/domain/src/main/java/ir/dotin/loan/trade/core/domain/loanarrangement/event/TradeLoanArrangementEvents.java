package ir.dotin.loan.trade.core.domain.loanarrangement.event;

import org.jspecify.annotations.NonNull;

import ir.dotin.platform.commons.domain.event.DomainEvent;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanArrangementId;
import ir.dotin.loan.trade.core.domain.loanarrangement.entity.TradeLoanArrangement;

public sealed interface TradeLoanArrangementEvents<T extends Record & TradeLoanArrangementEvents<T, P>, P>
        extends DomainEvent<T, P>
        permits TradeLoanArrangementActivated,
                TradeLoanArrangementCreated,
                TradeLoanArrangementDeactivated,
                NewTradeLoanArrangementVersionPrepared {

    String EVENT_TYPE_PREFIX = "TRADE_ARRANGEMENT_";

    @Override
    @NonNull
    LoanArrangementId aggregateId();

    @Override
    default Class<TradeLoanArrangement> aggregateType() {
        return TradeLoanArrangement.class;
    }
}
