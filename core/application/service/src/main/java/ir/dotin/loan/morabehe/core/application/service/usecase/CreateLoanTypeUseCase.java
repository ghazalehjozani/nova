package ir.dotin.loan.morabehe.core.application.service.usecase;

import ir.dotin.loan.morabehe.core.application.service.command.MorabeheCreateLoanTypeCommand;
import ir.dotin.loan.morabehe.core.application.service.response.LoanTypeResponse;

public interface CreateLoanTypeUseCase {

    LoanTypeResponse execute(MorabeheCreateLoanTypeCommand command);

    LoanTypeResponse compensate(MorabeheCreateLoanTypeCommand command);
}
