package ir.dotin.loan.morabehe.core.domain.disbursement.strategy.impl;

import java.util.ArrayList;
import java.util.List;

import ir.dotin.loan.baseloan.core.domain.shared.vo.transaction.Article;
import ir.dotin.platform.domain.common.Notification;
import ir.dotin.platform.domain.common.Result;
import ir.dotin.platform.domain.common.annotation.DomainService;
import ir.dotin.platform.domain.common.vo.Money;
import ir.dotin.loan.baseloan.core.domain.shared.enums.TransactionCause;
import ir.dotin.loan.baseloan.core.domain.shared.enums.TransactionType;
import ir.dotin.loan.baseloan.core.domain.shared.interaction.FindAccountByRelationTypeClient;
import ir.dotin.loan.baseloan.core.domain.shared.service.transaction.ArticleCommentFactory;
import ir.dotin.loan.baseloan.core.domain.shared.strategy.AbstractDocumentItemStrategy;
import ir.dotin.loan.baseloan.core.domain.shared.strategy.CalculationContext;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTopic;
import ir.dotin.loan.baseloan.core.domain.shared.vo.transaction.PostTitle;
import ir.dotin.loan.baseloan.core.domain.shared.vo.transaction.metadata.TransactionInfo;
import ir.dotin.loan.baseloan.core.domain.shared.vo.transaction.metadata.TransactionMetadata;
import ir.dotin.loan.morabehe.core.domain.loanfacility.aggregate.MorabeheLoanFacility;
import ir.dotin.loan.morabehe.core.domain.disbursement.strategy.CommitmentHandlingStrategy;
import ir.dotin.loan.morabehe.core.domain.loantype.enums.MorabeheRelationType;

@DomainService
public final class BankCommitmentTransactionStrategy extends AbstractDocumentItemStrategy<MorabeheLoanFacility>
        implements CommitmentHandlingStrategy {

    private static final TransactionType TRANSACTION_TYPE = TransactionType.CODE_11009;
    private static final TransactionCause TRANSACTION_CAUSE = TransactionCause.SET_BANK_COMMITMENT;

    public BankCommitmentTransactionStrategy(
            FindAccountByRelationTypeClient findAccountClient, ArticleCommentFactory commentFactory) {
        super(findAccountClient, commentFactory);
    }

    @Override
    public Result<List<Article>> calculateItems(CalculationContext<MorabeheLoanFacility> context) {
        return calculateItemsInternal(context, "Incomplete articles for Bank Commitment");
    }

    @Override
    protected Result<List<Article>> generateItems(CalculationContext<MorabeheLoanFacility> context) {

        Money amount = context.principalAmount();
        LoanTopic topic = context.primaryLoanTopic();
        TransactionMetadata metadata = context.baseMetadata();
        PostTitle postTitle = context.postTitle();
        Notification notification = Notification.empty();
        List<Article> items = new ArrayList<>();

        TransactionInfo.Builder trxInfo = new TransactionInfo.Builder()
                .withTransactionType(TRANSACTION_TYPE)
                .withTransactionCause(TRANSACTION_CAUSE);
        metadata.withTransactionInfo(trxInfo);

        // Debit Item
        MorabeheRelationType debitRelation = MorabeheRelationType.BANK_COMMITMENTS_CONTRA;
        Article debitItem =
                findAndCreateAccountItem(debitRelation, amount, topic, postTitle, metadata, notification);
        if (debitItem != null) {
            items.add(debitItem);
        }

        // Credit Item
        MorabeheRelationType creditRelation = MorabeheRelationType.BANK_COMMITMENTS;
        Article creditItem =
                findAndCreateAccountItem(creditRelation, amount, topic, postTitle, metadata, notification);
        if (creditItem != null) {
            items.add(creditItem);
        }

        return Result.of(items, notification);
    }
}
