package ir.dotin.loan.trade.core.domain.shared.document.factory;

import ir.dotin.platform.accounting.document.api.enumeration.TransactionCause;
import ir.dotin.platform.accounting.document.api.enumeration.TransactionType;
import ir.dotin.platform.accounting.document.api.model.ArticleComponent;
import ir.dotin.platform.accounting.document.api.model.metadata.TransactionInfo;
import ir.dotin.platform.accounting.document.api.strategy.ArticleSpec;
import ir.dotin.platform.accounting.document.api.strategy.spec.AbstractArticleSpecFactory;
import ir.dotin.platform.accounting.document.api.strategy.spec.DebitCreditArticleSpecFactory;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.domain.annotation.DomainComponent;
import ir.dotin.loan.baseloan.core.domain.shared.enums.LoanTransactionCause;
import ir.dotin.loan.trade.core.domain.loantype.enums.TradeRelationType;
import ir.dotin.loan.trade.core.domain.shared.document.config.PaymentAmountMetadataConfig;
import ir.dotin.loan.trade.core.domain.shared.document.enums.PaymentAmountArticleType;

@DomainComponent
public class PaymentAmountArticleSpecFactory
        extends AbstractArticleSpecFactory<PaymentAmountArticleType, TradeRelationType>
        implements DebitCreditArticleSpecFactory<PaymentAmountArticleType, TradeRelationType> {

    private static final TransactionType TRANSACTION_TYPE = TransactionType.CODE_10004;
    private static final TransactionCause TRANSACTION_CAUSE = LoanTransactionCause.LRPA;

    public PaymentAmountArticleSpecFactory(PaymentAmountMetadataConfig metadataConfig) {
        super(metadataConfig, createTransactionInfo());
    }

    @Override
    public Result<ArticleSpec<PaymentAmountArticleType>> createDebitSpec(ArticleComponent component) {
        return createSpec(component, PaymentAmountArticleType.PRINCIPAL_DEBIT_LEG);
    }

    @Override
    public Result<ArticleSpec<PaymentAmountArticleType>> createCreditSpec(ArticleComponent component) {
        return createSpec(component, PaymentAmountArticleType.DISBURSEMENT_CREDIT);
    }

    @Override
    public PaymentAmountArticleType getDebitArticleType() {
        return PaymentAmountArticleType.PRINCIPAL_DEBIT_LEG;
    }

    @Override
    public PaymentAmountArticleType getCreditArticleType() {
        return PaymentAmountArticleType.DISBURSEMENT_CREDIT;
    }

    private static TransactionInfo createTransactionInfo() {
        return TransactionInfo.of(TRANSACTION_TYPE, TRANSACTION_CAUSE)
                .unwrapOrThrow(c -> new IllegalStateException("Failed to create transaction info"));
    }
}
