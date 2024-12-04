package ir.dotin.loan.morabehe.core.application.service.handler;

import ir.dotin.loan.morabehe.core.application.service.assembler.MorabeheLoanRuleAssembler;
import ir.dotin.loan.morabehe.core.application.service.command.MorabeheCreateLoanRuleCommand;
import ir.dotin.loan.morabehe.core.application.service.response.LoanRuleResponse;
import ir.dotin.loan.morabehe.core.application.service.usecase.CreateLoanRuleUseCase;
import ir.dotin.loan.morabehe.core.domain.config.entity.loanrule.MorabeheLoanRule;
import org.springframework.stereotype.Component;

@Component
public class MorabeheCreateLoanRuleHandler {

    private final MorabeheLoanRuleAssembler loanRuleCommandMapper;
    private final CreateLoanRuleUseCase createLoanRuleUsecase;

    public MorabeheCreateLoanRuleHandler(MorabeheLoanRuleAssembler loanRuleCommandMapper,
                                         CreateLoanRuleUseCase createLoanRuleUsecase) {
        this.loanRuleCommandMapper = loanRuleCommandMapper;
        this.createLoanRuleUsecase = createLoanRuleUsecase;
    }

    public LoanRuleResponse handle(MorabeheCreateLoanRuleCommand command) {
        MorabeheLoanRule morabeheLoanRule = loanRuleCommandMapper.mapToAggregateRoot(command);
        MorabeheLoanRule savedLoanRule = createLoanRuleUsecase.create(morabeheLoanRule);
        return loanRuleCommandMapper.mapToResponse(savedLoanRule);
    }

}
