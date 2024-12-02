package ir.dotin.loan.morabehe.core.application.service.usecase.impl;

import ir.dotin.loan.baseloan.domain.loanapplication.valueobject.Sanction;
import ir.dotin.loan.baseloan.domain.loanapplication.valueobject.SanctionSerial;
import ir.dotin.loan.morabehe.core.application.ports.secondary.client.MorabeheSanctionClientPort;
import ir.dotin.loan.morabehe.core.application.ports.secondary.persistence.MorabeheLoanApplicationPersistencePort;
import ir.dotin.loan.morabehe.core.application.ports.secondary.persistence.MorabeheLoanRulePersistencePort;
import ir.dotin.loan.morabehe.core.application.service.usecase.ApproveLoanApplicationUseCase;
import ir.dotin.loan.morabehe.core.domain.config.entity.loanrule.MorabeheLoanRule;
import ir.dotin.loan.morabehe.core.domain.loanapplication.entity.application.MorabeheLoanApplication;
import ir.dotin.loan.morabehe.core.domain.loanapplication.service.ApproveLoanApplicationService;
import ir.dotin.loan.morabehe.core.domain.loanapplication.valueobject.MorabeheLoanApplicationId;
import org.springframework.stereotype.Service;

@Service
public class ApproveLoanApplicationUseCaseImpl implements ApproveLoanApplicationUseCase {

    private final MorabeheLoanApplicationPersistencePort morabeheLoanApplicationPersistencePort;
    private final ApproveLoanApplicationService approveLoanApplicationService;
    private final MorabeheLoanRulePersistencePort morabeheLoanRulePersistencePort;
    private final MorabeheSanctionClientPort morabeheSanctionClientPort;

    public ApproveLoanApplicationUseCaseImpl(
            MorabeheLoanApplicationPersistencePort morabeheLoanApplicationPersistencePort,
            ApproveLoanApplicationService approveLoanApplicationService,
            MorabeheLoanRulePersistencePort morabeheLoanRulePersistencePort,
            MorabeheSanctionClientPort morabeheSanctionClientPort) {
        this.morabeheLoanApplicationPersistencePort = morabeheLoanApplicationPersistencePort;
        this.approveLoanApplicationService = approveLoanApplicationService;
        this.morabeheLoanRulePersistencePort = morabeheLoanRulePersistencePort;
        this.morabeheSanctionClientPort = morabeheSanctionClientPort;
    }

    @Override
    public MorabeheLoanApplication approve(MorabeheLoanApplicationId loanApplicationId, SanctionSerial serial) {

        MorabeheLoanApplication morabeheLoanApplication = morabeheLoanApplicationPersistencePort.findById(
                        loanApplicationId)
                .orElseThrow(() -> new RuntimeException("Loan Application not found"));
        MorabeheLoanRule morabeheLoanRule = morabeheLoanRulePersistencePort.findById(
                        morabeheLoanApplication.getLoanApplication().getLoanRuleId())
                .orElseThrow(() -> new RuntimeException("Loan Rule not found"));
        Sanction morabeheSanction = morabeheSanctionClientPort.getBySerial(serial);
        approveLoanApplicationService.approved(morabeheLoanApplication, morabeheLoanRule, morabeheSanction);
        morabeheLoanApplicationPersistencePort.save(morabeheLoanApplication);
        return morabeheLoanApplication;
    }
}
