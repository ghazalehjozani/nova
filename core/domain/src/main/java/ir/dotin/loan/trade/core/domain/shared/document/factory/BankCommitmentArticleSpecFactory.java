package ir.dotin.loan.trade.core.domain.shared.document.factory;

import ir.dotin.platform.accounting.document.api.enumeration.TransactionCause;
import ir.dotin.platform.accounting.document.api.enumeration.TransactionType;
import ir.dotin.platform.accounting.document.api.model.ArticleComponent;
import ir.dotin.platform.accounting.document.api.model.metadata.TransactionInfo;
import ir.dotin.platform.accounting.document.api.strategy.ArticleSpec;
import ir.dotin.platform.accounting.document.api.strategy.spec.AbstractArticleSpecFactory;
import ir.dotin.platform.accounting.document.api.strategy.spec.DebitCreditArticleSpecFactory;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.commons.domain.annotation.DomainComponent;
import ir.dotin.loan.baseloan.core.domain.shared.enums.LoanTransactionCause;
import ir.dotin.loan.trade.core.domain.loantype.enums.TradeRelationType;
import ir.dotin.loan.trade.core.domain.shared.document.config.BankCommitmentMetadataConfig;
import ir.dotin.loan.trade.core.domain.shared.document.enums.DisburseBankCommitmentArticleType;

@DomainComponent
public class BankCommitmentArticleSpecFactory
        extends AbstractArticleSpecFactory<DisburseBankCommitmentArticleType, TradeRelationType>
        implements DebitCreditArticleSpecFactory<DisburseBankCommitmentArticleType, TradeRelationType> {

    private static final TransactionType TRANSACTION_TYPE = TransactionType.CODE_11009;
    private static final TransactionCause TRANSACTION_CAUSE = LoanTransactionCause.SET_BANK_COMMITMENT;

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
