package ir.dotin.loan.morabehe.core.application.service.usecase.impl;

import ir.dotin.loan.morabehe.core.application.ports.secondary.persistence.MorabeheLoanRulePersistencePort;
import ir.dotin.loan.morabehe.core.application.service.usecase.UpdateLoanRuleUseCase;
import ir.dotin.loan.morabehe.core.domain.config.entity.loanrule.MorabeheLoanRule;
import ir.dotin.loan.morabehe.core.domain.config.service.MorabeheLoanRuleUpdateService;
import ir.dotin.loan.morabehe.core.domain.config.valueobject.MorabeheLoanRuleId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class UpdateLoanRuleUseCaseImpl implements UpdateLoanRuleUseCase {

    private final MorabeheLoanRulePersistencePort persistencePort;
    private final MorabeheLoanRuleUpdateService morabeheLoanRuleUpdateService;

    public UpdateLoanRuleUseCaseImpl(MorabeheLoanRulePersistencePort persistencePort,
                                     MorabeheLoanRuleUpdateService morabeheLoanRuleUpdateService) {
        this.persistencePort = persistencePort;
        this.morabeheLoanRuleUpdateService = morabeheLoanRuleUpdateService;
    }


    @Override
    public MorabeheLoanRule update(UUID loanRuleId, MorabeheLoanRule newMorabeheLoanRule) {
        MorabeheLoanRuleId morabeheLoanRuleId = new MorabeheLoanRuleId(loanRuleId);
        MorabeheLoanRule oldMorabeheLoanRule = persistencePort
                .findById(morabeheLoanRuleId)
                .orElseThrow(() -> new IllegalArgumentException("MorabeheLoanRule not found"));
        morabeheLoanRuleUpdateService.update(oldMorabeheLoanRule, newMorabeheLoanRule);
        persistencePort.update(oldMorabeheLoanRule);
        persistencePort.save(newMorabeheLoanRule);
        // TODO: Publish Event
        return newMorabeheLoanRule;
    }
}
