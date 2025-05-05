package ir.dotin.loan.morabehe.core.domain.disbursement.event;

import ir.dotin.loan.morabehe.core.domain.disbursement.vo.MorabeheDisbursementRecordId;
import ir.dotin.platform.domain.common.event.DomainEvent;

public sealed interface MorabeheDisbursementEvent<T extends Record & MorabeheDisbursementEvent<T, P>, P>
        extends DomainEvent<T, P>
        permits MorabeheDisbursementCompletedEvent,
                MorabeheDisbursementFailedEvent,
                MorabeheDisbursementTransactionPostedEvent {

    String EVENT_TYPE_PREFIX = "MORABEHE_DISBURSEMENT_";

    @Override
    MorabeheDisbursementRecordId aggregateId();
}
