package ir.dotin.loan.morabehe.core.application.ports.secondary;

import ir.dotin.loan.baseloan.domain.config.valueobject.LoanRuleCode;
import ir.dotin.loan.baseloan.domain.config.valueobject.LoanRuleId;
import ir.dotin.loan.morabehe.core.domain.config.entity.loanrule.MorabeheLoanRule;
import ir.dotin.loan.morabehe.core.domain.config.valueobject.MorabeheLoanRuleId;
import java.util.Optional;

public interface MorabeheLoanRulePersistencePort {

    void save(MorabeheLoanRule rule);

    void update(MorabeheLoanRule newRule);

    Optional<MorabeheLoanRule> findById(LoanRuleId id);

    boolean existsByCode(LoanRuleCode code);

    boolean existsByIdAndEnable(LoanRuleId id);

}
