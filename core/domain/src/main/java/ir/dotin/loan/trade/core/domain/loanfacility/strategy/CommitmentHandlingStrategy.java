package ir.dotin.loan.trade.core.domain.loanfacility.strategy;

import ir.dotin.loan.baseloan.core.domain.shared.strategy.DocumentCalculationStrategy;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;
import ir.dotin.loan.trade.core.domain.loanfacility.enums.DisburseBankCommitmentArticleType;
import ir.dotin.loan.trade.core.domain.loantype.enums.TradeRelationType;

public interface CommitmentHandlingStrategy
        extends DocumentCalculationStrategy<TradeLoanFacility, TradeRelationType, DisburseBankCommitmentArticleType> {

    @Override
    default Class<DisburseBankCommitmentArticleType> getArticleTypeClass() {
        return DisburseBankCommitmentArticleType.class;
    }
}
