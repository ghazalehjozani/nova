package ir.dotin.loan.trade.core.domain.disbursement.event;

import ir.dotin.platform.domain.common.event.DomainEvent;
import ir.dotin.loan.trade.core.domain.disbursement.vo.TradeDisbursementRecordId;

public sealed interface TradeDisbursementEvent<T extends Record & TradeDisbursementEvent<T, P>, P>
        extends DomainEvent<T, P>
        permits TradeDisbursementCompletedEvent, TradeDisbursementFailedEvent, TradeDisbursementTransactionPostedEvent {

    String EVENT_TYPE_PREFIX = "MORABEHE_DISBURSEMENT_";

    @Override
    TradeDisbursementRecordId aggregateId();
}
