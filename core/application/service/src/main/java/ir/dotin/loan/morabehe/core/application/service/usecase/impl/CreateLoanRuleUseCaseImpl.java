package ir.dotin.loan.morabehe.core.application.service.usecase.impl;

import ir.dotin.loan.morabehe.core.application.ports.secondary.MorabeheLoanRulePersistencePort;
import ir.dotin.loan.morabehe.core.application.service.usecase.CreateLoanRuleUseCase;
import ir.dotin.loan.morabehe.core.domain.config.entity.loanrule.MorabeheLoanRule;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CreateLoanRuleUseCaseImpl implements CreateLoanRuleUseCase {

    private final MorabeheLoanRulePersistencePort persistencePort;

    public CreateLoanRuleUseCaseImpl(MorabeheLoanRulePersistencePort persistencePort) {
        this.persistencePort = persistencePort;
    }


    @Override
    public MorabeheLoanRule create(MorabeheLoanRule loanRule) {
        loanRule.createLoanRule();
        persistencePort.save(loanRule);
        // TODO: Publish Event
        return loanRule;
    }
}
