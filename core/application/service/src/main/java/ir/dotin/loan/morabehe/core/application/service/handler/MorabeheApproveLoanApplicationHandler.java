package ir.dotin.loan.morabehe.core.application.service.handler;

import ir.dotin.loan.morabehe.core.application.service.command.MorabeheApproveLoanApplicationCommand;
import ir.dotin.loan.morabehe.core.application.service.mapper.MorabeheLoanApplicationCommandMapper;
import ir.dotin.loan.morabehe.core.application.service.response.LoanApplicationResponse;
import ir.dotin.loan.morabehe.core.application.service.usecase.ApproveLoanApplicationUseCase;
import ir.dotin.loan.morabehe.core.domain.loanapplication.entity.application.MorabeheLoanApplication;
import org.springframework.stereotype.Component;

@Component
public class MorabeheApproveLoanApplicationHandler {

    private final MorabeheLoanApplicationCommandMapper loanApplicationCommandMapper;
    private final ApproveLoanApplicationUseCase approveLoanApplicationUseCase;

    public MorabeheApproveLoanApplicationHandler(MorabeheLoanApplicationCommandMapper loanApplicationCommandMapper, ApproveLoanApplicationUseCase approveLoanApplicationUseCase) {
        this.loanApplicationCommandMapper = loanApplicationCommandMapper;
        this.approveLoanApplicationUseCase = approveLoanApplicationUseCase;
    }

    public LoanApplicationResponse handle(MorabeheApproveLoanApplicationCommand command) {
        MorabeheLoanApplication morabeheLoanApplication = loanApplicationCommandMapper
                .mapToAggregateRoot(command);
        MorabeheLoanApplication savedLoanApplication = approveLoanApplicationUseCase.approve(morabeheLoanApplication.getId(),
                morabeheLoanApplication.getLoanApplication().getSanctionSerial());
        return loanApplicationCommandMapper.mapToResponse(savedLoanApplication);
    }
}
