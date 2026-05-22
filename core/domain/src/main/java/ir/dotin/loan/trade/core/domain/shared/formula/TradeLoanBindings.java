package ir.dotin.loan.trade.core.domain.shared.formula;

import java.math.BigDecimal;

import ir.dotin.platform.formula.api.binding.FieldBinding;
import ir.dotin.platform.pangaea.commons.domain.vo.Money;
import ir.dotin.loan.baseloan.core.domain.loanfacility.formula.BaseLoanBindings;

/** Trade loan field bindings. Includes base + trade-specific bindings. */
public final class TradeLoanBindings extends BaseLoanBindings {

    private TradeLoanBindings() {}

    // ═══════════════════════════════════════════════════════════════════════
    // Base Bindings Re-typed for TradeLoanParameterProvider (compile-time safety)
    // ═══════════════════════════════════════════════════════════════════════

    public static final FieldBinding<TradeLoanParameterProvider, Money> TRADE_APPROVED_AMOUNT =
            FieldBinding.of("approvedAmount", TradeLoanParameterProvider::getApprovedAmount);

    public static final FieldBinding<TradeLoanParameterProvider, Money> TRADE_REQUESTED_AMOUNT =
            FieldBinding.of("requestedAmount", TradeLoanParameterProvider::getRequestedAmount);

    public static final FieldBinding<TradeLoanParameterProvider, BigDecimal> TRADE_DURATION_MONTHS =
            FieldBinding.of("durationMonths", p -> BigDecimal.valueOf(p.getDurationMonths()));

    public static final FieldBinding<TradeLoanParameterProvider, BigDecimal> TRADE_INSTALLMENT_COUNT =
            FieldBinding.of("installmentCount", p -> BigDecimal.valueOf(p.getInstallmentCount()));

    public static final FieldBinding<TradeLoanParameterProvider, BigDecimal> TRADE_GRACE_PERIOD =
            FieldBinding.of("gracePeriodMonths", p -> BigDecimal.valueOf(p.getGracePeriodMonths()));

    public static final FieldBinding<TradeLoanParameterProvider, Money> TRADE_TOTAL_DISBURSED =
            FieldBinding.of("totalDisbursed", TradeLoanParameterProvider::getTotalDisbursedAmount);
}
