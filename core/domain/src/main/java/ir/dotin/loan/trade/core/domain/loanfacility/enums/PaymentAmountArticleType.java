package ir.dotin.loan.trade.core.domain.loanfacility.enums;

import org.jspecify.annotations.NonNull;

import ir.dotin.loan.baseloan.core.domain.shared.vo.document.ArticleType;
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
