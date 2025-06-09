package ir.dotin.loan.trade.core.domain.disbursement.enums;

import org.jspecify.annotations.NonNull;

import ir.dotin.loan.baseloan.core.domain.shared.vo.document.ArticleType;
import ir.dotin.loan.trade.core.domain.loantype.enums.TradeRelationType;

public enum DisbursedInterestArticleType implements ArticleType<DisbursedInterestArticleType, TradeRelationType> {
    INTEREST_ARTICLE_TOPIC_COMPONENT(TradeRelationType.INTEREST_CALCULATION_CONTEXT),
    INTEREST_DEBIT_LEG(TradeRelationType.PRINCIPAL),
    INTEREST_CREDIT_LEG(TradeRelationType.FUTURE_INTEREST);

    @NonNull
    private final TradeRelationType relationType;

    DisbursedInterestArticleType(@NonNull TradeRelationType relationType) {
        this.relationType = relationType;
    }

    @Override
    public TradeRelationType getRelationType() {
        return relationType;
    }
}
