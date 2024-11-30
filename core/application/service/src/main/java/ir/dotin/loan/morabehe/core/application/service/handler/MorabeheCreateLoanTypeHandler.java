package ir.dotin.loan.morabehe.core.application.service.handler;


import ir.dotin.loan.morabehe.core.application.service.command.MorabeheCreateLoanTypeCommand;
import ir.dotin.loan.morabehe.core.application.service.mapper.MorabeheLoanTypeCommandMapper;
import ir.dotin.loan.morabehe.core.application.service.response.LoanTypeResponse;
import ir.dotin.loan.morabehe.core.application.service.usecase.CreateLoanTypeUseCase;
import ir.dotin.loan.morabehe.core.domain.config.entity.loantype.MorabeheLoanType;
import org.springframework.stereotype.Component;

@Component
public class MorabeheCreateLoanTypeHandler {

    private final MorabeheLoanTypeCommandMapper loanTypeCommandMapper;
    private final CreateLoanTypeUseCase createLoanTypeUseCase;

    public MorabeheCreateLoanTypeHandler(MorabeheLoanTypeCommandMapper loanTypeCommandMapper, CreateLoanTypeUseCase createLoanTypeUseCase) {
        this.loanTypeCommandMapper = loanTypeCommandMapper;
        this.createLoanTypeUseCase = createLoanTypeUseCase;
    }


    public LoanTypeResponse handle(MorabeheCreateLoanTypeCommand command) {
        MorabeheLoanType morabeheLoanType = loanTypeCommandMapper.mapToAggregateRoot(command);
        MorabeheLoanType saveLoanType = createLoanTypeUseCase.create(morabeheLoanType);
        return loanTypeCommandMapper.mapToResponse(saveLoanType);
    }
}
