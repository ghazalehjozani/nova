package ir.dotin.loan.trade.core.application.service.interaction;

import org.springframework.stereotype.Service;

import ir.dotin.platform.commons.core.Result;
import ir.dotin.loan.baseloan.core.domain.shared.interaction.FindAccountByRelationTypeClient;
import ir.dotin.loan.baseloan.core.domain.shared.vo.AccountInfo;
import ir.dotin.loan.trade.core.application.ports.outbound.client.FindAccountByRelationTypePort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FindAccountByRelationTypeService implements FindAccountByRelationTypeClient {

    private final FindAccountByRelationTypePort findAccountByRelationTypePort;

    @Override
    public Result<AccountInfo> findAccount(FindAccountByRelationTypeInput input) {
        return findAccountByRelationTypePort.findAccount(input);
    }
}
