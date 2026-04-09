package ir.dotin.loan.trade.core.domain.shared.document.enums;

import ir.dotin.platform.accounting.document.api.model.ArticleType;
import ir.dotin.loan.trade.core.domain.loantype.enums.TradeRelationType;

public enum IssueContractBankCommitmentArticleType
        implements ArticleType<IssueContractBankCommitmentArticleType, TradeRelationType> {
    BANK_COMMITMENT_DEBIT_LEG(TradeRelationType.BANK_COMMITMENTS_CONTRA),
    BANK_COMMITMENT_CREDIT_LEG(TradeRelationType.BANK_COMMITMENTS);

    private final TradeRelationType relationType;

    IssueContractBankCommitmentArticleType(TradeRelationType relationType) {
        this.relationType = relationType;
    }

    @Override
    public TradeRelationType getRelationType() {
        return relationType;
    }
}
