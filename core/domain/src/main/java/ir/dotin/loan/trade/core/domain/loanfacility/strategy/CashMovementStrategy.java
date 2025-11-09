package ir.dotin.loan.trade.core.domain.loanfacility.strategy;

import ir.dotin.loan.baseloan.core.domain.shared.strategy.DocumentCalculationStrategy;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;
import ir.dotin.loan.trade.core.domain.loanfacility.enums.PaymentAmountArticleType;
import ir.dotin.loan.trade.core.domain.loantype.enums.TradeRelationType;

public interface CashMovementStrategy
        extends DocumentCalculationStrategy<TradeLoanFacility, TradeRelationType, PaymentAmountArticleType> {

    default Class<PaymentAmountArticleType> getArticleTypeClass() {
        return PaymentAmountArticleType.class;
    }
}
