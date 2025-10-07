package ir.dotin.loan.trade.adapters.driven.client.noop;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import ir.dotin.platform.commons.core.Result;
import ir.dotin.loan.baseloan.core.domain.shared.interaction.LoanTopicResolver;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTopic;
import ir.dotin.loan.trade.core.application.ports.outbound.client.LoanTopicResolverPort;

/**
 * No-operation (NoOp) implementation of LoanTopicResolverPort that returns failure. This adapter is used when no real
 * implementation is available, providing a safe fallback.
 */
@Component
@ConditionalOnMissingBean(LoanTopicResolverPort.class)
public class NoOpLoanTopicResolverClientAdapter implements LoanTopicResolverPort {

    @Override
    public Result<LoanTopic> resolvePrimaryTopic(LoanTopicResolver.Input input) {
        // Return failure to indicate loan topic resolution is not available
        return Result.success();
    }
}
