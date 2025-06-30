package ir.dotin.loan.trade.core.domain.loanfacility.event;

import org.jspecify.annotations.NonNull;

import ir.dotin.platform.domain.common.event.DomainEvent;
import ir.dotin.loan.trade.core.domain.loanfacility.vo.TradeLoanFacilityId;

public sealed interface TradeLoanFacilityEvent<T extends Record & TradeLoanFacilityEvent<T, P>, P>
        extends DomainEvent<T, P>
        permits TradeLoanFacilityCreatedEvent,
                TradeLoanFacilityPendingApprovalEvent,
                TradeLoanFacilityApprovedEvent,
                TradeLoanFacilityRejectedEvent,
                TradeLoanFacilityContractIssuedEvent,
                TradeLoanFacilityPendingDisbursementEvent,
                TradeLoanFacilityActivatedEvent,
                TradeLoanFacilityDisbursementFailedEvent,
                TradeLoanFacilityClosedPaidOffEvent,
                TradeLoanFacilityClosedDefaultedEvent,
                TradeLoanFacilityCancelledEvent,
                TradeLoanFacilityCollateralAddedEvent {

    String EVENT_TYPE_PREFIX = "TRADE_LOAN_FACILITY_";

    @Override
    @NonNull
    TradeLoanFacilityId aggregateId();
}
