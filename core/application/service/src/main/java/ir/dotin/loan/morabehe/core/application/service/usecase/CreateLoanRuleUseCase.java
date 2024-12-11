package ir.dotin.loan.morabehe.core.application.service.usecase;


import ir.dotin.loan.baseloan.application.service.usecase.UseCase;
import ir.dotin.loan.morabehe.core.application.service.command.MorabeheCreateLoanRuleCommand;
import ir.dotin.loan.morabehe.core.application.service.response.LoanRuleResponse;

public interface CreateLoanRuleUseCase extends UseCase<MorabeheCreateLoanRuleCommand, LoanRuleResponse> {

}
