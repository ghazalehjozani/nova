package ir.dotin.loan.trade.core.domain.shared.document.enums;

import org.jspecify.annotations.NonNull;

import ir.dotin.platform.accounting.document.api.model.ArticleType;
import ir.dotin.loan.trade.core.domain.loantype.enums.TradeRelationType;

public enum DisbursedInterestArticleType implements ArticleType<DisbursedInterestArticleType, TradeRelationType> {
    INTEREST_DEBIT_LEG(TradeRelationType.PRINCIPAL),
    INTEREST_CREDIT_LEG(TradeRelationType.FUTURE_INTEREST);

    @NonNull
    private final TradeRelationType relationType;

    DisbursedInterestArticleType(@NonNull TradeRelationType relationType) {
        this.relationType = relationType;
    }

    @Override
    public @NonNull TradeRelationType getRelationType() {
        return relationType;
    }
}
