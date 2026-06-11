package ir.dotin.loan.trade.core.application.service.submitfacilityforapproval.compensation.commandhandler;

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
import ir.dotin.loan.trade.core.application.ports.inbound.command.CompensateApprovalSubmissionCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.application.service.submitfacilityforapproval.i18n.SubmitFacilityForApprovalErrorCodes;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;

@Service
final class CompensateApprovalSubmissionCommandHandler
        extends WorkflowCommandHandler<
                CompensateApprovalSubmissionCommand, CompensateApprovalSubmissionCommandHandler.Data> {

    private static final Logger log = LoggerFactory.getLogger(CompensateApprovalSubmissionCommandHandler.class);

    @Override
    protected Workflow<Data> route(WorkflowRoute<Data> route) {
        return route.singleWrite(
                "compensate-approval-submission",
                ctx -> StepResult.fromWriteResult(
                        write(ctx.data().command(), ctx.data().prepared())));
    }

    @Override
    protected Result<Data> seed(CompensateApprovalSubmissionCommand command) {
        return prepare(command).map(prepared -> new Data(command, prepared));
    }

    private Result<Unit> prepare(CompensateApprovalSubmissionCommand command) {
        log.warn("Compensating approval submission for facility: {}", command.loanFacilityId());
        return Result.success();
    }

    private Result<List<DomainEvent<?>>> write(CompensateApprovalSubmissionCommand command, Unit prepared) {
        return loadAndProcess(command.loanFacilityId(), f -> f.revertApprovalSubmission(clock));
    }

    private Result<List<DomainEvent<?>>> loadAndProcess(
            java.util.UUID facilityId, java.util.function.Function<TradeLoanFacility, Result<Unit>> operation) {
        return Result.fromOptional(
                        repository.findById(LoanFacilityId.of(facilityId)),
                        () -> FailureCause.notFound(Notification.ofError(
                                SubmitFacilityForApprovalErrorCodes.FACILITY_NOT_FOUND, facilityId)))
                .flatMap(facility -> operation.apply(facility).map(v -> facility))
                .onSuccess(repository::save)
                .map(TradeLoanFacility::domainEvents);
    }

    record Data(CompensateApprovalSubmissionCommand command, Unit prepared) {}

    private final TradeLoanFacilityRepository repository;
    private final Clock clock;

    CompensateApprovalSubmissionCommandHandler(
            WorkflowEngine engine, TradeLoanFacilityRepository repository, Clock clock) {
        super(engine);
        this.repository = repository;
        this.clock = clock;
    }
}
