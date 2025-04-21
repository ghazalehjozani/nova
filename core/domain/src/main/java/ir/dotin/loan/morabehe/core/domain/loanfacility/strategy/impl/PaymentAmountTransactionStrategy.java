package ir.dotin.loan.morabehe.core.domain.loanfacility.strategy.impl;

import java.util.ArrayList;
import java.util.List;

import ir.dotin.platform.domain.common.Notification;
import ir.dotin.platform.domain.common.Result;
import ir.dotin.platform.domain.common.annotation.DomainService;
import ir.dotin.platform.domain.common.vo.Money;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.DisburseDestination;
import ir.dotin.loan.baseloan.core.domain.shared.enums.transaction.Direction;
import ir.dotin.loan.baseloan.core.domain.shared.interaction.FindAccountByRelationTypeClient;
import ir.dotin.loan.baseloan.core.domain.shared.service.transaction.DocumentItemCommentFactory;
import ir.dotin.loan.baseloan.core.domain.shared.strategy.AbstractDocumentItemStrategy;
import ir.dotin.loan.baseloan.core.domain.shared.strategy.CalculationContext;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTopic;
import ir.dotin.loan.baseloan.core.domain.shared.vo.transaction.DocumentItem;
import ir.dotin.loan.baseloan.core.domain.shared.vo.transaction.PostTitle;
import ir.dotin.loan.baseloan.core.domain.shared.vo.transaction.metadata.TransactionMetadata;
import ir.dotin.loan.morabehe.core.domain.loanfacility.aggregate.MorabeheLoanFacility;
import ir.dotin.loan.morabehe.core.domain.loanfacility.strategy.CashMovementStrategy;
import ir.dotin.loan.morabehe.core.domain.loantype.enums.MorabeheRelationType;

@DomainService
public final class PaymentAmountTransactionStrategy extends AbstractDocumentItemStrategy<MorabeheLoanFacility>
        implements CashMovementStrategy {

    public PaymentAmountTransactionStrategy(
            FindAccountByRelationTypeClient findAccountClient, DocumentItemCommentFactory commentFactory) {
        super(findAccountClient, commentFactory);
    }

    @Override
    public Result<List<DocumentItem>> calculateItems(CalculationContext<MorabeheLoanFacility> context) {
        return calculateItemsInternal(context, "Incomplete items for Payment Amount");
    }

    @Override
    protected Result<List<DocumentItem>> generateItems(CalculationContext<MorabeheLoanFacility> context) {

        Money amount = context.principalAmount();
        //noinspection DuplicatedCode
        LoanTopic topic = context.primaryLoanTopic();
        PostTitle postTitle = context.postTitle();
        TransactionMetadata metadata = context.baseMetadata();
        Notification notification = Notification.empty();
        List<DocumentItem> items = new ArrayList<>();

        // Debit Item
        MorabeheRelationType debitRelation = MorabeheRelationType.PRINCIPAL;
        DocumentItem debitItem =
                findAndCreateAccountItem(debitRelation, amount, topic, postTitle, metadata, notification);
        if (debitItem != null) {
            items.add(debitItem);
        }

        // Credit Item (Box or Deposit)
        DisburseDestination destination =
                context.loanFacility().getLoanApplication().getDisburseDestination();
        switch (destination.type()) {
            case BOX -> {
                DocumentItem item = DocumentItem.createBoxItem(
                        amount,
                        Direction.CREDITOR,
                        commentFactory.createBoxComment(Direction.CREDITOR, postTitle),
                        metadata);
                items.add(item);
            }
            case DEPOSIT -> {
                DocumentItem item = DocumentItem.createDepositItem(
                        destination.depositNumber(),
                        amount,
                        Direction.CREDITOR,
                        commentFactory.createDepositComment(Direction.CREDITOR, destination.depositNumber(), postTitle),
                        metadata);
                items.add(item);
            }
        }

        return Result.of(items, notification);
    }
}
