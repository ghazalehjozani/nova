package ir.dotin.loan.trade.core.domain.loanfacility.strategy.impl;

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
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;
import ir.dotin.loan.trade.core.domain.loanfacility.enums.IssueContractBankCommitmentArticleType;
import ir.dotin.loan.trade.core.domain.loanfacility.strategy.IssueContractCommitmentHandlingStrategy;
import ir.dotin.loan.trade.core.domain.loanfacility.strategy.factory.IssueContractBankCommitmentArticleSpecFactory;
import ir.dotin.loan.trade.core.domain.loantype.enums.TradeRelationType;

import static java.util.Objects.requireNonNull;

@DomainService
public final class IssueContractBankCommitmentTransactionStrategy
        extends AbstractMultiArticleCalculationStrategy<TradeLoanFacility, TradeRelationType, IssueContractBankCommitmentArticleType>
        implements IssueContractCommitmentHandlingStrategy {

    private final DebitCreditArticleSpecFactory<IssueContractBankCommitmentArticleType, TradeRelationType> specFactory;

    public IssueContractBankCommitmentTransactionStrategy(
            FindAccountByRelationTypeClient findAccountClient,
            DebitCreditArticleSpecFactory<IssueContractBankCommitmentArticleType, TradeRelationType>
                    bankCommitmentArticleSpecFactory,
            ArticleBalanceValidator articleBalanceValidator) {
        super(findAccountClient, requireNonNull(articleBalanceValidator, "Balance validator cannot be null"));
        this.specFactory = requireNonNull(bankCommitmentArticleSpecFactory, "Spec factory cannot be null");
    }

    public IssueContractBankCommitmentTransactionStrategy(FindAccountByRelationTypeClient findAccountClient) {
        this(findAccountClient, new IssueContractBankCommitmentArticleSpecFactory(), new ArticleBalanceValidator());
    }

    @Override
    protected Result<List<Article>> generateDebits(
            CalculationContext<TradeLoanFacility, TradeRelationType, IssueContractBankCommitmentArticleType> context) {

        return context.requireArticleComponent(specFactory.getDebitArticleType())
                .flatMap(specFactory::createDebitSpec)
                .flatMap(spec -> createMultipleDebits(context, ImmutableList.of(spec)));
    }

    @Override
    protected Result<List<Article>> generateCredits(
            CalculationContext<TradeLoanFacility, TradeRelationType, IssueContractBankCommitmentArticleType> context) {

        return context.requireArticleComponent(specFactory.getCreditArticleType())
                .flatMap(specFactory::createCreditSpec)
                .flatMap(spec -> createMultipleCredits(context, ImmutableList.of(spec)));
    }
}
