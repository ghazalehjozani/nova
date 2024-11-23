package ir.dotin.loan.morabehe.core.application.service.mapper;

import ir.dotin.loan.baseloan.application.service.mapper.BaseLoanRuleCommandMapper;
import ir.dotin.loan.morabehe.core.application.service.command.MorabeheCreateLoanRuleCommand;
import ir.dotin.loan.morabehe.core.application.service.response.LoanRuleResponse;
import ir.dotin.loan.morabehe.core.domain.config.entity.loanrule.LoanRule.LoanRuleBuilder;
import ir.dotin.loan.morabehe.core.domain.config.entity.loanrule.MorabeheLoanRule;
import ir.dotin.platform.ddd.common.interaction.feature.FeatureConfig;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class MorabeheLoanRuleCommandMapper extends BaseLoanRuleCommandMapper<LoanRuleBuilder> {


    public MorabeheLoanRule mapToAggregateRoot(MorabeheCreateLoanRuleCommand command) {
        LoanRuleBuilder loanRuleBuilder = new LoanRuleBuilder(
                new FeatureConfig(Map.of("Key1", false)));
        super.mapCommonFields(command.loanRule(), loanRuleBuilder);
        return new MorabeheLoanRule(null, loanRuleBuilder.validateAndBuild());
    }

    public LoanRuleResponse mapToResponse(MorabeheLoanRule loanRule) {
        return new LoanRuleResponse(loanRule.getId().id());
    }

}
