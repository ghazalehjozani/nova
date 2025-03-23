package ir.dotin.loan.morabehe.core.application.service.usecase.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ir.dotin.loan.baseloan.core.application.ports.output.messaging.EventPublisherPort;
import ir.dotin.loan.baseloan.core.application.ports.output.persistence.JournalRepositoryPort;
import ir.dotin.loan.morabehe.core.application.ports.outbound.persistence.MorabeheLoanRulePersistencePort;
import ir.dotin.loan.morabehe.core.application.service.assembler.MorabeheLoanRuleAssembler;
import ir.dotin.loan.morabehe.core.application.service.command.MorabeheCreateLoanRuleCommand;
import ir.dotin.loan.morabehe.core.application.service.response.LoanRuleResponse;
import ir.dotin.loan.morabehe.core.application.service.usecase.CreateLoanRuleUseCase;
import ir.dotin.loan.morabehe.core.domain.config.entity.loanrule.MorabeheLoanRule;
import ir.dotin.loan.morabehe.core.domain.config.exception.MorabeheLoanRuleValidationException;

@Service
@Transactional
public class CreateLoanRuleUseCaseImpl implements CreateLoanRuleUseCase {

    private static final Logger logger = LoggerFactory.getLogger(CreateLoanRuleUseCaseImpl.class);

    private final MorabeheLoanRulePersistencePort persistencePort;
    private final JournalRepositoryPort journalRepositoryPort;
    private final EventPublisherPort journalPublisherPort;
    private final MorabeheLoanRuleAssembler assembler;

    public CreateLoanRuleUseCaseImpl(
            MorabeheLoanRulePersistencePort persistencePort,
            JournalRepositoryPort journalRepositoryPort,
            EventPublisherPort journalPublisherPort,
            MorabeheLoanRuleAssembler assembler) {
        this.persistencePort = persistencePort;
        this.journalRepositoryPort = journalRepositoryPort;
        this.journalPublisherPort = journalPublisherPort;
        this.assembler = assembler;
    }

    @Override
    public LoanRuleResponse execute(final MorabeheCreateLoanRuleCommand command) {
        logger.debug("Executing CreateLoanRuleUseCase with command: {}", command);

        final MorabeheLoanRule loanRule = assembler.mapToAggregateRoot(command);

        final boolean existsByCode =
                persistencePort.existsByCode(loanRule.getLoanRule().code());
        if (existsByCode) {
            throw new MorabeheLoanRuleValidationException("Duplicate loan rule code", "code");
        }

        loanRule.createLoanRule();

        persistencePort.save(loanRule);
        journalRepositoryPort.appendEvents(loanRule.domainEvents());

        journalPublisherPort.publishEvents(loanRule.domainEvents());
        return assembler.mapToResponse(loanRule);
    }

    @Override
    public LoanRuleResponse compensate(MorabeheCreateLoanRuleCommand command) {
        // TODO
        return null;
    }
}
