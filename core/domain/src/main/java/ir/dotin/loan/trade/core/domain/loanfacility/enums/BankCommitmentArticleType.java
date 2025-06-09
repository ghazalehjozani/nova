package ir.dotin.loan.trade.core.domain.loanfacility.enums;

import ir.dotin.loan.baseloan.core.domain.shared.vo.document.ArticleType;
import ir.dotin.loan.trade.core.domain.loantype.enums.TradeRelationType;

public enum BankCommitmentArticleType implements ArticleType<BankCommitmentArticleType, TradeRelationType> {
    BANK_COMMITMENT_DEBIT_LEG(TradeRelationType.BANK_COMMITMENTS_CONTRA),
    BANK_COMMITMENT_CREDIT_LEG(TradeRelationType.BANK_COMMITMENTS);

    private final TradeRelationType relationType;

    BankCommitmentArticleType(TradeRelationType relationType) {
        this.relationType = relationType;
    }

    @Override
    public TradeRelationType getRelationType() {
        return relationType;
    }
}
