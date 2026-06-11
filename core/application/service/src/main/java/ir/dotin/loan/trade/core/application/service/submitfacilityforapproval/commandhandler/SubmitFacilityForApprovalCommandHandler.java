package ir.dotin.loan.trade.core.application.service.submitfacilityforapproval.commandhandler;

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
import ir.dotin.platform.pangaea.workflow.api.engine.WorkflowEngine;
import ir.dotin.platform.pangaea.workflow.api.model.StepResult;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.trade.core.application.ports.inbound.command.SubmitFacilityForApprovalCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.application.service.shared.authz.BranchAccessValidator;
import ir.dotin.loan.trade.core.application.service.submitfacilityforapproval.i18n.SubmitFacilityForApprovalErrorCodes;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;
import ir.dotin.loan.trade.core.domain.loanfacility.service.TradeLoanFacilityService;

@Service
public final class SubmitFacilityForApprovalCommandHandler
        extends WorkflowCommandHandler<
                SubmitFacilityForApprovalCommand, SubmitFacilityForApprovalCommandHandler.Data> {

    private static final Logger log = LoggerFactory.getLogger(SubmitFacilityForApprovalCommandHandler.class);

    record Data(SubmitFacilityForApprovalCommand command, Unit prepared) {}

    private final TradeLoanFacilityRepository repository;
    private final BranchAccessValidator branchAccessValidator;
    private final TradeLoanFacilityService domainService;
    private final Workflow<Data> workflow;

    public SubmitFacilityForApprovalCommandHandler(
            WorkflowEngine engine,
            TradeLoanFacilityRepository repository,
            BranchAccessValidator branchAccessValidator,
            TradeLoanFacilityService domainService) {
        super(engine);
        this.repository = repository;
        this.branchAccessValidator = branchAccessValidator;
        this.domainService = domainService;
        this.workflow = Workflow.singleWrite(
                "submit-facility-for-approval",
                ctx -> StepResult.fromWriteResult(write(ctx.data().command(), ctx.data().prepared())));
    }

    @Override
    protected Workflow<Data> workflow() {
        return workflow;
    }

    @Override
    protected Result<Data> seed(SubmitFacilityForApprovalCommand command) {
        return prepare(command).map(prepared -> new Data(command, prepared));
    }

    private Result<Unit> prepare(SubmitFacilityForApprovalCommand command) {
        return branchAccessValidator.verifyCallerCoversFacility(
                command.branchCode(), LoanFacilityId.of(command.loanFacilityId()));
    }

    private Result<List<DomainEvent<?>>> write(SubmitFacilityForApprovalCommand command, Unit prepared) {
        LoanFacilityId loanFacilityId = LoanFacilityId.of(command.loanFacilityId());
        return Result.fromOptional(
                        repository.findById(loanFacilityId),
                        () -> FailureCause.notFound(Notification.ofError(
                                SubmitFacilityForApprovalErrorCodes.FACILITY_NOT_FOUND, command.loanFacilityId())))
                .flatMap(facility -> domainService.submitForApproval(facility).map(v -> facility))
                .onSuccess(facility -> {
                    repository.save(facility, command.version());
                    log.info("Facility submitted for approval: {}", command.loanFacilityId());
                })
                .map(TradeLoanFacility::domainEvents);
    }
}
