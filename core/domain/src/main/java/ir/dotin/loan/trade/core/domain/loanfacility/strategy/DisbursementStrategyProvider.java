package ir.dotin.loan.trade.core.domain.loanfacility.strategy;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import ir.dotin.loan.baseloan.core.domain.shared.strategy.DocumentCalculationStrategy;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.ArticleType;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;
import ir.dotin.loan.trade.core.domain.loantype.enums.TradeRelationType;

public interface DisbursementStrategyProvider {

    List<DocumentCalculationStrategy<TradeLoanFacility, TradeRelationType, ? extends ArticleType<?, TradeRelationType>>>
            getStrategies(TradeLoanFacility facility);

    default Set<TradeRelationType> getAllRequiredRelationTypes(TradeLoanFacility facility) {
        return getStrategies(facility).stream()
                .flatMap(strategy -> strategy.getRequiredRelationTypes().stream())
                .map(rt -> (TradeRelationType) rt)
                .collect(Collectors.toSet());
    }
}
