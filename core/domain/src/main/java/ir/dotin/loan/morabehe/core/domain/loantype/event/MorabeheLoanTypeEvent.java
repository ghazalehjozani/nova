package ir.dotin.loan.morabehe.core.domain.loantype.event;

import ir.dotin.platform.domain.common.event.DomainEvent;
import ir.dotin.loan.morabehe.core.domain.loantype.vo.MorabeheLoanTypeId;

public sealed interface MorabeheLoanTypeEvent<T extends Record & MorabeheLoanTypeEvent<T, P>, P>
        extends DomainEvent<T, P>
        permits MorabeheLoanTypeActivated,
                MorabeheLoanTypeCreated,
                MorabeheLoanTypeDeactivated,
                NewMorabeheLoanTypeVersionPrepared {

    String EVENT_TYPE_PREFIX = "MORABEHE_LOAN_TYPE_";

    @Override
    MorabeheLoanTypeId aggregateId();
}
