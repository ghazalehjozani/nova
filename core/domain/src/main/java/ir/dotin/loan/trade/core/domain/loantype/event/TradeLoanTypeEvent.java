package ir.dotin.loan.trade.core.domain.loantype.event;

import org.jspecify.annotations.NonNull;

import ir.dotin.platform.commons.domain.event.DomainEvent;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTypeId;
import ir.dotin.loan.trade.core.domain.loantype.entity.TradeLoanType;

public sealed interface TradeLoanTypeEvent<T extends Record & TradeLoanTypeEvent<T, P>, P> extends DomainEvent<T, P>
        permits TradeLoanTypeActivated,
                TradeLoanTypeCreated,
                TradeLoanTypeDeactivated,
                NewTradeLoanTypeVersionPrepared {

    String EVENT_TYPE_PREFIX = "TRADE_LOAN_TYPE_";

    @Override
    @NonNull
    LoanTypeId aggregateId();

    @Override
    default Class<TradeLoanType> aggregateType() {
        return TradeLoanType.class;
    }
}
