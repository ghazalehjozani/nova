package ir.dotin.loan.morabehe.core.application.service.usecase.impl;

import ir.dotin.loan.morabehe.core.application.ports.outbound.persistence.MorabeheLoanRulePersistencePort;
import ir.dotin.loan.morabehe.core.application.ports.outbound.persistence.MorabeheLoanTypePersistencePort;
import ir.dotin.loan.morabehe.core.application.service.assembler.MorabeheLoanTypeAssembler;
import ir.dotin.loan.morabehe.core.application.service.command.MorabeheCreateLoanApplicationCommand;
import ir.dotin.loan.morabehe.core.application.service.command.MorabeheCreateLoanTypeCommand;
import ir.dotin.loan.morabehe.core.application.service.response.LoanApplicationResponse;
import ir.dotin.loan.morabehe.core.application.service.response.LoanTypeResponse;
import ir.dotin.loan.morabehe.core.application.service.usecase.CreateLoanTypeUseCase;
import ir.dotin.loan.morabehe.core.domain.config.entity.loantype.MorabeheLoanType;
import ir.dotin.loan.morabehe.core.domain.config.exception.MorabeheLoanTypeValidationException;
import ir.dotin.loan.morabehe.core.domain.config.valueobject.MorabeheLoanRuleId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CreateLoanTypeUseCaseImpl implements CreateLoanTypeUseCase {

    private static final Logger logger = LoggerFactory.getLogger(CreateLoanTypeUseCaseImpl.class);

    private final MorabeheLoanTypePersistencePort loanTypePersistencePort;
    private final MorabeheLoanRulePersistencePort morabeheLoanRulePersistencePort;
    private final MorabeheLoanTypeAssembler assembler;

    public CreateLoanTypeUseCaseImpl(MorabeheLoanTypePersistencePort loanTypePersistencePort,
                                     MorabeheLoanRulePersistencePort morabeheLoanRulePersistencePort,
                                     MorabeheLoanTypeAssembler assembler) {
        this.loanTypePersistencePort = loanTypePersistencePort;
        this.morabeheLoanRulePersistencePort = morabeheLoanRulePersistencePort;
        this.assembler = assembler;
    }

    @Override
    public LoanTypeResponse execute(MorabeheCreateLoanTypeCommand command) {
        logger.debug("Executing CreateLoanTypeUseCase with command: {}", command);

        MorabeheLoanType morabeheLoanType = assembler.mapToAggregateRoot(command);
        boolean existsByCode = loanTypePersistencePort.existsByCode(
                morabeheLoanType.getLoanType().getCode());
        if (existsByCode) {
            throw new MorabeheLoanTypeValidationException("Duplicate loan type code", "code");
        }
        for (MorabeheLoanRuleId loanRuleId : morabeheLoanType.getLoanType().getLoanRuleIds()) {
            boolean exists = morabeheLoanRulePersistencePort.existsByIdAndEnable(loanRuleId);
            if (!exists) {
                throw new MorabeheLoanTypeValidationException("loan rule not exist with Id:", "id",
                                                              loanRuleId);
            }
        }
        morabeheLoanType.createLoanType();
        loanTypePersistencePort.save(morabeheLoanType);
        // TODO: Publish Event
        return assembler.mapToResponse(morabeheLoanType);
    }

    @Override
    public LoanTypeResponse compensate(MorabeheCreateLoanTypeCommand command) {
        // TODO
        return null;
    }
}
