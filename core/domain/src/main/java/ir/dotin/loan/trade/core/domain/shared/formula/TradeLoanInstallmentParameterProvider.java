package ir.dotin.loan.trade.core.domain.shared.formula;

import ir.dotin.platform.formula.api.FormulaParameterSource;
import ir.dotin.platform.pangaea.commons.domain.vo.Money;

public interface TradeLoanInstallmentParameterProvider extends FormulaParameterSource {

    Money getTotalInterest();

    Money getOutstandingPrincipal();

    Money getPaidPrincipalSum();

    Money getTotalLoanAmount();

    int getTotalInstallmentCount();
}
