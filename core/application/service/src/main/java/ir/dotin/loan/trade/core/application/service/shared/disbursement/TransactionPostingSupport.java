package ir.dotin.loan.trade.core.application.service.shared.disbursement;

import java.time.Clock;
import java.util.List;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTransaction;
import ir.dotin.loan.baseloan.core.domain.shared.vo.TrackedTransactionNumber;
import ir.dotin.loan.trade.core.application.ports.outbound.client.accountservice.TransactionPostingPort;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TransactionPostingSupport {

    private final TransactionPostingPort transactionPostingPort;
    private final Clock clock;

    public Result<List<PostedTransaction>> postTransactions(
            LoanFacilityId facilityId, String mergedDocumentTitle, List<LoanTransaction> transactions) {
        return transactionPostingPort
                .postTransactions(facilityId, mergedDocumentTitle, transactions)
                .map(tracked -> tracked.stream()
                        .map(t -> new PostedTransaction(t.value(), t.trackingId(), t.status()))
                        .toList());
    }

    public Result<Unit> reverseTransactions(List<TrackedTransactionNumber> trackedNumbers) {
        return transactionPostingPort.reverseTransactions(trackedNumbers);
    }

    public List<TrackedTransactionNumber> rebuildTrackedNumbers(@Nullable List<PostedTransaction> postedTransactions) {
        if (postedTransactions == null) {
            return List.of();
        }
        return postedTransactions.stream()
                .map(posted -> TrackedTransactionNumber.create(
                        posted.transactionNumber(), posted.trackingId(), posted.status(), clock))
                .toList();
    }
}
