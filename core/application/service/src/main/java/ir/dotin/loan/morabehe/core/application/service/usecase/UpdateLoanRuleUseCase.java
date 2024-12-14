package ir.dotin.loan.morabehe.core.application.service.usecase;

import ir.dotin.loan.baseloan.core.application.service.usecase.UseCase;
import ir.dotin.loan.morabehe.core.application.service.command.MorabeheUpdateLoanRuleCommand;
import ir.dotin.loan.morabehe.core.application.service.response.LoanRuleResponse;

public interface UpdateLoanRuleUseCase extends UseCase<MorabeheUpdateLoanRuleCommand, LoanRuleResponse> {

}
