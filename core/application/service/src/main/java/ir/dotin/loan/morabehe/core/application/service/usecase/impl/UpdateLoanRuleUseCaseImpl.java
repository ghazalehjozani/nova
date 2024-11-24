package ir.dotin.loan.morabehe.core.application.service.usecase.impl;

import ir.dotin.loan.morabehe.core.application.ports.secondary.MorabeheLoanRulePersistencePort;
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

    public UpdateLoanRuleUseCaseImpl(MorabeheLoanRulePersistencePort persistencePort, MorabeheLoanRuleUpdateService morabeheLoanRuleUpdateService) {
        this.persistencePort = persistencePort;
        this.morabeheLoanRuleUpdateService = morabeheLoanRuleUpdateService;
    }


    @Override
    public MorabeheLoanRule update(UUID loanRuleId, MorabeheLoanRule newMorabeheLoanRule) {
        MorabeheLoanRule oldMorabeheLoanRule = persistencePort.findById(new MorabeheLoanRuleId(loanRuleId))
                .orElseThrow(() -> new IllegalArgumentException("MorabeheLoanRule not found"));
        morabeheLoanRuleUpdateService.update(newMorabeheLoanRule, oldMorabeheLoanRule);
        persistencePort.save(newMorabeheLoanRule);
        // TODO: Publish Event
        return newMorabeheLoanRule;
    }
}
