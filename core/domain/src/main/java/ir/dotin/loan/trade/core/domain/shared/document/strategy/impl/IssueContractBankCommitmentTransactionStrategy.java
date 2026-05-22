package ir.dotin.loan.trade.core.domain.shared.document.strategy.impl;

import java.util.List;

import ir.dotin.platform.accounting.document.api.model.Article;
import ir.dotin.platform.accounting.document.api.strategy.AbstractMultiArticleCalculationStrategy;
import ir.dotin.platform.accounting.document.api.strategy.CalculationContext;
import ir.dotin.platform.accounting.document.api.validation.ArticleBalanceValidator;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.domain.annotation.DomainService;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;
import ir.dotin.loan.trade.core.domain.loantype.enums.TradeRelationType;
import ir.dotin.loan.trade.core.domain.shared.document.enums.IssueContractBankCommitmentArticleType;
import ir.dotin.loan.trade.core.domain.shared.document.factory.IssueContractBankCommitmentArticleSpecFactory;
import ir.dotin.loan.trade.core.domain.shared.document.strategy.IssueContractCommitmentHandlingStrategy;

import static java.util.Objects.requireNonNull;

@DomainService
public final class IssueContractBankCommitmentTransactionStrategy
        extends AbstractMultiArticleCalculationStrategy<
                TradeLoanFacility, TradeRelationType, IssueContractBankCommitmentArticleType>
        implements IssueContractCommitmentHandlingStrategy {

    private final IssueContractBankCommitmentArticleSpecFactory specFactory;

    public IssueContractBankCommitmentTransactionStrategy(
            IssueContractBankCommitmentArticleSpecFactory bankCommitmentArticleSpecFactory,
            ArticleBalanceValidator articleBalanceValidator) {
        super(requireNonNull(articleBalanceValidator, "Balance validator cannot be null"));
        this.specFactory = requireNonNull(bankCommitmentArticleSpecFactory, "Spec factory cannot be null");
    }

    @Override
    protected Result<List<Article>> generateDebits(
            CalculationContext<TradeLoanFacility, TradeRelationType, IssueContractBankCommitmentArticleType> context) {
        return context.requireArticleComponent(specFactory.getDebitArticleType())
                .flatMap(specFactory::createDebitSpec)
                .flatMap(spec -> createMultipleDebits(context, List.of(spec)));
    }

    @Override
    protected Result<List<Article>> generateCredits(
            CalculationContext<TradeLoanFacility, TradeRelationType, IssueContractBankCommitmentArticleType> context) {
        return context.requireArticleComponent(specFactory.getCreditArticleType())
                .flatMap(specFactory::createCreditSpec)
                .flatMap(spec -> createMultipleCredits(context, List.of(spec)));
    }
}
