package ir.dotin.loan.morabehe.core.application.ports.secondary;

import ir.dotin.loan.baseloan.domain.config.valueobject.LoanRuleCode;
import ir.dotin.loan.morabehe.core.domain.config.entity.loanrule.MorabeheLoanRule;
import ir.dotin.loan.morabehe.core.domain.config.valueobject.MorabeheLoanRuleId;
import java.util.Optional;

public interface MorabeheLoanRulePersistencePort {

    void save(MorabeheLoanRule rule);

    Optional<MorabeheLoanRule> findById(MorabeheLoanRuleId id);

    boolean existsByCode(LoanRuleCode code);

}
