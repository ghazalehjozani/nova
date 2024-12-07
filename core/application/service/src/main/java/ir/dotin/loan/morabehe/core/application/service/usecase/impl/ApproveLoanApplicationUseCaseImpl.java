package ir.dotin.loan.morabehe.core.application.service.usecase.impl;

import ir.dotin.loan.baseloan.domain.loanapplication.valueobject.Sanction;
import ir.dotin.loan.morabehe.core.application.ports.outbound.client.MorabeheSanctionClientPort;
import ir.dotin.loan.morabehe.core.application.ports.outbound.persistence.MorabeheLoanApplicationPersistencePort;
import ir.dotin.loan.morabehe.core.application.ports.outbound.persistence.MorabeheLoanRulePersistencePort;
import ir.dotin.loan.morabehe.core.application.service.assembler.MorabeheLoanApplicationAssembler;
import ir.dotin.loan.morabehe.core.application.service.command.MorabeheApproveLoanApplicationCommand;
import ir.dotin.loan.morabehe.core.application.service.exception.LoanApplicationNotFoundException;
import ir.dotin.loan.morabehe.core.application.service.exception.LoanRuleNotFoundException;
import ir.dotin.loan.morabehe.core.application.service.response.LoanApplicationResponse;
import ir.dotin.loan.morabehe.core.application.service.usecase.ApproveLoanApplicationUseCase;
import ir.dotin.loan.morabehe.core.domain.config.entity.loanrule.MorabeheLoanRule;
import ir.dotin.loan.morabehe.core.domain.loanapplication.entity.application.LoanApplication;
import ir.dotin.loan.morabehe.core.domain.loanapplication.entity.application.MorabeheLoanApplication;
import ir.dotin.loan.morabehe.core.domain.loanapplication.service.ApproveLoanApplicationService;
import ir.dotin.loan.morabehe.core.domain.loanapplication.valueobject.MorabeheLoanApplicationId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class ApproveLoanApplicationUseCaseImpl implements ApproveLoanApplicationUseCase {

    private static final Logger logger = LoggerFactory.getLogger(ApproveLoanApplicationUseCaseImpl.class);

    private final MorabeheLoanApplicationPersistencePort loanApplicationPersistencePort;
    private final ApproveLoanApplicationService approveService;
    private final MorabeheLoanRulePersistencePort loanRulePersistencePort;
    private final MorabeheSanctionClientPort sanctionClientPort;
    private final MorabeheLoanApplicationAssembler assembler;

    public ApproveLoanApplicationUseCaseImpl(
            MorabeheLoanApplicationPersistencePort loanApplicationPersistencePort,
            @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
            ApproveLoanApplicationService approveService,
            MorabeheLoanRulePersistencePort loanRulePersistencePort,
            MorabeheSanctionClientPort sanctionClientPort,
            MorabeheLoanApplicationAssembler assembler) {
        this.loanApplicationPersistencePort = loanApplicationPersistencePort;
        this.approveService = approveService;
        this.loanRulePersistencePort = loanRulePersistencePort;
        this.sanctionClientPort = sanctionClientPort;
        this.assembler = assembler;
    }

    @Override
    @Transactional
    public LoanApplicationResponse execute(MorabeheApproveLoanApplicationCommand command) {
        logger.debug("Executing ApproveLoanApplicationUseCase with command: {}", command);

        var applicationId = new MorabeheLoanApplicationId(command.loanApplication().loanApplicationId());
        MorabeheLoanApplication existingApplication = loanApplicationPersistencePort
                .findById(applicationId)
                .orElseThrow(() -> new LoanApplicationNotFoundException(applicationId));

        MorabeheLoanApplication morabeheLoanApplication = assembler.mapToAggregateRoot(command, existingApplication);

        LoanApplication loanApplication = morabeheLoanApplication.getLoanApplication();
        MorabeheLoanRule loanRule = loanRulePersistencePort
                .findById(loanApplication.getLoanRuleId())
                .orElseThrow(() -> new LoanRuleNotFoundException(loanApplication.getLoanRuleId()));

        Sanction sanction = sanctionClientPort.getBySerial(loanApplication.getSanctionSerial());

        approveService.approve(morabeheLoanApplication, loanRule, sanction);

        loanApplicationPersistencePort.update(morabeheLoanApplication);

        return assembler.mapToResponse(morabeheLoanApplication);
    }

    @Override
    public LoanApplicationResponse compensate(MorabeheApproveLoanApplicationCommand command) {
        // TODO
        return null;
    }

}
