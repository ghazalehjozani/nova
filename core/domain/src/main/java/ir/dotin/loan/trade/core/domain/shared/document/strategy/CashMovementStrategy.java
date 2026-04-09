package ir.dotin.loan.trade.core.domain.shared.document.strategy;

import ir.dotin.platform.accounting.document.api.strategy.DocumentCalculationStrategy;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;
import ir.dotin.loan.trade.core.domain.loantype.enums.TradeRelationType;
import ir.dotin.loan.trade.core.domain.shared.document.enums.PaymentAmountArticleType;

public interface CashMovementStrategy
        extends DocumentCalculationStrategy<TradeLoanFacility, TradeRelationType, PaymentAmountArticleType> {

    @Override
    default Class<PaymentAmountArticleType> getArticleTypeClass() {
        return PaymentAmountArticleType.class;
    }
}
