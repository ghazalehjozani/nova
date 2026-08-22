package ir.dotin.loan.trade.core.domain.installmentschedule.service;

import java.time.Clock;

import ir.dotin.platform.pangaea.commons.domain.annotation.DomainService;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.service.impl.AbstractRepaymentSchedulingService;
import ir.dotin.loan.trade.core.domain.installmentschedule.event.InstallmentScheduleEventFactoryImpl;

@DomainService
public class TradeRepaymentSchedulingService extends AbstractRepaymentSchedulingService {

    // why: previously also took a LoanFacilityProvider and silently discarded it — never passed to super,
    // never stored. The only thing that would consume it is the deferred InterestPolicy.interestFormula
    // wiring, so the parameter is dropped rather than kept as dead flexibility; LoanFacilityProvider and
    // its LoanFacilityProviderService implementation stay for that change to pick up.
    public TradeRepaymentSchedulingService(
            InstallmentScheduleEventFactoryImpl installmentScheduleEventFactory, Clock clock) {
        super(installmentScheduleEventFactory, clock);
    }
}
