package ir.dotin.loan.trade.core.domain.disbursement.strategy;

import java.util.List;

import ir.dotin.loan.baseloan.core.domain.shared.strategy.DocumentCalculationStrategy;
import ir.dotin.loan.trade.core.domain.loanfacility.aggregate.TradeLoanFacility;

public interface DisbursementStrategyProvider {
    List<DocumentCalculationStrategy<TradeLoanFacility>> getStrategies(TradeLoanFacility facility);
}
