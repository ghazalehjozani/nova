package ir.dotin.loan.morabehe.core.application.ports.secondary;

import ir.dotin.loan.morabehe.core.domain.config.entity.loanrule.MorabeheLoanRule;
import ir.dotin.loan.morabehe.core.domain.config.valueobject.MorabeheLoanRuleId;

import java.util.Optional;
import java.util.UUID;

public interface MorabeheLoanRulePersistencePort {

    void save(MorabeheLoanRule rule);

    Optional<MorabeheLoanRule> findById(MorabeheLoanRuleId id);
}
