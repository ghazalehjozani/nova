package ir.dotin.loan.trade.adapters.driven.client.noop;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import ir.dotin.platform.commons.core.Result;
import ir.dotin.loan.baseloan.core.domain.shared.vo.AccountInfo;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.AccountId;
import ir.dotin.loan.trade.core.application.ports.outbound.client.FindAccountByIdPort;

/**
 * No-operation (NoOp) implementation of FindAccountByIdPort that returns null. This adapter is used when no real
 * implementation is available, providing a safe fallback.
 */
@Component
@ConditionalOnMissingBean(FindAccountByIdPort.class)
public class NoOpFindAccountByIdClientAdapter implements FindAccountByIdPort {

    @Override
    public Result<AccountInfo> findAccountById(AccountId accountId) {
        // Return null to indicate no account found
        return Result.success();
    }
}
