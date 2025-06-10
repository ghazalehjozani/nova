package ir.dotin.loan.trade.core.domain.disbursement.strategy.impl;

import com.google.common.collect.ImmutableList;
import ir.dotin.loan.baseloan.core.domain.shared.interaction.FindAccountByRelationTypeClient;
import ir.dotin.loan.baseloan.core.domain.shared.strategy.AbstractMultiArticleCalculationStrategy;
import ir.dotin.loan.baseloan.core.domain.shared.strategy.CalculationContext;
import ir.dotin.loan.baseloan.core.domain.shared.strategy.factory.DebitCreditArticleSpecFactory;
import ir.dotin.loan.baseloan.core.domain.shared.validator.ArticleBalanceValidator;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.Article;
import ir.dotin.loan.trade.core.domain.disbursement.enums.DisburseBankCommitmentArticleType;
import ir.dotin.loan.trade.core.domain.disbursement.strategy.CommitmentHandlingStrategy;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;
import ir.dotin.loan.trade.core.domain.loantype.enums.TradeRelationType;
import ir.dotin.platform.domain.common.Result;
import ir.dotin.platform.domain.common.annotation.DomainComponent;
import ir.dotin.platform.domain.common.annotation.DomainService;

import java.util.List;

import static java.util.Objects.requireNonNull;

@DomainComponent
public final class BankCommitmentTransactionStrategy
        extends AbstractMultiArticleCalculationStrategy<
                TradeLoanFacility, TradeRelationType, DisburseBankCommitmentArticleType>
        implements CommitmentHandlingStrategy {

    private final DebitCreditArticleSpecFactory<DisburseBankCommitmentArticleType, TradeRelationType> specFactory;

    public BankCommitmentTransactionStrategy(
            FindAccountByRelationTypeClient findAccountClient,
            DebitCreditArticleSpecFactory<DisburseBankCommitmentArticleType, TradeRelationType>
                    bankCommitmentArticleSpecFactory,
            ArticleBalanceValidator articleBalanceValidator) {
        super(findAccountClient, requireNonNull(articleBalanceValidator, "Balance validator cannot be null"));
        this.specFactory = requireNonNull(bankCommitmentArticleSpecFactory, "Spec factory cannot be null");
    }

    @Override
    protected Result<List<Article>> generateDebits(
            CalculationContext<TradeLoanFacility, TradeRelationType, DisburseBankCommitmentArticleType> context) {

        return context.requireArticleComponent(specFactory.getDebitArticleType())
                .flatMap(specFactory::createDebitSpec)
                .flatMap(spec -> createMultipleDebits(context, ImmutableList.of(spec)));
    }

    @Override
    protected Result<List<Article>> generateCredits(
            CalculationContext<TradeLoanFacility, TradeRelationType, DisburseBankCommitmentArticleType> context) {

        return context.requireArticleComponent(specFactory.getCreditArticleType())
                .flatMap(specFactory::createCreditSpec)
                .flatMap(spec -> createMultipleCredits(context, ImmutableList.of(spec)));
    }
}
