package ir.dotin.loan.trade.core.domain.disbursement.strategy.impl;

import java.util.List;

import com.google.common.collect.ImmutableList;

import ir.dotin.platform.domain.common.Result;
import ir.dotin.platform.domain.common.annotation.DomainService;
import ir.dotin.loan.baseloan.core.domain.shared.interaction.FindAccountByRelationTypeClient;
import ir.dotin.loan.baseloan.core.domain.shared.strategy.AbstractMultiArticleCalculationStrategy;
import ir.dotin.loan.baseloan.core.domain.shared.strategy.CalculationContext;
import ir.dotin.loan.baseloan.core.domain.shared.strategy.factory.DebitCreditArticleSpecFactory;
import ir.dotin.loan.baseloan.core.domain.shared.validator.ArticleBalanceValidator;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.Article;
import ir.dotin.loan.trade.core.domain.disbursement.enums.PaymentAmountArticleType;
import ir.dotin.loan.trade.core.domain.disbursement.strategy.CashMovementStrategy;
import ir.dotin.loan.trade.core.domain.disbursement.strategy.factory.PaymentAmountArticleSpecFactory;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;
import ir.dotin.loan.trade.core.domain.loantype.enums.TradeRelationType;

import static java.util.Objects.requireNonNull;

@DomainService
public final class PaymentAmountTransactionStrategy
        extends AbstractMultiArticleCalculationStrategy<TradeLoanFacility, TradeRelationType, PaymentAmountArticleType>
        implements CashMovementStrategy {

    private final DebitCreditArticleSpecFactory<PaymentAmountArticleType, TradeRelationType> specFactory;

    public PaymentAmountTransactionStrategy(
            FindAccountByRelationTypeClient findAccountClient,
            DebitCreditArticleSpecFactory<PaymentAmountArticleType, TradeRelationType> paymentAmountArticleSpecFactory,
            ArticleBalanceValidator articleBalanceValidator) {
        super(findAccountClient, requireNonNull(articleBalanceValidator, "Balance validator cannot be null"));
        this.specFactory = requireNonNull(paymentAmountArticleSpecFactory, "Spec factory cannot be null");
    }

    public PaymentAmountTransactionStrategy(FindAccountByRelationTypeClient findAccountClient) {
        this(findAccountClient, new PaymentAmountArticleSpecFactory(), new ArticleBalanceValidator());
    }

    @Override
    protected Result<List<Article>> generateDebits(
            CalculationContext<TradeLoanFacility, TradeRelationType, PaymentAmountArticleType> context) {

        return context.requireArticleComponent(specFactory.getDebitArticleType())
                .flatMap(specFactory::createDebitSpec)
                .flatMap(spec -> createMultipleDebits(context, ImmutableList.of(spec)));
    }

    @Override
    protected Result<List<Article>> generateCredits(
            CalculationContext<TradeLoanFacility, TradeRelationType, PaymentAmountArticleType> context) {

        return context.requireArticleComponent(specFactory.getCreditArticleType())
                .flatMap(specFactory::createCreditSpec)
                .flatMap(spec -> createMultipleCredits(context, ImmutableList.of(spec)));
    }
}
