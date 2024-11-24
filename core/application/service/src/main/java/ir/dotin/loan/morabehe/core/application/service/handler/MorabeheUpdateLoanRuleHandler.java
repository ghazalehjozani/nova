package ir.dotin.loan.morabehe.core.application.service.handler;

import ir.dotin.loan.morabehe.core.application.service.command.MorabeheUpdateLoanRuleCommand;
import ir.dotin.loan.morabehe.core.application.service.mapper.MorabeheLoanRuleCommandMapper;
import ir.dotin.loan.morabehe.core.application.service.response.LoanRuleResponse;
import ir.dotin.loan.morabehe.core.application.service.usecase.UpdateLoanRuleUseCase;
import ir.dotin.loan.morabehe.core.domain.config.entity.loanrule.MorabeheLoanRule;
import org.springframework.stereotype.Component;

@Component
public class MorabeheUpdateLoanRuleHandler {

    private final MorabeheLoanRuleCommandMapper morabeheLoanRuleCommandMapper;
    private final UpdateLoanRuleUseCase updateLoanRuleUseCase;

    public MorabeheUpdateLoanRuleHandler(MorabeheLoanRuleCommandMapper morabeheLoanRuleCommandMapper, UpdateLoanRuleUseCase updateLoanRuleUseCase) {
        this.morabeheLoanRuleCommandMapper = morabeheLoanRuleCommandMapper;
        this.updateLoanRuleUseCase = updateLoanRuleUseCase;
    }

    public LoanRuleResponse handle(MorabeheUpdateLoanRuleCommand command) {
        MorabeheLoanRule newMorabeheLoanRule = morabeheLoanRuleCommandMapper.mapToAggregateRoot(command);
        MorabeheLoanRule updatedLoanRule = updateLoanRuleUseCase.update(command.loanRuleId(), newMorabeheLoanRule);
        return morabeheLoanRuleCommandMapper.mapToResponse(updatedLoanRule);
    }
}
