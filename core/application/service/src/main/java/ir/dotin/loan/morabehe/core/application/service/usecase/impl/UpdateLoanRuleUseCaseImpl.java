package ir.dotin.loan.morabehe.core.application.service.usecase.impl;

import ir.dotin.loan.morabehe.core.application.ports.secondary.MorabeheLoanRulePersistencePort;
import ir.dotin.loan.morabehe.core.application.service.UpdateLoanRuleUseCase;
import ir.dotin.loan.morabehe.core.domain.config.entity.loanrule.MorabeheLoanRule;
import ir.dotin.loan.morabehe.core.domain.config.service.MorabeheLoanRuleUpdateService;

public class UpdateLoanRuleUseCaseImpl implements UpdateLoanRuleUseCase {

    private final MorabeheLoanRulePersistencePort persistencePort;
    private final MorabeheLoanRuleUpdateService morabeheLoanRuleUpdateService;

    public UpdateLoanRuleUseCaseImpl(MorabeheLoanRulePersistencePort persistencePort, MorabeheLoanRuleUpdateService morabeheLoanRuleUpdateService) {
        this.persistencePort = persistencePort;
        this.morabeheLoanRuleUpdateService = morabeheLoanRuleUpdateService;
    }


    @Override
    public String update(MorabeheLoanRule newMorabeheLoanRule, MorabeheLoanRule oldMorabeheLoanRule) {
        morabeheLoanRuleUpdateService.update(newMorabeheLoanRule, oldMorabeheLoanRule);
        persistencePort.save(newMorabeheLoanRule);
        return newMorabeheLoanRule.getId().toString();
    }
}
