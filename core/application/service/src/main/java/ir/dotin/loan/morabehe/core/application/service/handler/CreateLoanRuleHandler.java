package ir.dotin.loan.morabehe.core.application.service.handler;

import ir.dotin.loan.morabehe.core.application.service.command.CreateLoanRuleCommand;
import ir.dotin.loan.morabehe.core.application.service.loader.MorabeheLoanApplicationLoaderImpl;
import ir.dotin.loan.morabehe.core.application.service.mapper.LoanRuleCommandMapper;
import ir.dotin.loan.morabehe.core.application.service.response.LoanRuleResponse;
import ir.dotin.loan.morabehe.core.application.service.usecase.CreateLoanRuleUsecase;
import ir.dotin.loan.morabehe.core.domain.config.entity.loanrule.MorabeheLoanRule;
import org.springframework.stereotype.Component;

@Component
public class CreateLoanRuleHandler {

    private final LoanRuleCommandMapper loanRuleCommandMapper;
    private final CreateLoanRuleUsecase createLoanRuleUsecase;

    public CreateLoanRuleHandler(LoanRuleCommandMapper loanRuleCommandMapper,
                                 CreateLoanRuleUsecase createLoanRuleUsecase) {
        this.loanRuleCommandMapper = loanRuleCommandMapper;
        this.createLoanRuleUsecase = createLoanRuleUsecase;
    }

    public LoanRuleResponse handle(CreateLoanRuleCommand command) {
        MorabeheLoanRule morabeheLoanRule = loanRuleCommandMapper.mapToAggregateRoot(command);
        MorabeheLoanRule savedLoanRule = createLoanRuleUsecase.create(morabeheLoanRule);
        return loanRuleCommandMapper.mapToResponse(savedLoanRule);
    }

}
