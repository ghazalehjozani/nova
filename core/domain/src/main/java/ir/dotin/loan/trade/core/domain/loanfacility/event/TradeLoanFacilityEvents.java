package ir.dotin.loan.trade.core.domain.loanfacility.event;

import org.jspecify.annotations.NonNull;

import ir.dotin.platform.commons.domain.event.DomainEvent;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;

public sealed interface TradeLoanFacilityEvent<T extends Record & TradeLoanFacilityEvent<T, P>, P>
        extends DomainEvent<T, P>
        permits TradeLoanFacilityActivatedEvent,
                TradeLoanFacilityAdditionalDisbursementCompletedEvent,
                TradeLoanFacilityApprovedEvent,
                TradeLoanFacilityCancelledEvent,
                TradeLoanFacilityClosedDefaultedEvent,
        TradeLoanFacilityPaidOffClosedEvent,
                TradeLoanFacilityCollateralAddedEvent,
                TradeLoanFacilityContractIssuedEvent,
                TradeLoanFacilityCreatedEvent,
                TradeLoanFacilityDisbursementFailedEvent,
        TradeLoanFacilityIrregularlyDisbursedEvent,
                TradeLoanFacilityPartiallyDisbursedEvent,
        TradeLoanFacilityApprovalSubmittedEvent,
        TradeLoanFacilityRejectedEvent {

    String EVENT_TYPE_PREFIX = "TRADE_LOAN_FACILITY_";

    @Override
    @NonNull
    LoanFacilityId aggregateId();

    @Override
    default Class<TradeLoanFacility> aggregateType() {
        return TradeLoanFacility.class;
    }
}
