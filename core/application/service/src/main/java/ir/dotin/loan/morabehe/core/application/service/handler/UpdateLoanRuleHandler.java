package ir.dotin.loan.morabehe.core.application.service.handler;

import ir.dotin.loan.morabehe.core.application.service.UpdateLoanRuleUseCase;
import ir.dotin.loan.morabehe.core.application.service.command.UpdateLoanRuleCommand;
import ir.dotin.loan.morabehe.core.application.service.mapper.LoanRuleCommandMapper;
import ir.dotin.loan.morabehe.core.domain.config.entity.loanrule.MorabeheLoanRule;
import org.springframework.stereotype.Component;

@Component
public class UpdateLoanRuleHandler {

    private final LoanRuleCommandMapper loanRuleCommandMapper;
    private final UpdateLoanRuleUseCase updateLoanRuleUseCase;

    public UpdateLoanRuleHandler(LoanRuleCommandMapper loanRuleCommandMapper, UpdateLoanRuleUseCase updateLoanRuleUseCase) {
        this.loanRuleCommandMapper = loanRuleCommandMapper;
        this.updateLoanRuleUseCase = updateLoanRuleUseCase;
    }

    public String handle(UpdateLoanRuleCommand command) {
        MorabeheLoanRule newMorabeheLoanRule = loanRuleCommandMapper.mapToAggregateRoot(command.newLoanRuleCommand());
        MorabeheLoanRule oldMorabeheLoanRule = loanRuleCommandMapper.mapToAggregateRoot(command.oldLoanRuleCommand());
        return updateLoanRuleUseCase.update(newMorabeheLoanRule, oldMorabeheLoanRule);
    }
}
