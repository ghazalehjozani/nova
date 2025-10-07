package ir.dotin.loan.trade.core.application.service.interaction;

import org.springframework.stereotype.Service;

import ir.dotin.platform.commons.core.Result;
import ir.dotin.loan.baseloan.core.domain.shared.interaction.LoanTopicResolver;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTopic;
import ir.dotin.loan.trade.core.application.ports.outbound.client.LoanTopicResolverPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LoanTopicResolverService implements LoanTopicResolver {

    private final LoanTopicResolverPort loanTopicResolverPort;

    @Override
    public Result<LoanTopic> resolvePrimaryTopic(LoanTopicResolver.Input input) {
        return loanTopicResolverPort.resolvePrimaryTopic(input);
    }
}
