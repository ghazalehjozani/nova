package ir.dotin.loan.trade.core.domain.shared.document.enums;

import org.jspecify.annotations.NonNull;

import ir.dotin.platform.accounting.document.api.enumeration.Direction;
import ir.dotin.platform.accounting.document.api.model.ArticleType;
import ir.dotin.loan.trade.core.domain.loantype.enums.TradeRelationType;

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

    @Override
    public Direction getDirection() {
        return relationType.getDirection().reversed();
    }
}
