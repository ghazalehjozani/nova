package ir.dotin.loan.trade.core.domain.loanfacility.event;

import ir.dotin.platform.commons.domain.event.DomainEvent;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;

public sealed interface TradeLoanFacilityEvents<T extends Record & TradeLoanFacilityEvents<T, P>, P>
        extends DomainEvent<T, P>
        permits TradeLoanFacilityLumpSumDisbursed,
                TradeLoanFacilityAdditionalDisbursementCompleted,
                TradeLoanFacilityApproved,
                TradeLoanFacilityCancelled,
                TradeLoanFacilityClosedDefaulted,
                TradeLoanFacilityPaidOffClosed,
                TradeLoanFacilityCollateralAdded,
                TradeLoanFacilityContractIssued,
                TradeLoanFacilityCreated,
                TradeLoanFacilityDisbursementFailed,
                TradeLoanFacilityIrregularlyDisbursed,
                TradeLoanFacilityPartiallyDisbursed,
                TradeLoanFacilityApprovalSubmitted,
                TradeLoanFacilityRejected {

    @Override
    default Class<TradeLoanFacility> aggregateType() {
        return TradeLoanFacility.class;
    }
}
