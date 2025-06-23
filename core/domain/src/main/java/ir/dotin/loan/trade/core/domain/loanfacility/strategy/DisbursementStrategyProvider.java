package ir.dotin.loan.trade.core.domain.loanfacility.strategy;

import java.util.List;

import ir.dotin.loan.baseloan.core.domain.shared.strategy.DocumentCalculationStrategy;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.ArticleType;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;
import ir.dotin.loan.trade.core.domain.loantype.enums.TradeRelationType;

public interface DisbursementStrategyProvider {
    List<DocumentCalculationStrategy<TradeLoanFacility, TradeRelationType, ? extends ArticleType<?, TradeRelationType>>>
            getStrategies(TradeLoanFacility facility);
}
