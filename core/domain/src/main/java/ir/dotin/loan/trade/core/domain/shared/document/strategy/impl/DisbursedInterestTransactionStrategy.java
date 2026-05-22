package ir.dotin.loan.trade.core.domain.shared.document.strategy.impl;

import java.util.List;

import ir.dotin.platform.accounting.document.api.model.Article;
import ir.dotin.platform.accounting.document.api.strategy.AbstractMultiArticleCalculationStrategy;
import ir.dotin.platform.accounting.document.api.strategy.CalculationContext;
import ir.dotin.platform.accounting.document.api.strategy.spec.DebitCreditArticleSpecFactory;
import ir.dotin.platform.accounting.document.api.validation.ArticleBalanceValidator;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.domain.annotation.DomainComponent;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;
import ir.dotin.loan.trade.core.domain.loantype.enums.TradeRelationType;
import ir.dotin.loan.trade.core.domain.shared.document.enums.DisbursedInterestArticleType;
import ir.dotin.loan.trade.core.domain.shared.document.strategy.DisbursedInterestFacilitiesStrategy;

import static java.util.Objects.requireNonNull;

@DomainComponent
public final class DisbursedInterestTransactionStrategy
        extends AbstractMultiArticleCalculationStrategy<
                TradeLoanFacility, TradeRelationType, DisbursedInterestArticleType>
        implements DisbursedInterestFacilitiesStrategy {

    private final DebitCreditArticleSpecFactory<DisbursedInterestArticleType, TradeRelationType> specFactory;

    public DisbursedInterestTransactionStrategy(
            DebitCreditArticleSpecFactory<DisbursedInterestArticleType, TradeRelationType> specFactory,
            ArticleBalanceValidator articleBalanceValidator) {
        super(requireNonNull(articleBalanceValidator, "Balance validator cannot be null"));
        this.specFactory = requireNonNull(specFactory, "Spec factory cannot be null");
    }

    @Override
    protected Result<List<Article>> generateDebits(
            CalculationContext<TradeLoanFacility, TradeRelationType, DisbursedInterestArticleType> context) {
        return context.requireArticleComponent(specFactory.getDebitArticleType())
                .flatMap(specFactory::createDebitSpec)
                .flatMap(spec -> createMultipleDebits(context, List.of(spec)));
    }

    @Override
    protected Result<List<Article>> generateCredits(
            CalculationContext<TradeLoanFacility, TradeRelationType, DisbursedInterestArticleType> context) {
        return context.requireArticleComponent(specFactory.getCreditArticleType())
                .flatMap(specFactory::createCreditSpec)
                .flatMap(spec -> createMultipleCredits(context, List.of(spec)));
    }
}
