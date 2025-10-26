package ir.dotin.loan.trade.core.domain.loanfacility.event;

import org.jspecify.annotations.NonNull;

import ir.dotin.platform.commons.domain.event.DomainEvent;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;

public sealed interface TradeLoanFacilityEvents<T extends Record & TradeLoanFacilityEvents<T, P>, P>
        extends DomainEvent<T, P>
        permits TradeLoanFacilityActivated,
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

    String EVENT_TYPE_PREFIX = "TRADE_LOAN_FACILITY_";

    @Override
    @NonNull
    LoanFacilityId aggregateId();

    @Override
    default Class<TradeLoanFacility> aggregateType() {
        return TradeLoanFacility.class;
    }
}
