package ir.dotin.loan.morabehe.core.domain.contractissuance.event;

import ir.dotin.platform.domain.common.event.DomainEvent;
import ir.dotin.loan.morabehe.core.domain.contractissuance.vo.MorabeheContractIssuanceRecordId;

public sealed interface MorabeheContractIssuanceEvent<T extends Record & MorabeheContractIssuanceEvent<T, P>, P>
        extends DomainEvent<T, P>
        permits MorabeheTransactionPostedEvent,
                MorabeheContractIssuanceFailedEvent,
                MorabeheContractIssuancePendingEvent,
                MorabeheContractIssuedEvent {

    String EVENT_TYPE_PREFIX = "MORABEHE_CONTRACT_ISSUANCE_";

    @Override
    MorabeheContractIssuanceRecordId aggregateId();
}
