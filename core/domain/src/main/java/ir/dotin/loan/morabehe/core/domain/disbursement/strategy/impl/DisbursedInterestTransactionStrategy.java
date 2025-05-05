package ir.dotin.loan.morabehe.core.domain.disbursement.strategy.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import ir.dotin.loan.baseloan.core.domain.shared.vo.transaction.Article;
import ir.dotin.platform.domain.common.Notification;
import ir.dotin.platform.domain.common.Result;
import ir.dotin.platform.domain.common.annotation.DomainService;
import ir.dotin.platform.domain.common.vo.Money;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.vo.InterestPolicy;
import ir.dotin.loan.baseloan.core.domain.shared.formula.BaseFormulaField;
import ir.dotin.loan.baseloan.core.domain.shared.interaction.FindAccountByRelationTypeClient;
import ir.dotin.loan.baseloan.core.domain.shared.service.transaction.ArticleCommentFactory;
import ir.dotin.loan.baseloan.core.domain.shared.strategy.AbstractDocumentItemStrategy;
import ir.dotin.loan.baseloan.core.domain.shared.strategy.CalculationContext;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTopic;
import ir.dotin.loan.baseloan.core.domain.shared.vo.transaction.PostTitle;
import ir.dotin.loan.baseloan.core.domain.shared.vo.transaction.metadata.TransactionMetadata;
import ir.dotin.loan.morabehe.core.domain.loanarrangement.aggregate.MorabeheLoanArrangement;
import ir.dotin.loan.morabehe.core.domain.loanarrangement.vo.MorabeheLoanArrangementId;
import ir.dotin.loan.morabehe.core.domain.loanfacility.aggregate.MorabeheLoanFacility;
import ir.dotin.loan.morabehe.core.domain.loanfacility.i18n.MorabeheLoanFacilityLocalizedMessageCodes;
import ir.dotin.loan.morabehe.core.domain.disbursement.formula.MorabeheInterestCalculationService;
import ir.dotin.loan.morabehe.core.domain.disbursement.strategy.DisbursedInterestFacilitiesStrategy;
import ir.dotin.loan.morabehe.core.domain.loantype.enums.MorabeheRelationType;
import ir.dotin.loan.morabehe.core.domain.shared.interaction.MorabeheLoanArrangementDataProvider;

@DomainService
public final class DisbursedInterestTransactionStrategy extends AbstractDocumentItemStrategy<MorabeheLoanFacility>
        implements DisbursedInterestFacilitiesStrategy {

    private final MorabeheLoanArrangementDataProvider arrangementDataProvider;
    private final MorabeheInterestCalculationService interestCalculationService;

    public DisbursedInterestTransactionStrategy(
            FindAccountByRelationTypeClient findAccountClient,
            ArticleCommentFactory commentFactory,
            MorabeheLoanArrangementDataProvider arrangementDataProvider,
            MorabeheInterestCalculationService interestCalculationService) {
        super(findAccountClient, commentFactory);
        this.arrangementDataProvider = Objects.requireNonNull(arrangementDataProvider);
        this.interestCalculationService = Objects.requireNonNull(interestCalculationService);
    }

    @Override
    public Result<List<Article>> calculateItems(CalculationContext<MorabeheLoanFacility> context) {
        return calculateItemsInternal(context, "Incomplete articles for Disbursed Facilities Interest");
    }

    @Override
    protected Result<List<Article>> generateItems(CalculationContext<MorabeheLoanFacility> context) {

        Money principal = context.principalAmount();
        if (principal.isZero()) {
            return Result.ofValue(Collections.emptyList());
        }

        MorabeheLoanArrangementId arrangementId = context.loanFacility().getLoanArrangementId();
        Result<MorabeheLoanArrangement> arrangementResult = arrangementDataProvider.findArrangementById(arrangementId);
        if (arrangementResult.isFailure()) {
            return Result.ofNotification(arrangementResult
                    .notification()
                    .addError(MorabeheLoanFacilityLocalizedMessageCodes.ARRANGEMENT_FETCH_FAILED, arrangementId));
        }
        MorabeheLoanArrangement arrangement = arrangementResult.value();
        InterestPolicy<BaseFormulaField> interestPolicy = arrangement.getInterestPolicy();

        Result<Money> interestAmountResult =
                interestCalculationService.calculateTotalInterest(interestPolicy, context.loanFacility());

        if (interestAmountResult.isFailure()) {
            return Result.ofNotification(interestAmountResult.notification());
        }
        Money totalInterestAmount = interestAmountResult.value();

        //noinspection DuplicatedCode
        LoanTopic topic = context.primaryLoanTopic();
        PostTitle postTitle = context.postTitle();
        TransactionMetadata metadata = context.baseMetadata();
        Notification notification = Notification.empty();
        List<Article> items = new ArrayList<>();

        // Debit Item
        MorabeheRelationType debitRelation = MorabeheRelationType.PRINCIPAL;
        Article debitItem =
                findAndCreateAccountItem(debitRelation, totalInterestAmount, topic, postTitle, metadata, notification);
        if (debitItem != null) {
            items.add(debitItem);
        }

        // Credit Item
        MorabeheRelationType creditRelation = MorabeheRelationType.FUTURE_INTEREST;
        Article creditItem =
                findAndCreateAccountItem(creditRelation, totalInterestAmount, topic, postTitle, metadata, notification);
        if (creditItem != null) {
            items.add(creditItem);
        }

        return Result.of(items, notification);
    }
}
