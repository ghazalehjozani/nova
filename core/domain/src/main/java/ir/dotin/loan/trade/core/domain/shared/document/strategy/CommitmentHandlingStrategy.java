package ir.dotin.loan.trade.core.domain.shared.document.strategy;

import ir.dotin.platform.accounting.document.api.strategy.DocumentCalculationStrategy;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;
import ir.dotin.loan.trade.core.domain.loantype.enums.TradeRelationType;
import ir.dotin.loan.trade.core.domain.shared.document.enums.DisburseBankCommitmentArticleType;

public interface CommitmentHandlingStrategy
        extends DocumentCalculationStrategy<TradeLoanFacility, TradeRelationType, DisburseBankCommitmentArticleType> {

    @Override
    default Class<DisburseBankCommitmentArticleType> getArticleTypeClass() {
        return DisburseBankCommitmentArticleType.class;
    }
}
