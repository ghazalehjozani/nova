package ir.dotin.loan.trade.core.domain.loanfacility.strategy.factory;

import ir.dotin.platform.domain.common.Result;
import ir.dotin.platform.domain.common.annotation.DomainComponent;
import ir.dotin.loan.baseloan.core.domain.shared.enums.TransactionCause;
import ir.dotin.loan.baseloan.core.domain.shared.enums.TransactionType;
import ir.dotin.loan.baseloan.core.domain.shared.strategy.ArticleSpec;
import ir.dotin.loan.baseloan.core.domain.shared.strategy.factory.AbstractArticleSpecFactory;
import ir.dotin.loan.baseloan.core.domain.shared.strategy.factory.DebitCreditArticleSpecFactory;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.ArticleComponent;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.metadata.TransactionInfo;
import ir.dotin.loan.trade.core.domain.loanfacility.enums.DisburseBankCommitmentArticleType;
import ir.dotin.loan.trade.core.domain.loanfacility.strategy.config.BankCommitmentMetadataConfig;
import ir.dotin.loan.trade.core.domain.loantype.enums.TradeRelationType;

@DomainComponent
public class BankCommitmentArticleSpecFactory
        extends AbstractArticleSpecFactory<DisburseBankCommitmentArticleType, TradeRelationType>
        implements DebitCreditArticleSpecFactory<DisburseBankCommitmentArticleType, TradeRelationType> {

    private static final TransactionType TRANSACTION_TYPE = TransactionType.CODE_11009;
    private static final TransactionCause TRANSACTION_CAUSE = TransactionCause.SET_BANK_COMMITMENT;

    public BankCommitmentArticleSpecFactory(BankCommitmentMetadataConfig metadataConfig) {
        super(metadataConfig, createTransactionInfo());
    }

    @Override
    public Result<ArticleSpec<DisburseBankCommitmentArticleType>> createDebitSpec(ArticleComponent component) {
        return createSpec(component, DisburseBankCommitmentArticleType.BANK_COMMITMENT_DEBIT_LEG);
    }

    @Override
    public Result<ArticleSpec<DisburseBankCommitmentArticleType>> createCreditSpec(ArticleComponent component) {
        return createSpec(component, DisburseBankCommitmentArticleType.BANK_COMMITMENT_CREDIT_LEG);
    }

    @Override
    public DisburseBankCommitmentArticleType getDebitArticleType() {
        return DisburseBankCommitmentArticleType.BANK_COMMITMENT_DEBIT_LEG;
    }

    @Override
    public DisburseBankCommitmentArticleType getCreditArticleType() {
        return DisburseBankCommitmentArticleType.BANK_COMMITMENT_CREDIT_LEG;
    }

    private static TransactionInfo createTransactionInfo() {
        return TransactionInfo.of(TRANSACTION_TYPE, TRANSACTION_CAUSE)
                .orElseThrow(() -> new IllegalStateException("Failed to create transaction info"));
    }
}
