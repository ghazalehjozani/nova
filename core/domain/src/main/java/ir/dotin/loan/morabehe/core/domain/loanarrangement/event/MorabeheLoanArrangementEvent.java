package ir.dotin.loan.morabehe.core.domain.loanarrangement.event;

import ir.dotin.platform.domain.common.event.DomainEvent;
import ir.dotin.loan.morabehe.core.domain.loanarrangement.vo.MorabeheLoanArrangementId;

public sealed interface MorabeheLoanArrangementEvent<T extends Record & MorabeheLoanArrangementEvent<T, P>, P>
        extends DomainEvent<T, P>
        permits MorabeheLoanArrangementActivated,
                MorabeheLoanArrangementCreated,
                MorabeheLoanArrangementDeactivated,
                NewMorabeheLoanArrangementVersionPrepared {

    String EVENT_TYPE_PREFIX = "MORABEHE_ARRANGEMENT_";

    @Override
    MorabeheLoanArrangementId aggregateId();
}
