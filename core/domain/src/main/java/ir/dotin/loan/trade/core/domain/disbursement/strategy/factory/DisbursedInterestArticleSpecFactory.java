package ir.dotin.loan.trade.core.domain.disbursement.strategy.factory;

import ir.dotin.platform.domain.common.Result;
import ir.dotin.platform.domain.common.annotation.DomainComponent;
import ir.dotin.loan.baseloan.core.domain.shared.enums.TransactionCause;
import ir.dotin.loan.baseloan.core.domain.shared.enums.TransactionType;
import ir.dotin.loan.baseloan.core.domain.shared.strategy.ArticleSpec;
import ir.dotin.loan.baseloan.core.domain.shared.strategy.factory.AbstractArticleSpecFactory;
import ir.dotin.loan.baseloan.core.domain.shared.strategy.factory.DebitCreditArticleSpecFactory;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.ArticleComponent;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.metadata.TransactionInfo;
import ir.dotin.loan.trade.core.domain.disbursement.enums.DisbursedInterestArticleType;
import ir.dotin.loan.trade.core.domain.disbursement.strategy.config.DisbursedInterestMetadataConfig;
import ir.dotin.loan.trade.core.domain.loantype.enums.TradeRelationType;

@DomainComponent
public class DisbursedInterestArticleSpecFactory
        extends AbstractArticleSpecFactory<DisbursedInterestArticleType, TradeRelationType>
        implements DebitCreditArticleSpecFactory<DisbursedInterestArticleType, TradeRelationType> {

    private static final TransactionType TRANSACTION_TYPE = TransactionType.CODE_11009;
    private static final TransactionCause TRANSACTION_CAUSE = TransactionCause.IDENTIFY_FUTURE_INTEREST;

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
                .orElseThrow(() -> new IllegalStateException("Failed to create transaction info"));
    }
}
