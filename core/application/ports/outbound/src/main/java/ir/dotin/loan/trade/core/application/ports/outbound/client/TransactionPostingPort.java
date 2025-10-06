package ir.dotin.loan.trade.core.application.ports.outbound.client;

import java.util.List;

import ir.dotin.platform.commons.core.Result;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTransaction;
import ir.dotin.loan.baseloan.core.domain.shared.vo.TrackedTransactionNumbers;
import ir.dotin.loan.trade.core.domain.loantype.enums.TradeRelationType;

public interface TransactionPostingPort {

    /**
     * Posts a single transaction to the external accounting system.
     *
     * @param transaction The transaction to post
     * @return Result containing a single tracked transaction number or failure notification
     */
    Result<TrackedTransactionNumbers<TradeRelationType>> postTransaction(LoanTransaction transaction);

    /**
     * Posts multiple transactions as a batch to the external accounting system. All transactions must succeed or the
     * entire batch fails (transactional semantics).
     *
     * @param transactions List of transactions to post
     * @return Result containing all tracked transaction numbers or failure notification
     */
    Result<TrackedTransactionNumbers<TradeRelationType>> postTransactions(List<LoanTransaction> transactions);
}
