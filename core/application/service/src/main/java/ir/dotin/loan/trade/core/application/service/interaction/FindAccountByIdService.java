package ir.dotin.loan.trade.core.application.service.interaction;

import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import ir.dotin.platform.commons.core.Result;
import ir.dotin.loan.baseloan.core.domain.shared.interaction.FindAccountByIdClient;
import ir.dotin.loan.baseloan.core.domain.shared.vo.AccountInfo;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.AccountId;
import ir.dotin.loan.trade.core.application.ports.outbound.client.FindAccountByIdPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FindAccountByIdService implements FindAccountByIdClient {

    private final FindAccountByIdPort findAccountByIdPort;

    @Override
    public @NonNull Result<AccountInfo> interact(@NonNull AccountId input) {
        return findAccountByIdPort.findAccountById(input);
    }
}
