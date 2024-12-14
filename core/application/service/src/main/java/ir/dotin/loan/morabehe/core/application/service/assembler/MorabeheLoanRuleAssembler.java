package ir.dotin.loan.morabehe.core.application.service.assembler;

import ir.dotin.loan.baseloan.application.service.assembler.config.BaseLoanRuleAssembler;
import ir.dotin.loan.morabehe.core.application.service.command.MorabeheCreateLoanRuleCommand;
import ir.dotin.loan.morabehe.core.application.service.command.MorabeheUpdateLoanRuleCommand;
import ir.dotin.loan.morabehe.core.application.service.response.LoanRuleResponse;
import ir.dotin.loan.morabehe.core.domain.config.entity.loanrule.LoanRule.LoanRuleBuilder;
import ir.dotin.loan.morabehe.core.domain.config.entity.loanrule.MorabeheLoanRule;
import ir.dotin.loan.morabehe.core.domain.config.valueobject.MorabeheLoanRuleId;
import ir.dotin.platform.ddd.common.interaction.feature.FeatureConfig;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class MorabeheLoanRuleAssembler extends BaseLoanRuleAssembler<LoanRuleBuilder> {


    public MorabeheLoanRule mapToAggregateRoot(MorabeheCreateLoanRuleCommand command) {
        LoanRuleBuilder loanRuleBuilder = new LoanRuleBuilder(
                new FeatureConfig(Map.of("Key1", false)));
        super.mapFromCommand(command.loanRule(), loanRuleBuilder);
        return new MorabeheLoanRule(null, loanRuleBuilder.validateAndBuild());
    }


    public MorabeheLoanRule mapToAggregateRoot(MorabeheUpdateLoanRuleCommand command) {
        LoanRuleBuilder loanRuleBuilder = new LoanRuleBuilder(
                new FeatureConfig(Map.of("Key1", false)));
        super.mapFromCommand(command.loanRule(), loanRuleBuilder);
        return new MorabeheLoanRule(new MorabeheLoanRuleId(command.loanRule().loanRuleId()), loanRuleBuilder.validateAndBuild());
    }

    public LoanRuleResponse mapToResponse(MorabeheLoanRule loanRule) {
        return new LoanRuleResponse(loanRule.id().value());
    }

}
