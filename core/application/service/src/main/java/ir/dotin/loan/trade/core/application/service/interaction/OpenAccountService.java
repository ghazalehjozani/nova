package ir.dotin.loan.trade.core.application.service.interaction;

import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import ir.dotin.platform.commons.core.Result;
import ir.dotin.loan.baseloan.core.domain.shared.interaction.OpenAccountClient;
import ir.dotin.loan.baseloan.core.domain.shared.vo.AccountInfo;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTopic;
import ir.dotin.loan.trade.core.application.ports.outbound.client.accountservice.AccountServicePort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OpenAccountService implements OpenAccountClient {

    private final AccountServicePort findOrCreateAccountPort;

    @Override
    public @NonNull Result<AccountInfo> interact(@NonNull LoanTopic input) {
        return findOrCreateAccountPort.openAccount(input);
    }
}
