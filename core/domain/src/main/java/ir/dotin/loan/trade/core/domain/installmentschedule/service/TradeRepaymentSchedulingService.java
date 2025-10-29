package ir.dotin.loan.trade.core.domain.installmentschedule.service;

import java.time.Clock;

import org.jspecify.annotations.NonNull;

import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.commons.domain.annotation.DomainService;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.service.impl.AbstractRepaymentSchedulingService;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.trade.core.domain.installmentschedule.event.InstallmentScheduleEventFactoryImpl;
import ir.dotin.loan.trade.core.domain.installmentschedule.intraction.LoanFacilityProvider;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;
import ir.dotin.loan.trade.core.domain.loanfacility.service.TradeInterestCalculationService;
import ir.dotin.loan.trade.core.domain.shared.formula.TradeLoanFacilityFormulaField;
import ir.dotin.loan.trade.core.domain.shared.formula.TradeLoanParameterProvider;
import ir.dotin.loan.trade.core.domain.shared.formula.TradeLoanParameterProviderImpl;

@DomainService
public class TradeRepaymentSchedulingService
        extends AbstractRepaymentSchedulingService<TradeLoanParameterProvider, TradeLoanFacilityFormulaField> {

    private final LoanFacilityProvider loanFacilityProvider;

    public TradeRepaymentSchedulingService(
            TradeInterestCalculationService interestCalculationService,
            InstallmentScheduleEventFactoryImpl installmentScheduleEventFactory,
            Clock clock,
            LoanFacilityProvider loanFacilityProvider) {
        super(interestCalculationService, installmentScheduleEventFactory, clock);
        this.loanFacilityProvider = loanFacilityProvider;
    }

    @Override
    protected TradeLoanParameterProvider getLoanFacilityParameterProvider(@NonNull LoanFacilityId id) {
        Result<TradeLoanFacility> tradeLoanFacility =
                loanFacilityProvider.findLoanFacilityById(id); // TODO: Handle failure
        return TradeLoanParameterProviderImpl.of(tradeLoanFacility.orElseThrow());
    }
}
