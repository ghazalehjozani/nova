package ir.dotin.loan.trade.core.application.ports.outbound.client.accountservice;

import java.util.List;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
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

    Result<List<TrackedTransactionNumber>> postTransactions(
            LoanFacilityId facilityId, String documentComment, List<LoanTransaction> transactionsToPost);

    Result<Void> reverseTransaction(TrackedTransactionNumber transactionNumber);

    default Result<Void> reverseTransactions(List<TrackedTransactionNumber> transactionNumbers) {
        Notification notification = Notification.create();
        transactionNumbers.parallelStream().forEach(transactionNumber -> {
            Result<Void> result = reverseTransaction(transactionNumber);
            if (result.hasErrors()) {
                notification.merge(result.notification());
            }
        });
        return notification.hasErrors() ? Result.failure(notification) : Result.success();
    }
}
