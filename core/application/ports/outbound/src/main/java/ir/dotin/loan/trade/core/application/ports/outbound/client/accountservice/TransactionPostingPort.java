package ir.dotin.loan.trade.core.application.ports.outbound.client.accountservice;

import ir.dotin.platform.commons.core.Result;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTransaction;
import ir.dotin.loan.baseloan.core.domain.shared.vo.TrackedTransactionNumber;

public interface TransactionPostingPort {

    /**
     * Posts a transaction to the external accounting system.
     *
     * @param transactionToPost The transaction to post
     * @return Result containing the transaction number if successful, or failure notification
     */
    Result<TrackedTransactionNumber> postTransaction(LoanTransaction transactionToPost);
}
