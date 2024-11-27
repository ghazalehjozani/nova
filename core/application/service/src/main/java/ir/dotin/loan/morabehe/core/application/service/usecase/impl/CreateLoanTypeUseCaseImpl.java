package ir.dotin.loan.morabehe.core.application.service.usecase.impl;

import ir.dotin.loan.morabehe.core.application.ports.secondary.MorabeheLoanTypePersistencePort;
import ir.dotin.loan.morabehe.core.application.service.usecase.CreateLoanTypeUseCase;
import ir.dotin.loan.morabehe.core.domain.config.entity.loantype.LoanType;
import ir.dotin.loan.morabehe.core.domain.config.entity.loantype.MorabeheLoanType;
import ir.dotin.loan.morabehe.core.domain.config.exception.MorabeheLoanTypeValidationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CreateLoanTypeUseCaseImpl implements CreateLoanTypeUseCase {

    private final MorabeheLoanTypePersistencePort loanTypePersistencePort;

    public CreateLoanTypeUseCaseImpl(MorabeheLoanTypePersistencePort loanTypePersistencePort) {
        this.loanTypePersistencePort = loanTypePersistencePort;
    }

    @Override
    public MorabeheLoanType create(MorabeheLoanType morabeheLoanType) {
        boolean existsByCode = loanTypePersistencePort.existsByCode(morabeheLoanType.getLoanType().getCode());
        new MorabeheLoanType(null, new LoanType.LoanTypeBuilder());
        if (existsByCode) {
            throw new MorabeheLoanTypeValidationException("Duplicate loan type code", "code");
        }
        morabeheLoanType.createLoanType();
        loanTypePersistencePort.save(morabeheLoanType);
        // TODO: Publish Event
        return morabeheLoanType;
    }
}
