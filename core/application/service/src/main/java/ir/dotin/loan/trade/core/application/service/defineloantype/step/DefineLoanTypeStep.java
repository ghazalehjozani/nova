package ir.dotin.loan.trade.core.application.service.defineloantype.step;

import java.time.Clock;
import java.util.List;

import org.springframework.stereotype.Component;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.domain.event.DomainEvent;
import ir.dotin.platform.pangaea.workflow.api.context.WorkflowContext;
import ir.dotin.platform.pangaea.workflow.api.definition.WriteActivity;
import ir.dotin.platform.pangaea.workflow.api.model.StepResult;
import ir.dotin.loan.trade.core.application.ports.inbound.command.DefineLoanTypeCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanTypeRepository;
import ir.dotin.loan.trade.core.application.service.defineloantype.component.LoanTypePrerequisitesLoader;
import ir.dotin.loan.trade.core.application.service.defineloantype.mapper.DefineLoanTypeCommandMapper;
import ir.dotin.loan.trade.core.domain.loantype.entity.TradeLoanType;
import ir.dotin.loan.trade.core.domain.loantype.service.TradeLoanTypeValidationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class DefineLoanTypeStep implements WriteActivity<DefineLoanTypeData> {

    private final DefineLoanTypeCommandMapper mapper;
    private final TradeLoanTypeRepository loanTypeRepository;
    private final TradeLoanTypeValidationService loanTypeValidationService;
    private final Clock clock;

    @Override
    public StepResult<List<DomainEvent<?>>> execute(WorkflowContext<DefineLoanTypeData> ctx) {
        DefineLoanTypeData data = ctx.data();
        return StepResult.fromWriteResult(write(data.command(), data.prepared()));
    }

    private Result<List<DomainEvent<?>>> write(
            DefineLoanTypeCommand command, LoanTypePrerequisitesLoader.Prerequisites prepared) {
        return buildLoanType(command, prepared)
                .flatMap(this::validateBusinessRules)
                .onSuccess(loanTypeRepository::save)
                .onSuccess(loanType -> log.debug(
                        "Loan type defined successfully: {}", loanType.getId().value()))
                .map(TradeLoanType::domainEvents);
    }

    private Result<TradeLoanType> buildLoanType(
            DefineLoanTypeCommand command, LoanTypePrerequisitesLoader.Prerequisites prereqs) {
        return TradeLoanType.create(mapper.toBuilder(command).loanArrangementIds(prereqs.arrangementIds()), clock);
    }

    private Result<TradeLoanType> validateBusinessRules(TradeLoanType loanType) {
        return loanTypeValidationService
                .validateMandatoryRelationTypeLoanTopics(loanType)
                .map(ignored -> loanType);
    }
}
