package ir.dotin.loan.trade.core.domain.shared.document.strategy;

import ir.dotin.platform.accounting.document.api.strategy.DocumentCalculationStrategy;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;
import ir.dotin.loan.trade.core.domain.loantype.enums.TradeRelationType;
import ir.dotin.loan.trade.core.domain.shared.document.enums.IssueContractBankCommitmentArticleType;

public interface IssueContractCommitmentHandlingStrategy
        extends DocumentCalculationStrategy<
                TradeLoanFacility, TradeRelationType, IssueContractBankCommitmentArticleType> {

    @Override
    default Class<IssueContractBankCommitmentArticleType> getArticleTypeClass() {
        return IssueContractBankCommitmentArticleType.class;
    }
}
