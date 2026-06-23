package ir.dotin.loan.trade.core.domain.loanfacility.event;

import ir.dotin.platform.pangaea.commons.domain.event.DomainEvent;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;

public sealed interface TradeLoanFacilityEvents<T extends Record & TradeLoanFacilityEvents<T>> extends DomainEvent<T>
        permits TradeLoanFacilityApprovalSubmitted,
                TradeLoanFacilityApproved,
                TradeLoanFacilityCancelled,
                TradeLoanFacilityClosedDefaulted,
                TradeLoanFacilityCollateralAdded,
                TradeLoanFacilityContractIssuanceReverted,
                TradeLoanFacilityContractIssued,
                TradeLoanFacilityCreated,
                TradeLoanFacilityDisbursementFailed,
                TradeLoanFacilityFullyDisbursed,
                TradeLoanFacilityIrregularTrancheDisbursed,
                TradeLoanFacilityLumpSumDisbursed,
                TradeLoanFacilityPaidOffClosed,
                TradeLoanFacilityRejected,
                TradeLoanFacilityOriginationReverted,
                TradeLoanFacilityApprovalSubmissionReverted,
                TradeLoanFacilityApprovalReverted,
                TradeLoanFacilityDisbursementReverted,
                TradeLoanFacilityIrregularTrancheDisbursementReverted,
                TradeLoanFacilityAddCollateralReverted,
                TradeLoanFacilityClosePaidOffReverted,
                TradeLoanFacilityRestructuring,
                TradeLoanFacilityGuarantorAdded,
                TradeLoanFacilityGuarantorRemoved {

    @Override
    default Class<TradeLoanFacility> aggregateType() {
        return TradeLoanFacility.class;
    }
}
