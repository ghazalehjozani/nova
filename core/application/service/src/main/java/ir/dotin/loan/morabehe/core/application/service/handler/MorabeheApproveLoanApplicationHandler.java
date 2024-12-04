package ir.dotin.loan.morabehe.core.application.service.handler;

import ir.dotin.loan.morabehe.core.application.ports.outbound.persistence.MorabeheLoanApplicationPersistencePort;
import ir.dotin.loan.morabehe.core.application.service.assembler.MorabeheLoanApplicationAssembler;
import ir.dotin.loan.morabehe.core.application.service.command.MorabeheApproveLoanApplicationCommand;
import ir.dotin.loan.morabehe.core.application.service.response.LoanApplicationResponse;
import ir.dotin.loan.morabehe.core.application.service.usecase.ApproveLoanApplicationUseCase;
import ir.dotin.loan.morabehe.core.domain.loanapplication.entity.application.MorabeheLoanApplication;
import ir.dotin.loan.morabehe.core.domain.loanapplication.valueobject.MorabeheLoanApplicationId;
import org.springframework.stereotype.Component;

@Component
public class MorabeheApproveLoanApplicationHandler {

    private final MorabeheLoanApplicationAssembler loanApplicationCommandMapper;
    private final ApproveLoanApplicationUseCase approveLoanApplicationUseCase;
    private final MorabeheLoanApplicationPersistencePort morabeheLoanApplicationPersistencePort;

    public MorabeheApproveLoanApplicationHandler(MorabeheLoanApplicationAssembler loanApplicationCommandMapper, ApproveLoanApplicationUseCase approveLoanApplicationUseCase, MorabeheLoanApplicationPersistencePort morabeheLoanApplicationPersistencePort) {
        this.loanApplicationCommandMapper = loanApplicationCommandMapper;
        this.approveLoanApplicationUseCase = approveLoanApplicationUseCase;
        this.morabeheLoanApplicationPersistencePort = morabeheLoanApplicationPersistencePort;
    }

    public LoanApplicationResponse handle(MorabeheApproveLoanApplicationCommand command) {
        MorabeheLoanApplication oldMorabeheLoanApplication = morabeheLoanApplicationPersistencePort.findById(
                        new MorabeheLoanApplicationId(command.loanApplication().loanApplicationId()))
                .orElseThrow(() -> new RuntimeException("Loan Application not found"));
        MorabeheLoanApplication morabeheLoanApplication = loanApplicationCommandMapper
                .mapToAggregateRoot(command, oldMorabeheLoanApplication);
        MorabeheLoanApplication savedLoanApplication = approveLoanApplicationUseCase.approve(oldMorabeheLoanApplication,
                morabeheLoanApplication.getLoanApplication().getSanctionSerial());
        return loanApplicationCommandMapper.mapToResponse(savedLoanApplication);
    }
}
