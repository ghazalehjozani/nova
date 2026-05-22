package ir.dotin.loan.trade.core.domain.installmentschedule.service;

import java.time.Clock;

import ir.dotin.platform.pangaea.commons.domain.annotation.DomainService;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.service.impl.AbstractRepaymentSchedulingService;
import ir.dotin.loan.trade.core.domain.installmentschedule.event.InstallmentScheduleEventFactoryImpl;
import ir.dotin.loan.trade.core.domain.installmentschedule.intraction.LoanFacilityProvider;

@DomainService
public class TradeRepaymentSchedulingService extends AbstractRepaymentSchedulingService {

    public TradeRepaymentSchedulingService(
            InstallmentScheduleEventFactoryImpl installmentScheduleEventFactory,
            Clock clock,
            LoanFacilityProvider loanFacilityProvider) {
        super(installmentScheduleEventFactory, clock);
    }
}
