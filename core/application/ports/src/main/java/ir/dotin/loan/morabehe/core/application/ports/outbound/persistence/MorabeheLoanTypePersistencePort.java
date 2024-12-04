package ir.dotin.loan.morabehe.core.application.ports.outbound.persistence;

import ir.dotin.loan.baseloan.domain.loanapplication.valueobject.LoanTypeCode;
import ir.dotin.loan.morabehe.core.domain.config.entity.loantype.MorabeheLoanType;
import ir.dotin.loan.morabehe.core.domain.config.valueobject.MorabeheLoanRuleId;
import ir.dotin.loan.morabehe.core.domain.config.valueobject.MorabeheLoanTypeId;

import java.util.Optional;

public interface MorabeheLoanTypePersistencePort {

    void save(MorabeheLoanType rule);

    Optional<MorabeheLoanType> findById(MorabeheLoanTypeId id);

    Optional<MorabeheLoanType> findByIdAndLoanRuleId(MorabeheLoanTypeId id,
                                                     MorabeheLoanRuleId loanRuleId);

    boolean existsByCode(LoanTypeCode code);
}
