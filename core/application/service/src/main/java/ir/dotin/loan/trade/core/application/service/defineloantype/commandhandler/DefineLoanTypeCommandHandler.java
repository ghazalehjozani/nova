package ir.dotin.loan.trade.core.application.service.defineloantype.commandhandler;

import java.time.Clock;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.domain.event.DomainEvent;
import ir.dotin.platform.pangaea.workflow.api.command.WorkflowCommandHandler;
import ir.dotin.platform.pangaea.workflow.api.definition.Workflow;
import ir.dotin.platform.pangaea.workflow.api.definition.WorkflowRoute;
import ir.dotin.platform.pangaea.workflow.api.engine.WorkflowEngine;
import ir.dotin.platform.pangaea.workflow.api.model.StepResult;
import ir.dotin.loan.trade.core.application.ports.inbound.command.DefineLoanTypeCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanTypeRepository;
import ir.dotin.loan.trade.core.application.service.defineloantype.component.LoanTypePrerequisitesLoader;
import ir.dotin.loan.trade.core.application.service.defineloantype.mapper.DefineLoanTypeCommandMapper;
import ir.dotin.loan.trade.core.domain.loantype.entity.TradeLoanType;
import ir.dotin.loan.trade.core.domain.loantype.service.TradeLoanTypeValidationService;

@Service
public final class DefineLoanTypeCommandHandler
        extends WorkflowCommandHandler<DefineLoanTypeCommand, DefineLoanTypeCommandHandler.Data> {

    private static final Logger log = LoggerFactory.getLogger(DefineLoanTypeCommandHandler.class);

    @Override
    protected Workflow<Data> route(WorkflowRoute<Data> route) {
        return route.singleWrite(
                "define-loan-type",
                ctx -> StepResult.fromWriteResult(
                        write(ctx.data().command(), ctx.data().prepared())));
    }

    @Override
    protected Result<Data> seed(DefineLoanTypeCommand command) {
        return prepare(command).map(prepared -> new Data(command, prepared));
    }

    private Result<LoanTypePrerequisitesLoader.Prerequisites> prepare(DefineLoanTypeCommand command) {
        return prerequisitesLoader.gather(command);
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

    record Data(DefineLoanTypeCommand command, LoanTypePrerequisitesLoader.Prerequisites prepared) {}

    private final DefineLoanTypeCommandMapper mapper;
    private final TradeLoanTypeRepository loanTypeRepository;
    private final TradeLoanTypeValidationService loanTypeValidationService;
    private final Clock clock;
    private final LoanTypePrerequisitesLoader prerequisitesLoader;

    public DefineLoanTypeCommandHandler(
            WorkflowEngine engine,
            DefineLoanTypeCommandMapper mapper,
            TradeLoanTypeRepository loanTypeRepository,
            TradeLoanTypeValidationService loanTypeValidationService,
            Clock clock,
            LoanTypePrerequisitesLoader prerequisitesLoader) {
        super(engine);
        this.mapper = mapper;
        this.loanTypeRepository = loanTypeRepository;
        this.loanTypeValidationService = loanTypeValidationService;
        this.clock = clock;
        this.prerequisitesLoader = prerequisitesLoader;
    }
}
