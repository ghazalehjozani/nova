package ir.dotin.loan.morabehe.core.domain.loanfacility.event;

import ir.dotin.platform.domain.common.event.DomainEvent;
import ir.dotin.loan.morabehe.core.domain.loanfacility.vo.MorabeheLoanFacilityId;

public sealed interface MorabeheLoanFacilityEvent<T extends Record & MorabeheLoanFacilityEvent<T, P>, P>
        extends DomainEvent<T, P>
        permits MorabeheLoanFacilityCreatedEvent,
                MorabeheLoanFacilityPendingApprovalEvent,
                MorabeheLoanFacilityApprovedEvent,
                MorabeheLoanFacilityRejectedEvent,
                MorabeheLoanFacilityContractIssuedEvent,
                MorabeheLoanFacilityPendingDisbursementEvent,
                MorabeheLoanFacilityActivatedEvent,
                MorabeheLoanFacilityDisbursementFailedEvent,
                MorabeheLoanFacilityClosedPaidOffEvent,
                MorabeheLoanFacilityClosedDefaultedEvent,
                MorabeheLoanFacilityCancelledEvent,
                MorabeheLoanFacilityCollateralAddedEvent {
    String EVENT_TYPE_PREFIX = "MORABEHE_LOAN_FACILITY_";

    @Override
    MorabeheLoanFacilityId aggregateId();
}
