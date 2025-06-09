package ir.dotin.loan.trade.core.domain.loanfacility.strategy.factory;

import ir.dotin.platform.domain.common.Result;
import ir.dotin.platform.domain.common.annotation.DomainService;
import ir.dotin.loan.baseloan.core.domain.shared.enums.TransactionCause;
import ir.dotin.loan.baseloan.core.domain.shared.enums.TransactionType;
import ir.dotin.loan.baseloan.core.domain.shared.strategy.ArticleSpec;
import ir.dotin.loan.baseloan.core.domain.shared.strategy.factory.AbstractArticleSpecFactory;
import ir.dotin.loan.baseloan.core.domain.shared.strategy.factory.DebitCreditArticleSpecFactory;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.ArticleComponent;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.metadata.TransactionInfo;
import ir.dotin.loan.trade.core.domain.loanfacility.enums.BankCommitmentArticleType;
import ir.dotin.loan.trade.core.domain.loanfacility.strategy.config.BankCommitmentMetadataConfig;
import ir.dotin.loan.trade.core.domain.loantype.enums.TradeRelationType;

@DomainService
public class BankCommitmentArticleSpecFactory
        extends AbstractArticleSpecFactory<BankCommitmentArticleType, TradeRelationType>
        implements DebitCreditArticleSpecFactory<BankCommitmentArticleType, TradeRelationType> {

    private static final TransactionType TRANSACTION_TYPE = TransactionType.CODE_11009;
    private static final TransactionCause TRANSACTION_CAUSE = TransactionCause.SET_BANK_COMMITMENT;

    public BankCommitmentArticleSpecFactory(BankCommitmentMetadataConfig metadataConfig) {
        super(metadataConfig, createTransactionInfo());
    }

    public BankCommitmentArticleSpecFactory() {
        this(new BankCommitmentMetadataConfig());
    }

    @Override
    public Result<ArticleSpec<BankCommitmentArticleType>> createDebitSpec(ArticleComponent component) {
        return createSpec(component, BankCommitmentArticleType.BANK_COMMITMENT_DEBIT_LEG);
    }

    @Override
    public Result<ArticleSpec<BankCommitmentArticleType>> createCreditSpec(ArticleComponent component) {
        return createSpec(component, BankCommitmentArticleType.BANK_COMMITMENT_CREDIT_LEG);
    }

    @Override
    public BankCommitmentArticleType getDebitArticleType() {
        return BankCommitmentArticleType.BANK_COMMITMENT_DEBIT_LEG;
    }

    @Override
    public BankCommitmentArticleType getCreditArticleType() {
        return BankCommitmentArticleType.BANK_COMMITMENT_CREDIT_LEG;
    }

    private static TransactionInfo createTransactionInfo() {
        return TransactionInfo.of(TRANSACTION_TYPE, TRANSACTION_CAUSE)
                .orElseThrow(() -> new IllegalStateException("Failed to create transaction info"));
    }
}
