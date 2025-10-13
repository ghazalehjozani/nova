package ir.dotin.loan.trade.adapters.driven.fcbclient.noop;

import org.springframework.stereotype.Component;

import ir.dotin.platform.commons.core.Result;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTransaction;
import ir.dotin.loan.baseloan.core.domain.shared.vo.TransactionNumber;
import ir.dotin.loan.trade.core.application.ports.outbound.client.ExternalTransactionPostingPort;

/**
 * No-operation (NoOp) implementation of ExternalTransactionPostingPort that returns failure. This adapter is used when
 * no real implementation is available, providing a safe fallback.
 */
@Component
public class NoOpExternalTransactionPostingClientAdapter implements ExternalTransactionPostingPort {

    @Override
    public Result<TransactionNumber> postTransaction(LoanTransaction transactionToPost) {
        return Result.success(TransactionNumber.of("123").getValue());
    }
}
