package ir.dotin.loan.morabehe.core.application.service.usecase;

import ir.dotin.loan.morabehe.core.application.service.command.MorabeheUpdateLoanRuleCommand;
import ir.dotin.loan.morabehe.core.application.service.response.LoanRuleResponse;

public interface UpdateLoanRuleUseCase {

    LoanRuleResponse execute(MorabeheUpdateLoanRuleCommand command);

    LoanRuleResponse compensate(MorabeheUpdateLoanRuleCommand command);
}
