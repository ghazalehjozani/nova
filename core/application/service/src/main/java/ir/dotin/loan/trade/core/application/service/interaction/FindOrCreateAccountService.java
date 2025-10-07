package ir.dotin.loan.trade.core.application.service.interaction;

import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import ir.dotin.platform.commons.core.Result;
import ir.dotin.loan.baseloan.core.domain.shared.interaction.FindOrCreateAccountClient;
import ir.dotin.loan.baseloan.core.domain.shared.vo.AccountInfo;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTopic;
import ir.dotin.loan.trade.core.application.ports.outbound.client.FindOrCreateAccountPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FindOrCreateAccountService implements FindOrCreateAccountClient {

    private final FindOrCreateAccountPort findOrCreateAccountPort;

    @Override
    public @NonNull Result<AccountInfo> interact(@NonNull LoanTopic input) {
        return findOrCreateAccountPort.findOrCreateAccount(input);
    }
}
