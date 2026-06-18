package ir.dotin.loan.trade.core.domain.shared.formula;

import java.math.BigDecimal;

import ir.dotin.platform.formula.api.binding.FieldBinding;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.formula.LoanArrangementBindings;

public final class TradeLoanArrangementBindings extends LoanArrangementBindings {

    private TradeLoanArrangementBindings() {}

    public static final FieldBinding<TradeLoanArrangementParameterProvider, BigDecimal> TRADE_INTEREST_RATE =
            FieldBinding.of("interestRate", p -> p.getInterestRate().value());

    public static final FieldBinding<TradeLoanArrangementParameterProvider, BigDecimal> TRADE_PENALTY_RATE =
            FieldBinding.of("penaltyRate", p -> p.getPenaltyRate().value());
}
