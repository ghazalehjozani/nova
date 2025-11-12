package ir.dotin.loan.trade.core.domain.loanfacility.strategy;

import ir.dotin.loan.baseloan.core.domain.shared.strategy.DocumentCalculationStrategy;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;
import ir.dotin.loan.trade.core.domain.loanfacility.enums.IssueContractBankCommitmentArticleType;
import ir.dotin.loan.trade.core.domain.loantype.enums.TradeRelationType;

public interface IssueContractCommitmentHandlingStrategy
        extends DocumentCalculationStrategy<
                TradeLoanFacility, TradeRelationType, IssueContractBankCommitmentArticleType> {

    @Override
    default Class<IssueContractBankCommitmentArticleType> getArticleTypeClass() {
        return IssueContractBankCommitmentArticleType.class;
    }
}
