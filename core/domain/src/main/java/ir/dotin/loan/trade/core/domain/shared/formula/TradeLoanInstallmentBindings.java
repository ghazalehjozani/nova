package ir.dotin.loan.trade.core.domain.shared.formula;

import java.math.BigDecimal;

import ir.dotin.platform.formula.api.binding.FieldBinding;
import ir.dotin.platform.pangaea.commons.domain.vo.Money;

public final class TradeLoanInstallmentBindings {

    private TradeLoanInstallmentBindings() {}

    public static final FieldBinding<TradeLoanInstallmentParameterProvider, Money> TRADE_TOTAL_INTEREST =
            FieldBinding.of("totalInterest", TradeLoanInstallmentParameterProvider::getTotalInterest);

    public static final FieldBinding<TradeLoanInstallmentParameterProvider, Money> TRADE_OUTSTANDING_PRINCIPAL =
            FieldBinding.of("outstandingPrincipal", TradeLoanInstallmentParameterProvider::getOutstandingPrincipal);

    public static final FieldBinding<TradeLoanInstallmentParameterProvider, Money> TRADE_PAID_PRINCIPAL =
            FieldBinding.of("paidPrincipal", TradeLoanInstallmentParameterProvider::getPaidPrincipalSum);

    public static final FieldBinding<TradeLoanInstallmentParameterProvider, Money> TRADE_TOTAL_LOAN_AMOUNT =
            FieldBinding.of("totalLoanAmount", TradeLoanInstallmentParameterProvider::getTotalLoanAmount);

    public static final FieldBinding<TradeLoanInstallmentParameterProvider, BigDecimal>
            TRADE_INSTALLMENT_SCHEDULE_COUNT =
                    FieldBinding.of("installmentScheduleCount", p -> BigDecimal.valueOf(p.getTotalInstallmentCount()));
}
