package ir.dotin.loan.trade.core.domain.loanfacility.strategy.factory;

import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.commons.domain.annotation.DomainComponent;
import ir.dotin.loan.baseloan.core.domain.shared.enums.TransactionCause;
import ir.dotin.loan.baseloan.core.domain.shared.enums.TransactionType;
import ir.dotin.loan.baseloan.core.domain.shared.strategy.ArticleSpec;
import ir.dotin.loan.baseloan.core.domain.shared.strategy.factory.AbstractArticleSpecFactory;
import ir.dotin.loan.baseloan.core.domain.shared.strategy.factory.DebitCreditArticleSpecFactory;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.ArticleComponent;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.metadata.TransactionInfo;
import ir.dotin.loan.trade.core.domain.loanfacility.enums.PaymentAmountArticleType;
import ir.dotin.loan.trade.core.domain.loanfacility.strategy.config.PaymentAmountMetadataConfig;
import ir.dotin.loan.trade.core.domain.loantype.enums.TradeRelationType;

@DomainComponent
public class PaymentAmountArticleSpecFactory
        extends AbstractArticleSpecFactory<PaymentAmountArticleType, TradeRelationType>
        implements DebitCreditArticleSpecFactory<PaymentAmountArticleType, TradeRelationType> {

    private static final TransactionType TRANSACTION_TYPE = TransactionType.CODE_10004;
    private static final TransactionCause TRANSACTION_CAUSE = TransactionCause.LRPA;

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
                .orElseThrow(() -> new IllegalStateException("Failed to create transaction info"));
    }
}
