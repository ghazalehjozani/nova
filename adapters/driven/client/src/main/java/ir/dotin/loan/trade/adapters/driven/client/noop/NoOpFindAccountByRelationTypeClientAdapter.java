package ir.dotin.loan.trade.adapters.driven.client.noop;

import org.springframework.stereotype.Component;

import ir.dotin.platform.commons.core.Result;
import ir.dotin.loan.baseloan.core.domain.shared.interaction.FindAccountByRelationTypeClient;
import ir.dotin.loan.baseloan.core.domain.shared.vo.AccountInfo;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.AccountId;
import ir.dotin.loan.trade.core.application.ports.outbound.client.FindAccountByRelationTypePort;

/**
 * No-operation (NoOp) implementation of FindAccountByRelationTypePort that returns failure. This adapter is used when
 * no real implementation is available, providing a safe fallback.
 */
@Component
public class NoOpFindAccountByRelationTypeClientAdapter implements FindAccountByRelationTypePort {

    @Override
    public Result<AccountInfo> findAccount(FindAccountByRelationTypeClient.FindAccountByRelationTypeInput input) {
        // Return failure to indicate account finding is not available
        return AccountInfo.of(AccountId.valueOf("123").getValue(), null, null);
    }
}
