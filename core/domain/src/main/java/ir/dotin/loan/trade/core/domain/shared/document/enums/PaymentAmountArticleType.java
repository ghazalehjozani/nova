package ir.dotin.loan.trade.core.domain.shared.document.enums;

import org.jspecify.annotations.NonNull;

import ir.dotin.platform.accounting.document.api.model.ArticleType;
import ir.dotin.loan.trade.core.domain.loantype.enums.TradeRelationType;

public enum PaymentAmountArticleType implements ArticleType<PaymentAmountArticleType, TradeRelationType> {
    PRINCIPAL_DEBIT_LEG(TradeRelationType.PRINCIPAL),
    DISBURSEMENT_CREDIT(TradeRelationType.DISBURSEMENT_TRANSACTION_CONTEXT);

    @NonNull
    private final TradeRelationType relationType;

    PaymentAmountArticleType(@NonNull TradeRelationType relationType) {
        this.relationType = relationType;
    }

    @Override
    @NonNull
    public TradeRelationType getRelationType() {
        return relationType;
    }
}
