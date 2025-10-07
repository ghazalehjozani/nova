package ir.dotin.loan.trade.adapters.driven.client.noop;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import ir.dotin.platform.commons.core.Result;
import ir.dotin.loan.baseloan.core.domain.shared.vo.AccountInfo;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTopic;
import ir.dotin.loan.trade.core.application.ports.outbound.client.FindOrCreateAccountPort;

/**
 * No-operation (NoOp) implementation of FindOrCreateAccountPort that returns null. This adapter is used when no real
 * implementation is available, providing a safe fallback.
 */
@Component
@ConditionalOnMissingBean(FindOrCreateAccountPort.class)
public class NoOpFindOrCreateAccountClientAdapter implements FindOrCreateAccountPort {

    @Override
    public Result<AccountInfo> findOrCreateAccount(LoanTopic loanTopic) {
        // Return null to indicate no account found or created
        return Result.success();
    }
}
