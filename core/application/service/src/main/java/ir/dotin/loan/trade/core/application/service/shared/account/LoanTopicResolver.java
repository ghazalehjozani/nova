package ir.dotin.loan.trade.core.application.service.shared.account;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import ir.dotin.loan.baseloan.core.domain.shared.vo.EconomicSector;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTopic;
import ir.dotin.loan.trade.core.domain.loantype.entity.TradeLoanType;
import ir.dotin.loan.trade.core.domain.loantype.enums.TradeRelationType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LoanTopicResolver {

    public Set<LoanTopic> resolveTopics(
            TradeLoanType loanType, EconomicSector economicSector, Set<TradeRelationType> requiredRelationTypes) {

        return requiredRelationTypes.stream()
                .flatMap(relationType -> loanType.getRelationTypeLoanTopics().get(relationType).stream()
                        .filter(topic -> topic.economicSectors().contains(economicSector)))
                .collect(Collectors.toSet());
    }
}
