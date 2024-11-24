package ir.dotin.loan.morabehe.core.application.service.handler;

import ir.dotin.loan.morabehe.core.application.service.command.CreateLoanRuleCommand;
import ir.dotin.loan.morabehe.core.application.service.mapper.CreateLoanRuleCommandMapper;
import ir.dotin.loan.morabehe.core.application.service.response.LoanRuleResponse;
import ir.dotin.loan.morabehe.core.application.service.usecase.CreateLoanRuleUsecase;
import ir.dotin.loan.morabehe.core.domain.config.entity.loanrule.MorabeheLoanRule;
import org.springframework.stereotype.Component;

@Component
public class CreateLoanRuleHandler {

    private final CreateLoanRuleCommandMapper createLoanRuleCommandMapper;
    private final CreateLoanRuleUsecase createLoanRuleUsecase;

    public CreateLoanRuleHandler(CreateLoanRuleCommandMapper createLoanRuleCommandMapper,
                                 CreateLoanRuleUsecase createLoanRuleUsecase) {
        this.createLoanRuleCommandMapper = createLoanRuleCommandMapper;
        this.createLoanRuleUsecase = createLoanRuleUsecase;
    }

    public LoanRuleResponse handle(CreateLoanRuleCommand command) {
        MorabeheLoanRule morabeheLoanRule = createLoanRuleCommandMapper.mapToAggregateRoot(command);
        MorabeheLoanRule savedLoanRule = createLoanRuleUsecase.create(morabeheLoanRule);
        return createLoanRuleCommandMapper.mapToResponse(savedLoanRule);
    }

}
