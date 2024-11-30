package ir.dotin.loan.morabehe.core.application.service.usecase.impl;

import ir.dotin.loan.morabehe.core.application.ports.secondary.MorabeheLoanApplicationPersistencePort;
import ir.dotin.loan.morabehe.core.application.ports.secondary.MorabeheLoanRulePersistencePort;
import ir.dotin.loan.morabehe.core.application.ports.secondary.MorabeheLoanTypePersistencePort;
import ir.dotin.loan.morabehe.core.application.service.usecase.CreateLoanApplicationUseCase;
import ir.dotin.loan.morabehe.core.domain.config.entity.loanrule.MorabeheLoanRule;
import ir.dotin.loan.morabehe.core.domain.config.entity.loantype.MorabeheLoanType;
import ir.dotin.loan.morabehe.core.domain.loanapplication.entity.application.MorabeheLoanApplication;
import ir.dotin.loan.morabehe.core.domain.loanapplication.service.CreateLoanApplicationService;
import org.springframework.stereotype.Service;

@Service
public class CreateLoanApplicationUseCaseImpl implements CreateLoanApplicationUseCase {


    private final MorabeheLoanApplicationPersistencePort morabeheLoanApplicationPersistencePort;
    private final CreateLoanApplicationService createLoanApplicationService;
    private final MorabeheLoanRulePersistencePort morabeheLoanRulePersistencePort;
    private final MorabeheLoanTypePersistencePort morabeheLoanTypePersistencePort;

    public CreateLoanApplicationUseCaseImpl(
            MorabeheLoanApplicationPersistencePort morabeheLoanApplicationPersistencePort,
            CreateLoanApplicationService createLoanApplicationService,
            MorabeheLoanRulePersistencePort morabeheLoanRulePersistencePort,
            MorabeheLoanTypePersistencePort morabeheLoanTypePersistencePort) {
        this.morabeheLoanApplicationPersistencePort = morabeheLoanApplicationPersistencePort;
        this.createLoanApplicationService = createLoanApplicationService;
        this.morabeheLoanRulePersistencePort = morabeheLoanRulePersistencePort;
        this.morabeheLoanTypePersistencePort = morabeheLoanTypePersistencePort;
    }

    @Override
    public MorabeheLoanApplication create(MorabeheLoanApplication loanApplication) {
        MorabeheLoanRule loanRule = morabeheLoanRulePersistencePort.findById(
                        loanApplication.getLoanApplication()
                                .getLoanRuleId())
                .orElseThrow(() -> new RuntimeException("Loan Rule not found"));

        MorabeheLoanType loanType = morabeheLoanTypePersistencePort.findById(
                        loanApplication.getLoanApplication()
                                .getLoanTypeId())
                .orElseThrow(() -> new RuntimeException("Loan Type not found"));
        createLoanApplicationService.create(loanApplication, loanRule, loanType);
        morabeheLoanApplicationPersistencePort.save(loanApplication);
        return loanApplication;
    }

}
