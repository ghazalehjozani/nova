package ir.dotin.loan.trade.core.application.ports.outbound.client;

import ir.dotin.platform.commons.core.Result;
import ir.dotin.loan.baseloan.core.domain.shared.interaction.LoanTopicResolver;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTopic;

public interface LoanTopicResolverPort {

    /**
     * Resolves the primary loan topic based on loan type and economic sector.
     *
     * @param input The input containing loan type ID and economic sector
     * @return Result containing the resolved LoanTopic if successful, or failure notification
     */
    Result<LoanTopic> resolvePrimaryTopic(LoanTopicResolver.Input input);
}
