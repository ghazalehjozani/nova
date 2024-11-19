package ir.dotin.loan.morabehe.core.application.handler;

import ir.dotin.loan.morabehe.core.application.command.CreateLoanRuleCommand;
import ir.dotin.loan.morabehe.core.application.mapper.LoanRuleCommandMapper;
import ir.dotin.loan.morabehe.core.application.service.CreateLoanRuleService;
import ir.dotin.loan.morabehe.core.domain.config.entity.loanrule.MorabeheLoanRule;
import ir.dotin.platform.ddd.application.common.command.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class CreateLoanRuleHandler implements CommandHandler<CreateLoanRuleCommand, String> {

    private final LoanRuleCommandMapper loanRuleCommandMapper;
    private final CreateLoanRuleService createLoanRuleService;

    public CreateLoanRuleHandler(LoanRuleCommandMapper loanRuleCommandMapper,
                                 CreateLoanRuleService createLoanRuleService) {
        this.loanRuleCommandMapper = loanRuleCommandMapper;
        this.createLoanRuleService = createLoanRuleService;
    }

    @Override
    public String handle(CreateLoanRuleCommand command) {
        MorabeheLoanRule morabeheLoanRule = loanRuleCommandMapper.mapToAggregateRoot(command);
        return createLoanRuleService.create(morabeheLoanRule);
    }
}
