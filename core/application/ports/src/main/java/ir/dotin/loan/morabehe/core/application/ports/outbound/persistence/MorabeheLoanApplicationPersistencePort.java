package ir.dotin.loan.morabehe.core.application.ports.outbound.persistence;

import java.util.Optional;

import ir.dotin.loan.baseloan.domain.loanapplication.valueobject.ApplicationNumber;
import ir.dotin.loan.morabehe.core.domain.loanapplication.entity.application.MorabeheLoanApplication;
import ir.dotin.loan.morabehe.core.domain.loanapplication.valueobject.MorabeheLoanApplicationId;

public interface MorabeheLoanApplicationPersistencePort {

    void save(MorabeheLoanApplication loanApplication);

    Optional<MorabeheLoanApplication> findById(MorabeheLoanApplicationId id);

    boolean existsByApplicationNumber(ApplicationNumber applicationNumber);

    void update(MorabeheLoanApplication loanApplication);
}
