package ir.dotin.loan.trade.core.domain.shared.document.factory;

import ir.dotin.platform.accounting.document.api.enumeration.TransactionCause;
import ir.dotin.platform.accounting.document.api.enumeration.TransactionType;
import ir.dotin.platform.accounting.document.api.model.ArticleComponent;
import ir.dotin.platform.accounting.document.api.model.metadata.TransactionInfo;
import ir.dotin.platform.accounting.document.api.strategy.ArticleSpec;
import ir.dotin.platform.accounting.document.api.strategy.spec.AbstractArticleSpecFactory;
import ir.dotin.platform.accounting.document.api.strategy.spec.DebitCreditArticleSpecFactory;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.domain.annotation.DomainService;
import ir.dotin.loan.baseloan.core.domain.shared.enums.LoanTransactionCause;
import ir.dotin.loan.trade.core.domain.loantype.enums.TradeRelationType;
import ir.dotin.loan.trade.core.domain.shared.document.config.IssueContractBankCommitmentMetadataConfig;
import ir.dotin.loan.trade.core.domain.shared.document.enums.IssueContractBankCommitmentArticleType;

@DomainService
public class IssueContractBankCommitmentArticleSpecFactory
        extends AbstractArticleSpecFactory<IssueContractBankCommitmentArticleType, TradeRelationType>
        implements DebitCreditArticleSpecFactory<IssueContractBankCommitmentArticleType, TradeRelationType> {

    private static final TransactionType TRANSACTION_TYPE = TransactionType.CODE_11009;
    private static final TransactionCause TRANSACTION_CAUSE = LoanTransactionCause.SET_BANK_COMMITMENT;

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
                .unwrapOrThrow(c -> new IllegalStateException("Failed to create transaction info"));
    }
}
