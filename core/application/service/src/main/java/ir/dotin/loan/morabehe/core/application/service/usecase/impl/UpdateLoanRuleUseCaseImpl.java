package ir.dotin.loan.morabehe.core.application.service.usecase.impl;

import ir.dotin.loan.morabehe.core.application.ports.outbound.persistence.MorabeheLoanRulePersistencePort;
import ir.dotin.loan.morabehe.core.application.service.assembler.MorabeheLoanRuleAssembler;
import ir.dotin.loan.morabehe.core.application.service.command.MorabeheUpdateLoanRuleCommand;
import ir.dotin.loan.morabehe.core.application.service.response.LoanRuleResponse;
import ir.dotin.loan.morabehe.core.application.service.usecase.UpdateLoanRuleUseCase;
import ir.dotin.loan.morabehe.core.domain.config.entity.loanrule.MorabeheLoanRule;
import ir.dotin.loan.morabehe.core.domain.config.service.MorabeheLoanRuleUpdateService;
import ir.dotin.loan.morabehe.core.domain.config.valueobject.MorabeheLoanRuleId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UpdateLoanRuleUseCaseImpl implements UpdateLoanRuleUseCase {

    private static final Logger logger = LoggerFactory.getLogger(UpdateLoanRuleUseCaseImpl.class);

    private final MorabeheLoanRulePersistencePort persistencePort;
    private final MorabeheLoanRuleUpdateService morabeheLoanRuleUpdateService;
    private final MorabeheLoanRuleAssembler assembler;

    public UpdateLoanRuleUseCaseImpl(MorabeheLoanRulePersistencePort persistencePort,
                                     @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
                                     MorabeheLoanRuleUpdateService morabeheLoanRuleUpdateService,
                                     MorabeheLoanRuleAssembler assembler) {
        this.persistencePort = persistencePort;
        this.morabeheLoanRuleUpdateService = morabeheLoanRuleUpdateService;
        this.assembler = assembler;
    }


    @Override
    public LoanRuleResponse execute(MorabeheUpdateLoanRuleCommand command) {
        logger.debug("Executing UpdateLoanRuleUseCase with command: {}", command);

        MorabeheLoanRule newMorabeheLoanRule = assembler.mapToAggregateRoot(command);

        MorabeheLoanRuleId morabeheLoanRuleId = new MorabeheLoanRuleId(command.loanRuleId());

        MorabeheLoanRule oldMorabeheLoanRule = persistencePort
                .findById(morabeheLoanRuleId)
                .orElseThrow(() -> new IllegalArgumentException("MorabeheLoanRule not found"));

        morabeheLoanRuleUpdateService.update(oldMorabeheLoanRule, newMorabeheLoanRule);

        persistencePort.update(oldMorabeheLoanRule);

        persistencePort.save(newMorabeheLoanRule);
        // TODO: Publish Event
        return assembler.mapToResponse(newMorabeheLoanRule);
    }

    @Override
    public LoanRuleResponse compensate(MorabeheUpdateLoanRuleCommand command) {
        // TODO
        return null;
    }
}
