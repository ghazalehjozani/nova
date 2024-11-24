package ir.dotin.loan.morabehe.core.application.service.handler;

import ir.dotin.loan.morabehe.core.application.service.command.MorabeheCreateLoanRuleCommand;
import ir.dotin.loan.morabehe.core.application.service.mapper.MorabeheLoanRuleCommandMapper;
import ir.dotin.loan.morabehe.core.application.service.response.LoanRuleResponse;
import ir.dotin.loan.morabehe.core.application.service.usecase.CreateLoanRuleUsecase;
import ir.dotin.loan.morabehe.core.domain.config.entity.loanrule.MorabeheLoanRule;
import org.springframework.stereotype.Component;

@Component
public class MorabeheCreateLoanRuleHandler {

    private final MorabeheLoanRuleCommandMapper loanRuleCommandMapper;
    private final CreateLoanRuleUsecase createLoanRuleUsecase;

    public MorabeheCreateLoanRuleHandler(MorabeheLoanRuleCommandMapper loanRuleCommandMapper,
                                         CreateLoanRuleUsecase createLoanRuleUsecase) {
        this.loanRuleCommandMapper = loanRuleCommandMapper;
        this.createLoanRuleUsecase = createLoanRuleUsecase;
    }

    public LoanRuleResponse handle(MorabeheCreateLoanRuleCommand command) {
        MorabeheLoanRule morabeheLoanRule = loanRuleCommandMapper.mapToAggregateRoot(command);
        MorabeheLoanRule savedLoanRule = createLoanRuleUsecase.create(morabeheLoanRule);
        return loanRuleCommandMapper.mapToResponse(savedLoanRule);
    }

}
