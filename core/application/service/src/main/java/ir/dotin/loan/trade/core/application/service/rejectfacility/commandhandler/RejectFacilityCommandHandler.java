package ir.dotin.loan.trade.core.application.service.rejectfacility.commandhandler;

import java.time.Clock;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.platform.pangaea.commons.core.error.FailureCause;
import ir.dotin.platform.pangaea.commons.domain.event.DomainEvent;
import ir.dotin.platform.pangaea.workflow.api.command.WorkflowCommandHandler;
import ir.dotin.platform.pangaea.workflow.api.definition.Workflow;
import ir.dotin.platform.pangaea.workflow.api.definition.WorkflowRoute;
import ir.dotin.platform.pangaea.workflow.api.engine.WorkflowEngine;
import ir.dotin.platform.pangaea.workflow.api.model.StepResult;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.trade.core.application.ports.inbound.command.RejectFacilityCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.application.service.shared.authz.BranchAccessValidator;
import ir.dotin.loan.trade.core.application.service.shared.error.TradeLoanApplicationServiceErrors;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;

@Service
public final class RejectFacilityCommandHandler
        extends WorkflowCommandHandler<RejectFacilityCommand, RejectFacilityCommandHandler.Data> {

    private static final Logger log = LoggerFactory.getLogger(RejectFacilityCommandHandler.class);

    @Override
    protected Workflow<Data> route(WorkflowRoute<Data> route) {
        return route.singleWrite(
                "reject-facility",
                ctx -> StepResult.fromWriteResult(
                        write(ctx.data().command(), ctx.data().prepared())));
    }

    @Override
    protected Result<Data> seed(RejectFacilityCommand command) {
        return prepare(command).map(prepared -> new Data(command, prepared));
    }

    private Result<Unit> prepare(RejectFacilityCommand command) {
        return branchAccessValidator.verifyCallerCoversFacility(
                command.branchCode(), LoanFacilityId.of(command.loanFacilityId()));
    }

    private Result<List<DomainEvent<?>>> write(RejectFacilityCommand command, Unit prepared) {
        LoanFacilityId loanFacilityId = LoanFacilityId.of(command.loanFacilityId());
        return Result.fromOptional(
                        repository.findById(loanFacilityId),
                        () -> FailureCause.notFound(Notification.ofError(
                                TradeLoanApplicationServiceErrors.FACILITY_NOT_FOUND, command.uid())))
                .onSuccess(facility -> {
                    facility.reject(clock);
                    repository.save(facility, command.version());
                    log.info("Facility rejected: {}", command.loanFacilityId());
                })
                .map(TradeLoanFacility::domainEvents);
    }

    record Data(RejectFacilityCommand command, Unit prepared) {}

    private final TradeLoanFacilityRepository repository;
    private final BranchAccessValidator branchAccessValidator;
    private final Clock clock;

    public RejectFacilityCommandHandler(
            WorkflowEngine engine,
            TradeLoanFacilityRepository repository,
            BranchAccessValidator branchAccessValidator,
            Clock clock) {
        super(engine);
        this.repository = repository;
        this.branchAccessValidator = branchAccessValidator;
        this.clock = clock;
    }
}
