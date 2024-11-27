package ir.dotin.loan.morabehe.core.application.ports.secondary;

import ir.dotin.loan.baseloan.domain.config.valueobject.LoanRuleCode;
import ir.dotin.loan.baseloan.domain.loanapplication.valueobject.LoanTypeCode;
import ir.dotin.loan.morabehe.core.domain.config.entity.loanrule.MorabeheLoanRule;
import ir.dotin.loan.morabehe.core.domain.config.entity.loantype.MorabeheLoanType;
import ir.dotin.loan.morabehe.core.domain.config.valueobject.MorabeheLoanRuleId;
import ir.dotin.loan.morabehe.core.domain.config.valueobject.MorabeheLoanTypeId;

import java.util.Optional;

public interface MorabeheLoanTypePersistencePort {

    void save(MorabeheLoanType rule);

    Optional<MorabeheLoanType> findById(MorabeheLoanTypeId id);

    boolean existsByCode(LoanTypeCode code);
}
