package ir.dotin.loan.trade.core.application.service.approvefacility.compensation.commandhandler;

import java.time.Clock;
import java.util.List;

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
import ir.dotin.loan.trade.core.application.ports.inbound.command.CompensateApprovalCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.application.service.shared.error.TradeLoanApplicationServiceErrors;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
final class CompensateApprovalCommandHandler
        extends WorkflowCommandHandler<CompensateApprovalCommand, CompensateApprovalCommandHandler.Data> {

    @Override
    protected Workflow<Data> route(WorkflowRoute<Data> route) {
        return route.singleWrite(
                "compensate-approval",
                ctx -> StepResult.fromWriteResult(
                        write(ctx.data().command(), ctx.data().prepared())));
    }

    @Override
    protected Result<Data> seed(CompensateApprovalCommand command) {
        return prepare(command).map(prepared -> new Data(command, prepared));
    }

    private Result<Unit> prepare(CompensateApprovalCommand command) {
        log.warn("Compensating approval for facility: {}", command.loanFacilityId());
        return Result.success();
    }

    private Result<List<DomainEvent<?>>> write(CompensateApprovalCommand command, Unit prepared) {
        return Result.fromOptional(
                        repository.findById(LoanFacilityId.of(command.loanFacilityId())),
                        () -> FailureCause.notFound(Notification.ofError(
                                TradeLoanApplicationServiceErrors.FACILITY_NOT_FOUND, command.loanFacilityId())))
                .flatMap(facility -> facility.revertApproval(clock).map(v -> facility))
                .onSuccess(repository::save)
                .map(TradeLoanFacility::domainEvents);
    }

    record Data(CompensateApprovalCommand command, Unit prepared) {}

    private final TradeLoanFacilityRepository repository;
    private final Clock clock;

    CompensateApprovalCommandHandler(WorkflowEngine engine, TradeLoanFacilityRepository repository, Clock clock) {
        super(engine);
        this.repository = repository;
        this.clock = clock;
    }
}
