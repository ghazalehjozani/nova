package ir.dotin.loan.trade.core.domain.contractissuance.event;

import ir.dotin.platform.domain.common.event.DomainEvent;
import ir.dotin.loan.trade.core.domain.contractissuance.vo.TradeContractIssuanceRecordId;

public sealed interface TradeContractIssuanceEvent<T extends Record & TradeContractIssuanceEvent<T, P>, P>
        extends DomainEvent<T, P>
        permits TradeContractIssuanceTransactionPostedEvent,
                TradeContractIssuanceFailedEvent,
                TradeContractIssuedEvent {

    String EVENT_TYPE_PREFIX = "MORABEHE_CONTRACT_ISSUANCE_";

    @Override
    TradeContractIssuanceRecordId aggregateId();
}
