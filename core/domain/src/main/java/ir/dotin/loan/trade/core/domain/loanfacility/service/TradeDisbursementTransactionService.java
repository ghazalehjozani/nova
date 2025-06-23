package ir.dotin.loan.trade.core.domain.loanfacility.service;

import java.util.List;

import com.google.common.collect.ImmutableList;

import ir.dotin.platform.domain.common.Result;
import ir.dotin.platform.domain.common.annotation.DomainService;
import ir.dotin.loan.baseloan.core.domain.shared.factory.DocumentFactory;
import ir.dotin.loan.baseloan.core.domain.shared.strategy.CalculationContext;
import ir.dotin.loan.baseloan.core.domain.shared.strategy.DocumentCalculationStrategy;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTransaction;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;
import ir.dotin.loan.trade.core.domain.loanfacility.enums.DisbursedInterestArticleType;
import ir.dotin.loan.trade.core.domain.loantype.enums.TradeRelationType;

import static java.util.Objects.requireNonNull;

@DomainService
public final class TradeDisbursementTransactionService {

    private final DocumentFactory documentFactory;

    public TradeDisbursementTransactionService(DocumentFactory documentFactory) {
        this.documentFactory = requireNonNull(documentFactory);
    }

    public Result<LoanTransaction> calculateDisbursementTransaction(
            CalculationContext<TradeLoanFacility, TradeRelationType, DisbursedInterestArticleType> context,
            DocumentCalculationStrategy<TradeLoanFacility, TradeRelationType, DisbursedInterestArticleType> strategy,
            String postTitle) {
        requireNonNull(context, "context cannot be null");
        requireNonNull(strategy, "strategy cannot be null");
        requireNonNull(postTitle, "postTitle cannot be null");

        return documentFactory.createTransaction(context, strategy, postTitle);
    }

    public Result<List<LoanTransaction>> calculateMultipleDisbursementTransactions(
            CalculationContext<TradeLoanFacility, TradeRelationType, DisbursedInterestArticleType> context,
            List<DocumentCalculationStrategy<TradeLoanFacility, TradeRelationType, DisbursedInterestArticleType>>
                    strategies,
            String postTitle) {
        requireNonNull(context, "context cannot be null");
        requireNonNull(strategies, "strategies cannot be null");
        requireNonNull(postTitle, "postTitle cannot be null");

        if (strategies.isEmpty()) {
            return Result.success(ImmutableList.of());
        }

        return Result.traverse(strategies, strategy -> calculateDisbursementTransaction(context, strategy, postTitle));
    }
}
