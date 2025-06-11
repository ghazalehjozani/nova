package ir.dotin.loan.trade.core.domain.disbursement.strategy;

import ir.dotin.loan.baseloan.core.domain.shared.strategy.DocumentCalculationStrategy;
import ir.dotin.loan.trade.core.domain.disbursement.enums.DisbursedInterestArticleType;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;
import ir.dotin.loan.trade.core.domain.loantype.enums.TradeRelationType;

public interface AddedValueCalculationStrategy
        extends DocumentCalculationStrategy<TradeLoanFacility, TradeRelationType, DisbursedInterestArticleType> {}
