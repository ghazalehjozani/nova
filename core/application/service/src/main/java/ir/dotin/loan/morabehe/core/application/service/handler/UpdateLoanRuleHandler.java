package ir.dotin.loan.morabehe.core.application.service.handler;

import ir.dotin.loan.morabehe.core.application.service.command.UpdateLoanRuleCommand;
import ir.dotin.loan.morabehe.core.application.service.mapper.UpdateLoanRuleCommandMapper;
import ir.dotin.loan.morabehe.core.application.service.usecase.UpdateLoanRuleUseCase;
import ir.dotin.loan.morabehe.core.domain.config.entity.loanrule.MorabeheLoanRule;
import org.springframework.stereotype.Component;

@Component
public class UpdateLoanRuleHandler {

    private final UpdateLoanRuleCommandMapper updateLoanRuleCommandMapper;
    private final UpdateLoanRuleUseCase updateLoanRuleUseCase;

    public UpdateLoanRuleHandler(UpdateLoanRuleCommandMapper updateLoanRuleCommandMapper, UpdateLoanRuleUseCase updateLoanRuleUseCase) {
        this.updateLoanRuleCommandMapper = updateLoanRuleCommandMapper;
        this.updateLoanRuleUseCase = updateLoanRuleUseCase;
    }

    public String handle(UpdateLoanRuleCommand command) {
        MorabeheLoanRule newMorabeheLoanRule = updateLoanRuleCommandMapper.mapToAggregateRoot(command);
        return updateLoanRuleUseCase.update(newMorabeheLoanRule, command.oldLoanRuleId());
    }
}
