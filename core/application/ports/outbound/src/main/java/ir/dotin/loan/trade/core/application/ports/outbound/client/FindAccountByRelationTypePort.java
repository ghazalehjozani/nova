package ir.dotin.loan.trade.core.application.ports.outbound.client;

import ir.dotin.platform.commons.core.Result;
import ir.dotin.loan.baseloan.core.domain.shared.interaction.FindAccountByRelationTypeClient;
import ir.dotin.loan.baseloan.core.domain.shared.vo.AccountInfo;

public interface FindAccountByRelationTypePort {

    /**
     * Finds account information by loan topic and relation type.
     *
     * @param input The input containing loan topic
     * @return Result containing AccountInfo if successful, or failure notification
     */
    Result<AccountInfo> findAccount(FindAccountByRelationTypeClient.FindAccountByRelationTypeInput input);
}
