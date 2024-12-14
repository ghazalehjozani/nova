package ir.dotin.loan.morabehe.core.application.service.usecase.impl;

import ir.dotin.loan.baseloan.domain.loanapplication.valueobject.LoanTypeCode;
import ir.dotin.loan.morabehe.core.application.ports.outbound.persistence.MorabeheLoanRulePersistencePort;
import ir.dotin.loan.morabehe.core.application.ports.outbound.persistence.MorabeheLoanTypePersistencePort;
import ir.dotin.loan.morabehe.core.application.service.assembler.MorabeheLoanTypeAssembler;
import ir.dotin.loan.morabehe.core.application.service.command.MorabeheCreateLoanTypeCommand;
import ir.dotin.loan.morabehe.core.application.service.response.LoanTypeResponse;
import ir.dotin.loan.morabehe.core.application.service.usecase.CreateLoanTypeUseCase;
import ir.dotin.loan.morabehe.core.domain.config.entity.loantype.MorabeheLoanType;
import ir.dotin.loan.morabehe.core.domain.config.exception.MorabeheLoanTypeValidationException;
import ir.dotin.loan.morabehe.core.domain.config.valueobject.MorabeheLoanRuleId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@Transactional
public class CreateLoanTypeUseCaseImpl implements CreateLoanTypeUseCase {

    private static final Logger logger = LoggerFactory.getLogger(CreateLoanTypeUseCaseImpl.class);

    private final MorabeheLoanTypePersistencePort loanTypePersistencePort;
    private final MorabeheLoanRulePersistencePort loanRulePersistencePort;
    private final MorabeheLoanTypeAssembler assembler;

    public CreateLoanTypeUseCaseImpl(
            MorabeheLoanTypePersistencePort loanTypePersistencePort,
            MorabeheLoanRulePersistencePort loanRulePersistencePort,
            MorabeheLoanTypeAssembler assembler
    ) {
        this.loanTypePersistencePort = loanTypePersistencePort;
        this.loanRulePersistencePort = loanRulePersistencePort;
        this.assembler = assembler;
    }

    @Override
    public LoanTypeResponse execute(MorabeheCreateLoanTypeCommand command) {
        logger.debug("Executing CreateLoanTypeUseCase with command: {}", command);

        final MorabeheLoanType loanTypeAggregate = assembler.mapToAggregateRoot(command);
        final LoanTypeCode loanTypeCode = loanTypeAggregate.getLoanType().code();

        validateLoanTypeCodeUniqueness(loanTypeCode);
        validateLoanRuleIds(loanTypeAggregate.getLoanType().loanRuleIds());

        loanTypeAggregate.createLoanType();
        loanTypePersistencePort.save(loanTypeAggregate);
        // TODO: Publish Event

        return assembler.mapToResponse(loanTypeAggregate);
    }

    private void validateLoanTypeCodeUniqueness(LoanTypeCode code) {
        boolean existsByCode = loanTypePersistencePort.existsByCode(code);
        if (existsByCode) {
            throw new MorabeheLoanTypeValidationException("Duplicate loan type code", "code");
        }
    }

    private void validateLoanRuleIds(Set<MorabeheLoanRuleId> loanRuleIds) {
        for (MorabeheLoanRuleId loanRuleId : loanRuleIds) {
            boolean ruleExists = loanRulePersistencePort.existsByIdAndEnable(loanRuleId);
            if (!ruleExists) {
                throw new MorabeheLoanTypeValidationException("loan rule not exist with Id:", "id", loanRuleId);
            }
        }
    }

    @Override
    public LoanTypeResponse compensate(MorabeheCreateLoanTypeCommand command) {
        // TODO
        return null;
    }

}
