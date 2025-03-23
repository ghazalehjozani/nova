package ir.dotin.loan.morabehe.core.application.service.usecase.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ir.dotin.loan.morabehe.core.application.ports.outbound.persistence.MorabeheLoanRulePersistencePort;
import ir.dotin.loan.morabehe.core.application.service.assembler.MorabeheLoanRuleAssembler;
import ir.dotin.loan.morabehe.core.application.service.command.MorabeheUpdateLoanRuleCommand;
import ir.dotin.loan.morabehe.core.application.service.exception.LoanRuleNotFoundException;
import ir.dotin.loan.morabehe.core.application.service.response.LoanRuleResponse;
import ir.dotin.loan.morabehe.core.application.service.usecase.UpdateLoanRuleUseCase;
import ir.dotin.loan.morabehe.core.domain.config.entity.loanrule.MorabeheLoanRule;
import ir.dotin.loan.morabehe.core.domain.config.service.MorabeheLoanRuleUpdateService;
import ir.dotin.loan.morabehe.core.domain.config.valueobject.MorabeheLoanRuleId;

@Service
@Transactional
public class UpdateLoanRuleUseCaseImpl implements UpdateLoanRuleUseCase {

    private static final Logger logger = LoggerFactory.getLogger(UpdateLoanRuleUseCaseImpl.class);

    private final MorabeheLoanRulePersistencePort persistencePort;
    private final MorabeheLoanRuleUpdateService updateService;
    private final MorabeheLoanRuleAssembler assembler;

    public UpdateLoanRuleUseCaseImpl(
            final MorabeheLoanRulePersistencePort persistencePort,
            @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
                    final MorabeheLoanRuleUpdateService updateService,
            final MorabeheLoanRuleAssembler assembler) {
        this.persistencePort = persistencePort;
        this.updateService = updateService;
        this.assembler = assembler;
    }

    @Override
    public LoanRuleResponse execute(final MorabeheUpdateLoanRuleCommand command) {
        logger.debug("Executing UpdateLoanRuleUseCase with command: {}", command);

        final MorabeheLoanRule updatedRule = assembler.mapToAggregateRoot(command);

        final MorabeheLoanRuleId ruleId =
                new MorabeheLoanRuleId(command.loanRule().loanRuleId());
        final MorabeheLoanRule oldRule =
                persistencePort.findById(ruleId).orElseThrow(() -> new LoanRuleNotFoundException(ruleId));

        updateService.update(oldRule, updatedRule);

        persistencePort.update(oldRule);

        persistencePort.save(updatedRule);

        // TODO: Publish Event

        return assembler.mapToResponse(updatedRule);
    }

    @Override
    public LoanRuleResponse compensate(MorabeheUpdateLoanRuleCommand command) {
        // TODO
        return null;
    }
}
