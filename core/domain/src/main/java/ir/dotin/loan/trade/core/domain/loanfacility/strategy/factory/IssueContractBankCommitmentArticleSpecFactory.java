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
import ir.dotin.loan.trade.core.domain.loanfacility.enums.IssueContractBankCommitmentArticleType;
import ir.dotin.loan.trade.core.domain.loanfacility.strategy.config.IssueContractBankCommitmentMetadataConfig;
import ir.dotin.loan.trade.core.domain.loantype.enums.TradeRelationType;

@DomainService
public class IssueContractBankCommitmentArticleSpecFactory
        extends AbstractArticleSpecFactory<IssueContractBankCommitmentArticleType, TradeRelationType>
        implements DebitCreditArticleSpecFactory<IssueContractBankCommitmentArticleType, TradeRelationType> {

    private static final TransactionType TRANSACTION_TYPE = TransactionType.CODE_11009;
    private static final TransactionCause TRANSACTION_CAUSE = TransactionCause.SET_BANK_COMMITMENT;

    public IssueContractBankCommitmentArticleSpecFactory(IssueContractBankCommitmentMetadataConfig metadataConfig) {
        super(metadataConfig, createTransactionInfo());
    }

    public IssueContractBankCommitmentArticleSpecFactory() {
        this(new IssueContractBankCommitmentMetadataConfig());
    }

    @Override
    public Result<ArticleSpec<IssueContractBankCommitmentArticleType>> createDebitSpec(ArticleComponent component) {
        return createSpec(component, IssueContractBankCommitmentArticleType.BANK_COMMITMENT_DEBIT_LEG);
    }

    @Override
    public Result<ArticleSpec<IssueContractBankCommitmentArticleType>> createCreditSpec(ArticleComponent component) {
        return createSpec(component, IssueContractBankCommitmentArticleType.BANK_COMMITMENT_CREDIT_LEG);
    }

    @Override
    public IssueContractBankCommitmentArticleType getDebitArticleType() {
        return IssueContractBankCommitmentArticleType.BANK_COMMITMENT_DEBIT_LEG;
    }

    @Override
    public IssueContractBankCommitmentArticleType getCreditArticleType() {
        return IssueContractBankCommitmentArticleType.BANK_COMMITMENT_CREDIT_LEG;
    }

    private static TransactionInfo createTransactionInfo() {
        return TransactionInfo.of(TRANSACTION_TYPE, TRANSACTION_CAUSE)
                .orElseThrow(() -> new IllegalStateException("Failed to create transaction info"));
    }
}
