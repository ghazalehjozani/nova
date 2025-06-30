package ir.dotin.loan.trade.core.domain.loanfacility.strategy.impl;

import java.util.List;

import com.google.common.collect.ImmutableList;

import ir.dotin.platform.domain.common.Result;
import ir.dotin.platform.domain.common.annotation.DomainComponent;
import ir.dotin.loan.baseloan.core.domain.shared.interaction.FindAccountByRelationTypeClient;
import ir.dotin.loan.baseloan.core.domain.shared.strategy.AbstractMultiArticleCalculationStrategy;
import ir.dotin.loan.baseloan.core.domain.shared.strategy.CalculationContext;
import ir.dotin.loan.baseloan.core.domain.shared.strategy.factory.DebitCreditArticleSpecFactory;
import ir.dotin.loan.baseloan.core.domain.shared.validator.ArticleBalanceValidator;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.Article;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;
import ir.dotin.loan.trade.core.domain.loanfacility.enums.DisbursedInterestArticleType;
import ir.dotin.loan.trade.core.domain.loanfacility.strategy.DisbursedInterestFacilitiesStrategy;
import ir.dotin.loan.trade.core.domain.loantype.enums.TradeRelationType;

import static java.util.Objects.requireNonNull;

@DomainComponent
public final class DisbursedInterestTransactionStrategy
        extends AbstractMultiArticleCalculationStrategy<
                TradeLoanFacility, TradeRelationType, DisbursedInterestArticleType>
        implements DisbursedInterestFacilitiesStrategy {

    private final DebitCreditArticleSpecFactory<DisbursedInterestArticleType, TradeRelationType> specFactory;

    public DisbursedInterestTransactionStrategy(
            FindAccountByRelationTypeClient findAccountClient,
            DebitCreditArticleSpecFactory<DisbursedInterestArticleType, TradeRelationType>
                    disbursedInterestArticleSpecFactory,
            ArticleBalanceValidator articleBalanceValidator) {
        super(findAccountClient, requireNonNull(articleBalanceValidator, "Balance validator cannot be null"));
        this.specFactory = requireNonNull(disbursedInterestArticleSpecFactory, "Spec factory cannot be null");
    }

    @Override
    protected Result<List<Article>> generateDebits(
            CalculationContext<TradeLoanFacility, TradeRelationType, DisbursedInterestArticleType> context) {

        return context.requireArticleComponent(specFactory.getDebitArticleType())
                .flatMap(specFactory::createDebitSpec)
                .flatMap(spec -> createMultipleDebits(context, ImmutableList.of(spec)));
    }

    @Override
    protected Result<List<Article>> generateCredits(
            CalculationContext<TradeLoanFacility, TradeRelationType, DisbursedInterestArticleType> context) {

        return context.requireArticleComponent(specFactory.getCreditArticleType())
                .flatMap(specFactory::createCreditSpec)
                .flatMap(spec -> createMultipleCredits(context, ImmutableList.of(spec)));
    }
}
