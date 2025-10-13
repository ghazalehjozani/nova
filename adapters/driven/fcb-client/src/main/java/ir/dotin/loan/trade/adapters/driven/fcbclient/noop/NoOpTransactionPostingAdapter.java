package ir.dotin.loan.trade.adapters.driven.fcbclient.noop;

import java.util.List;

import org.springframework.stereotype.Component;

import ir.dotin.platform.commons.core.Result;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTransaction;
import ir.dotin.loan.baseloan.core.domain.shared.vo.TrackedTransactionNumbers;
import ir.dotin.loan.trade.core.application.ports.outbound.client.TransactionPostingPort;
import ir.dotin.loan.trade.core.domain.loantype.enums.TradeRelationType;

@Component
public class NoOpTransactionPostingAdapter implements TransactionPostingPort {
    @Override
    public Result<TrackedTransactionNumbers<TradeRelationType>> postTransaction(LoanTransaction transaction) {
        return null;
    }

    @Override
    public Result<TrackedTransactionNumbers<TradeRelationType>> postTransactions(List<LoanTransaction> transactions) {
        return null;
    }
}
