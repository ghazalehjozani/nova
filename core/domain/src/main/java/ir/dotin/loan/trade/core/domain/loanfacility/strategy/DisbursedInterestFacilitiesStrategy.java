package ir.dotin.loan.trade.core.domain.loanfacility.strategy;

import ir.dotin.loan.baseloan.core.domain.shared.strategy.DocumentCalculationStrategy;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;
import ir.dotin.loan.trade.core.domain.loanfacility.enums.DisbursedInterestArticleType;
import ir.dotin.loan.trade.core.domain.loantype.enums.TradeRelationType;

public interface DisbursedInterestFacilitiesStrategy
        extends DocumentCalculationStrategy<TradeLoanFacility, TradeRelationType, DisbursedInterestArticleType> {

    @Override
    default Class<DisbursedInterestArticleType> getArticleTypeClass() {
        return DisbursedInterestArticleType.class;
    }
}
