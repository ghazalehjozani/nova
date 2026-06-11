package ir.dotin.loan.trade.core.application.ports.outbound.client;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.servicelayer.api.port.RemotePort;
import ir.dotin.loan.baseloan.core.domain.shared.vo.AccountInfo;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTopic;

public interface FindOrCreateAccountPort extends RemotePort {

    /**
     * Finds an existing account for the given loan topic or creates a new one if not found.
     *
     * @param loanTopic The loan topic to find or create account for
     * @return AccountInfo for the found or created account
     */
    Result<AccountInfo> findOrCreateAccount(LoanTopic loanTopic);
}
