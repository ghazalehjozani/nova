package ir.dotin.loan.trade.core.application.ports.outbound.client;

import ir.dotin.platform.accounting.document.api.model.AccountId;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.servicelayer.api.port.RemoteReadPort;
import ir.dotin.loan.baseloan.core.domain.shared.vo.AccountInfo;

public interface FindAccountByIdPort extends RemoteReadPort {

    /**
     * Finds account information by account ID.
     *
     * @param accountId The account ID to search for
     * @return AccountInfo if found, or null if not found
     */
    Result<AccountInfo> findAccountById(AccountId accountId);
}
