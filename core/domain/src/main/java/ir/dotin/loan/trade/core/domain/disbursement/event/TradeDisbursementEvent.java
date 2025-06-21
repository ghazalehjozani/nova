package ir.dotin.loan.trade.core.domain.disbursement.event;

import org.jspecify.annotations.NonNull;

import ir.dotin.platform.domain.common.event.DomainEvent;
import ir.dotin.loan.trade.core.domain.disbursement.vo.TradeDisbursementRecordId;

public sealed interface TradeDisbursementEvent<T extends Record & TradeDisbursementEvent<T, P>, P>
        extends DomainEvent<T, P>
        permits TradeDisbursementCompletedEvent, TradeDisbursementFailedEvent, TradeDisbursementTransactionPostedEvent {

    String EVENT_TYPE_PREFIX = "TRADE_DISBURSEMENT_";

    @Override
    @NonNull
    TradeDisbursementRecordId aggregateId();
}
