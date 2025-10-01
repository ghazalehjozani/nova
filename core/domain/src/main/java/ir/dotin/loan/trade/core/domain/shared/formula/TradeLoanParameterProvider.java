package ir.dotin.loan.trade.core.domain.shared.formula;

import java.time.Period;

import ir.dotin.platform.commons.domain.vo.Money;
import ir.dotin.loan.baseloan.core.domain.shared.formula.LoanFacilityParameterProvider;

public interface TradeLoanParameterProvider extends LoanFacilityParameterProvider {

    @Override
    Money getApprovedAmount();

    @Override
    Money getRequestedAmount();

    @Override
    Period getGracePeriod();
}
