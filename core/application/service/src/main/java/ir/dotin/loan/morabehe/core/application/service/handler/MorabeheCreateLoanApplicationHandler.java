package ir.dotin.loan.morabehe.core.application.service.handler;

import ir.dotin.loan.morabehe.core.application.service.command.MorabeheCreateLoanApplicationCommand;
import ir.dotin.loan.morabehe.core.application.service.mapper.MorabeheLoanApplicationCommandMapper;
import ir.dotin.loan.morabehe.core.application.service.response.LoanApplicationResponse;
import ir.dotin.loan.morabehe.core.application.service.usecase.CreateLoanApplicationUseCase;
import ir.dotin.loan.morabehe.core.domain.loanapplication.entity.application.MorabeheLoanApplication;
import org.springframework.stereotype.Component;

@Component
public class MorabeheCreateLoanApplicationHandler {

    private final MorabeheLoanApplicationCommandMapper loanApplicationCommandMapper;
    private final CreateLoanApplicationUseCase createLoanApplicationUseCase;

    public MorabeheCreateLoanApplicationHandler(
            MorabeheLoanApplicationCommandMapper loanApplicationCommandMapper,
            CreateLoanApplicationUseCase createLoanApplicationUseCase) {
        this.loanApplicationCommandMapper = loanApplicationCommandMapper;
        this.createLoanApplicationUseCase = createLoanApplicationUseCase;
    }

    public LoanApplicationResponse handle(MorabeheCreateLoanApplicationCommand command) {
        MorabeheLoanApplication morabeheLoanRule = loanApplicationCommandMapper
                .mapToAggregateRoot(command);
        MorabeheLoanApplication savedLoanRule = createLoanApplicationUseCase.create(morabeheLoanRule);
        return loanApplicationCommandMapper.mapToResponse(savedLoanRule);
    }

}
