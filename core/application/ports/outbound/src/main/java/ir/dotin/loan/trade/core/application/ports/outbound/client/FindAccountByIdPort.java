package ir.dotin.loan.trade.core.application.ports.outbound.client;

import ir.dotin.platform.commons.core.Result;
import ir.dotin.loan.baseloan.core.domain.shared.vo.AccountInfo;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.AccountId;

public interface FindAccountByIdPort {

    /**
     * Finds account information by account ID.
     *
     * @param accountId The account ID to search for
     * @return AccountInfo if found, or null if not found
     */
    Result<AccountInfo> findAccountById(AccountId accountId);
}
