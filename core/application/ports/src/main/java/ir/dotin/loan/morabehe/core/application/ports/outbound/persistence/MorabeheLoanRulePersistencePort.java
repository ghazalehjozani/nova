package ir.dotin.loan.morabehe.core.application.ports.outbound.persistence;

import java.util.Optional;

import ir.dotin.loan.baseloan.domain.config.valueobject.LoanRuleCode;
import ir.dotin.loan.morabehe.core.domain.config.entity.loanrule.MorabeheLoanRule;
import ir.dotin.loan.morabehe.core.domain.config.valueobject.MorabeheLoanRuleId;

public interface MorabeheLoanRulePersistencePort {

    void save(MorabeheLoanRule rule);

    void update(MorabeheLoanRule newRule);

    Optional<MorabeheLoanRule> findById(MorabeheLoanRuleId id);

    boolean existsByCode(LoanRuleCode code);

    boolean existsByIdAndEnable(MorabeheLoanRuleId id);
}
