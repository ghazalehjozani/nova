package ir.dotin.loan.morabehe.core.application.service.usecase;

import ir.dotin.loan.baseloan.core.application.service.usecase.UseCase;
import ir.dotin.loan.morabehe.core.application.service.command.MorabeheApproveLoanApplicationCommand;
import ir.dotin.loan.morabehe.core.application.service.response.LoanApplicationResponse;

public interface ApproveLoanApplicationUseCase
        extends UseCase<MorabeheApproveLoanApplicationCommand, LoanApplicationResponse> { // TODO: Move To BaseLoan
}
