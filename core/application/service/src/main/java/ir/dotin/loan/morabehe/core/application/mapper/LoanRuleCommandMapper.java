package ir.dotin.loan.morabehe.core.application.mapper;

import ir.dotin.loan.morabehe.core.application.command.CreateLoanRuleCommand;
import ir.dotin.loan.morabehe.core.domain.config.entity.loanrule.LoanRule.LoanRuleBuilder;
import ir.dotin.loan.morabehe.core.domain.config.entity.loanrule.MorabeheLoanRule;
import ir.dotin.platform.ddd.common.interaction.feature.FeatureConfig;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class LoanRuleCommandMapper {

    public MorabeheLoanRule mapToAggregateRoot(CreateLoanRuleCommand command) {
        LoanRuleBuilder loanRuleBuilder = new LoanRuleBuilder(new FeatureConfig(Map.of()));
        // TODO: map command To aggregate
        return new MorabeheLoanRule(null, loanRuleBuilder);
    }

    // TODO: map to response

}
