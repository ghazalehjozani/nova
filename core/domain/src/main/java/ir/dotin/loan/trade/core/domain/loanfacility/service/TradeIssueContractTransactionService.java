package ir.dotin.loan.trade.core.domain.loanfacility.service;

import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.commons.domain.annotation.DomainService;
import ir.dotin.loan.baseloan.core.domain.shared.factory.DocumentFactory;
import ir.dotin.loan.baseloan.core.domain.shared.strategy.CalculationContext;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTransaction;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;
import ir.dotin.loan.trade.core.domain.loanfacility.enums.IssueContractBankCommitmentArticleType;
import ir.dotin.loan.trade.core.domain.loanfacility.strategy.IssueContractCommitmentHandlingStrategy;
import ir.dotin.loan.trade.core.domain.loantype.enums.TradeRelationType;

import static java.util.Objects.requireNonNull;

@DomainService
public class TradeIssueContractTransactionService {

    private final DocumentFactory documentFactory;
    private final IssueContractCommitmentHandlingStrategy strategy;

    public TradeIssueContractTransactionService(
            DocumentFactory documentFactory, IssueContractCommitmentHandlingStrategy strategy) {
        this.documentFactory = requireNonNull(documentFactory);
        this.strategy = strategy;
    }

    public Result<LoanTransaction> calculateTransaction(
            CalculationContext<TradeLoanFacility, TradeRelationType, IssueContractBankCommitmentArticleType> context,
            String postTitle) {
        requireNonNull(context, "context cannot be null");
        requireNonNull(postTitle, "postTitle cannot be null");

        return documentFactory.createTransaction(context, strategy, postTitle);
    }
}
