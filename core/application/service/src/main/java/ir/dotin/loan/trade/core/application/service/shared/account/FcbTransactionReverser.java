package ir.dotin.loan.trade.core.application.service.shared.account;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.loan.baseloan.core.domain.shared.vo.TrackedTransactionNumber;
import ir.dotin.loan.trade.core.application.ports.outbound.client.accountservice.TransactionPostingPort;

@Component
public class FcbTransactionReverser {

    private static final Logger log = LoggerFactory.getLogger(FcbTransactionReverser.class);

    private final TransactionPostingPort transactionPostingPort;

    public FcbTransactionReverser(TransactionPostingPort transactionPostingPort) {
        this.transactionPostingPort = transactionPostingPort;
    }

    public void reverseBestEffort(TrackedTransactionNumber tx) {
        Result<Unit> reverseResult = transactionPostingPort.reverseTransaction(tx);
        if (reverseResult.isFailure()) {
            log.warn(
                    "Local compensation committed but FCB reversal failed for transaction {}: {}",
                    tx.value(),
                    reverseResult.err().orElseThrow().notification());
        }
    }

    public void reverseAllBestEffort(List<TrackedTransactionNumber> txs) {
        Result<Unit> reverseResult = transactionPostingPort.reverseTransactions(txs);
        if (reverseResult.isFailure()) {
            log.warn(
                    "Local compensation committed but FCB reversal failed: {}",
                    reverseResult.err().orElseThrow().notification());
        }
    }
}
