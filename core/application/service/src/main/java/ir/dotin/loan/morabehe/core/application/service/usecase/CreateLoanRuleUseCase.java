package ir.dotin.loan.morabehe.core.application.service.usecase;


import ir.dotin.loan.morabehe.core.application.service.command.MorabeheCreateLoanRuleCommand;
import ir.dotin.loan.morabehe.core.application.service.response.LoanRuleResponse;

public interface CreateLoanRuleUseCase {

    LoanRuleResponse execute(MorabeheCreateLoanRuleCommand command);
    LoanRuleResponse compensate(MorabeheCreateLoanRuleCommand command);
}
