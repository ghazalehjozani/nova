package ir.dotin.loan.trade.core.domain.disbursement.enums;

import ir.dotin.loan.baseloan.core.domain.shared.vo.document.ArticleType;
import ir.dotin.loan.trade.core.domain.loantype.enums.TradeRelationType;
import org.jspecify.annotations.NonNull;

public enum DisburseBankCommitmentArticleType
        implements ArticleType<DisburseBankCommitmentArticleType, TradeRelationType> {
    BANK_COMMITMENT_DEBIT_LEG(TradeRelationType.BANK_COMMITMENTS),
    BANK_COMMITMENT_CREDIT_LEG(TradeRelationType.BANK_COMMITMENTS_CONTRA);

    @NonNull
    private final TradeRelationType relationType;

    DisburseBankCommitmentArticleType(@NonNull TradeRelationType relationType) {
        this.relationType = relationType;
    }

    @Override
    @NonNull
    public TradeRelationType getRelationType() {
        return relationType;
    }
}
