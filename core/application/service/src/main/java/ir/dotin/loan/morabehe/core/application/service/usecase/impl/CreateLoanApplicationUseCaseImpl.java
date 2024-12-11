package ir.dotin.loan.morabehe.core.application.service.usecase.impl;

import ir.dotin.loan.morabehe.core.application.ports.outbound.persistence.MorabeheLoanApplicationPersistencePort;
import ir.dotin.loan.morabehe.core.application.ports.outbound.persistence.MorabeheLoanRulePersistencePort;
import ir.dotin.loan.morabehe.core.application.ports.outbound.persistence.MorabeheLoanTypePersistencePort;
import ir.dotin.loan.morabehe.core.application.service.assembler.MorabeheLoanApplicationAssembler;
import ir.dotin.loan.morabehe.core.application.service.command.MorabeheCreateLoanApplicationCommand;
import ir.dotin.loan.morabehe.core.application.service.exception.LoanRuleNotFoundException;
import ir.dotin.loan.morabehe.core.application.service.exception.LoanTypeNotFoundException;
import ir.dotin.loan.morabehe.core.application.service.response.LoanApplicationResponse;
import ir.dotin.loan.morabehe.core.application.service.usecase.CreateLoanApplicationUseCase;
import ir.dotin.loan.morabehe.core.domain.config.entity.loanrule.MorabeheLoanRule;
import ir.dotin.loan.morabehe.core.domain.config.entity.loantype.MorabeheLoanType;
import ir.dotin.loan.morabehe.core.domain.loanapplication.entity.application.MorabeheLoanApplication;
import ir.dotin.loan.morabehe.core.domain.loanapplication.service.CreateLoanApplicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
public class CreateLoanApplicationUseCaseImpl implements CreateLoanApplicationUseCase {

    private static final Logger logger = LoggerFactory.getLogger(CreateLoanApplicationUseCaseImpl.class);

    private final MorabeheLoanApplicationPersistencePort loanApplicationPersistencePort;
    private final CreateLoanApplicationService createService;
    private final MorabeheLoanRulePersistencePort loanRulePersistencePort;
    private final MorabeheLoanTypePersistencePort loanTypePersistencePort;
    private final MorabeheLoanApplicationAssembler assembler;

    public CreateLoanApplicationUseCaseImpl(
            MorabeheLoanApplicationPersistencePort loanApplicationPersistencePort,
            @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
            CreateLoanApplicationService createService,
            MorabeheLoanRulePersistencePort loanRulePersistencePort,
            MorabeheLoanTypePersistencePort loanTypePersistencePort,
            MorabeheLoanApplicationAssembler assembler) {
        this.loanApplicationPersistencePort = loanApplicationPersistencePort;
        this.createService = createService;
        this.loanRulePersistencePort = loanRulePersistencePort;
        this.loanTypePersistencePort = loanTypePersistencePort;
        this.assembler = assembler;
    }

    @Override
    public LoanApplicationResponse execute(MorabeheCreateLoanApplicationCommand command) {
        logger.debug("Executing CreateLoanApplicationUseCase with command: {}", command);

        final MorabeheLoanApplication loanApplication = assembler.mapToAggregateRoot(command);

        final MorabeheLoanRule loanRule = loanRulePersistencePort
                .findById(loanApplication.getLoanApplication().getLoanRuleId())
                .orElseThrow(() -> new LoanRuleNotFoundException(loanApplication.getLoanApplication().getLoanRuleId()));

        final MorabeheLoanType loanType = loanTypePersistencePort
                .findById(loanApplication.getLoanApplication().getLoanTypeId())
                .orElseThrow(() -> new LoanTypeNotFoundException(loanApplication.getLoanApplication().getLoanTypeId()));

        createService.create(loanApplication, loanRule, loanType);//TODO: return

        loanApplicationPersistencePort.save(loanApplication);//TODO: Rename To Repository and remove Port

        return assembler.mapToResponse(loanApplication);
    }

    @Override
    public LoanApplicationResponse compensate(MorabeheCreateLoanApplicationCommand command) {
        // TODO
        return null;
    }

}
