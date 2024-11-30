package ir.dotin.loan.morabehe.core.application.service.usecase.impl;

import ir.dotin.loan.morabehe.core.application.ports.secondary.MorabeheLoanRulePersistencePort;
import ir.dotin.loan.morabehe.core.application.ports.secondary.MorabeheLoanTypePersistencePort;
import ir.dotin.loan.morabehe.core.application.service.usecase.CreateLoanTypeUseCase;
import ir.dotin.loan.morabehe.core.domain.config.entity.loantype.MorabeheLoanType;
import ir.dotin.loan.morabehe.core.domain.config.exception.MorabeheLoanTypeValidationException;
import ir.dotin.loan.morabehe.core.domain.config.valueobject.MorabeheLoanRuleId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CreateLoanTypeUseCaseImpl implements CreateLoanTypeUseCase {

    private final MorabeheLoanTypePersistencePort loanTypePersistencePort;
    private final MorabeheLoanRulePersistencePort morabeheLoanRulePersistencePort;

    public CreateLoanTypeUseCaseImpl(MorabeheLoanTypePersistencePort loanTypePersistencePort,
                                     MorabeheLoanRulePersistencePort morabeheLoanRulePersistencePort) {
        this.loanTypePersistencePort = loanTypePersistencePort;
        this.morabeheLoanRulePersistencePort = morabeheLoanRulePersistencePort;
    }

    @Override
    public MorabeheLoanType create(MorabeheLoanType morabeheLoanType) {
        boolean existsByCode = loanTypePersistencePort.existsByCode(
                morabeheLoanType.getLoanType().getCode());
        if (existsByCode) {
            throw new MorabeheLoanTypeValidationException("Duplicate loan type code", "code");
        }
        for (MorabeheLoanRuleId morabeheLoanRuleId : morabeheLoanType.getLoanType()
                .getLoanRuleIds()) {
            boolean exists = morabeheLoanRulePersistencePort
                    .existsByIdAndEnable(morabeheLoanRuleId);
            if (!exists) {
                throw new MorabeheLoanTypeValidationException("loan rule not exist with Id:", "id",
                                                              morabeheLoanRuleId);
            }
        }
        morabeheLoanType.createLoanType();
        loanTypePersistencePort.save(morabeheLoanType);
        // TODO: Publish Event
        return morabeheLoanType;
    }
}
