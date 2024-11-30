package ir.dotin.loan.morabehe.core.application.ports.secondary;

import ir.dotin.loan.baseloan.domain.loanapplication.valueobject.ApplicationNumber;
import ir.dotin.loan.morabehe.core.domain.loanapplication.entity.application.MorabeheLoanApplication;
import ir.dotin.loan.morabehe.core.domain.loanapplication.valueobject.MorabeheLoanApplicationId;
import java.util.Optional;

public interface MorabeheLoanApplicationPersistencePort {

    void save(MorabeheLoanApplication loanApplication);

    Optional<MorabeheLoanApplication> findById(MorabeheLoanApplicationId id);

    boolean existsByApplicationNumber(ApplicationNumber applicationNumber);
}
