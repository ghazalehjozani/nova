package ir.dotin.loan.trade.core.domain.loanfacility.enums;

import org.jspecify.annotations.NonNull;

import ir.dotin.loan.baseloan.core.domain.shared.vo.document.ArticleType;
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
