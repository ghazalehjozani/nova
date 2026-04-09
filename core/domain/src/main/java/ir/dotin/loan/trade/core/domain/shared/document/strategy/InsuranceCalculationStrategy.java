package ir.dotin.loan.trade.core.domain.shared.document.strategy;

import ir.dotin.platform.accounting.document.api.strategy.DocumentCalculationStrategy;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;
import ir.dotin.loan.trade.core.domain.loantype.enums.TradeRelationType;
import ir.dotin.loan.trade.core.domain.shared.document.enums.DisbursedInterestArticleType;

public interface InsuranceCalculationStrategy
        extends DocumentCalculationStrategy<TradeLoanFacility, TradeRelationType, DisbursedInterestArticleType> {

    @Override
    default Class<DisbursedInterestArticleType> getArticleTypeClass() {
        return DisbursedInterestArticleType.class;
    }
}
