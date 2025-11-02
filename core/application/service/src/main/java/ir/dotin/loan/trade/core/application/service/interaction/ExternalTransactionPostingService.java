package ir.dotin.loan.trade.core.application.service.interaction;

import org.springframework.stereotype.Service;

import ir.dotin.platform.commons.core.Result;
import ir.dotin.loan.baseloan.core.domain.shared.interaction.ExternalTransactionPostingClient;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTransaction;
import ir.dotin.loan.baseloan.core.domain.shared.vo.TrackedTransactionNumber;
import ir.dotin.loan.trade.core.application.ports.outbound.client.accountservice.TransactionPostingPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExternalTransactionPostingService implements ExternalTransactionPostingClient {

    private final TransactionPostingPort externalTransactionPostingPort;

    @Override
    public Result<TrackedTransactionNumber> postTransaction(LoanTransaction transactionToPost) {
        return externalTransactionPostingPort.postTransaction(transactionToPost);
    }
}
