package ir.dotin.loan.morabehe.core.application.ports.secondary;

import ir.dotin.loan.baseloan.domain.config.valueobject.LoanRuleId;
import ir.dotin.loan.baseloan.domain.config.valueobject.LoanTypeId;
import ir.dotin.loan.baseloan.domain.loanapplication.valueobject.LoanTypeCode;
import ir.dotin.loan.morabehe.core.domain.config.entity.loantype.MorabeheLoanType;
import java.util.Optional;

public interface MorabeheLoanTypePersistencePort {

    void save(MorabeheLoanType rule);

    Optional<MorabeheLoanType> findById(LoanTypeId id);

    Optional<MorabeheLoanType> findByIdAndLoanRuleId(LoanTypeId id, LoanRuleId loanRuleId);

    boolean existsByCode(LoanTypeCode code);
}
