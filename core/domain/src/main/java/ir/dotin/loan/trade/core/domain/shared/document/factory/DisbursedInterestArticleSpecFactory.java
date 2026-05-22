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
import ir.dotin.loan.trade.core.domain.shared.document.config.DisbursedInterestMetadataConfig;
import ir.dotin.loan.trade.core.domain.shared.document.enums.DisbursedInterestArticleType;

@DomainComponent
public class DisbursedInterestArticleSpecFactory
        extends AbstractArticleSpecFactory<DisbursedInterestArticleType, TradeRelationType>
        implements DebitCreditArticleSpecFactory<DisbursedInterestArticleType, TradeRelationType> {

    private static final TransactionType TRANSACTION_TYPE = TransactionType.CODE_11009;
    private static final TransactionCause TRANSACTION_CAUSE = LoanTransactionCause.IDENTIFY_FUTURE_INTEREST;

    public DisbursedInterestArticleSpecFactory(DisbursedInterestMetadataConfig metadataConfig) {
        super(metadataConfig, createTransactionInfo());
    }

    @Override
    public Result<ArticleSpec<DisbursedInterestArticleType>> createDebitSpec(ArticleComponent component) {
        return createSpec(component, DisbursedInterestArticleType.INTEREST_DEBIT_LEG);
    }

    @Override
    public Result<ArticleSpec<DisbursedInterestArticleType>> createCreditSpec(ArticleComponent component) {
        return createSpec(component, DisbursedInterestArticleType.INTEREST_CREDIT_LEG);
    }

    @Override
    public DisbursedInterestArticleType getDebitArticleType() {
        return DisbursedInterestArticleType.INTEREST_DEBIT_LEG;
    }

    @Override
    public DisbursedInterestArticleType getCreditArticleType() {
        return DisbursedInterestArticleType.INTEREST_CREDIT_LEG;
    }

    private static TransactionInfo createTransactionInfo() {
        return TransactionInfo.of(TRANSACTION_TYPE, TRANSACTION_CAUSE)
                .unwrapOrThrow(c -> new IllegalStateException("Failed to create transaction info"));
    }
}
