package ir.dotin.loan.trade.core.application.service.issuefacilitycontract.compensation.commandhandler;

import java.time.Clock;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.stereotype.Service;

import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.error.FailureCause;
import ir.dotin.platform.pangaea.commons.domain.event.DomainEvent;
import ir.dotin.platform.pangaea.workflow.api.command.WorkflowCommandHandler;
import ir.dotin.platform.pangaea.workflow.api.definition.Workflow;
import ir.dotin.platform.pangaea.workflow.api.definition.WorkflowRoute;
import ir.dotin.platform.pangaea.workflow.api.engine.WorkflowEngine;
import ir.dotin.platform.pangaea.workflow.api.model.StepResult;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.TrackedTransactionNumber;
import ir.dotin.loan.trade.core.application.ports.inbound.command.CompensateContractIssuanceCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.application.service.shared.account.FcbTransactionReverser;
import ir.dotin.loan.trade.core.application.service.shared.error.TradeLoanApplicationServiceErrors;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
final class CompensateContractIssuanceCommandHandler
        extends WorkflowCommandHandler<
                CompensateContractIssuanceCommand, CompensateContractIssuanceCommandHandler.Data> {

    @Override
    protected Workflow<Data> route(WorkflowRoute<Data> route) {
        return route.singleWrite(
                "compensate-contract-issuance",
                ctx -> StepResult.fromWriteResult(
                        write(ctx.data().command(), ctx.data().prepared())));
    }

    @Override
    protected Result<Data> seed(CompensateContractIssuanceCommand command) {
        return prepare(command).map(prepared -> new Data(command, prepared));
    }

    @Override
    protected void afterCompleted(
            CompensateContractIssuanceCommand command, Data data, List<DomainEvent<?>> publishedEvents) {
        TrackedTransactionNumber reversed = data.prepared().reversals().get();
        if (reversed != null) {
            fcbTransactionReverser.reverseBestEffort(reversed);
        }
    }

    private Result<ReversalPreparation> prepare(CompensateContractIssuanceCommand command) {
        log.warn("Compensating contract issuance for facility: {}", command.loanFacilityId());
        return Result.success(new ReversalPreparation(new AtomicReference<>()));
    }

    private Result<List<DomainEvent<?>>> write(
            CompensateContractIssuanceCommand command, ReversalPreparation prepared) {
        return Result.fromOptional(
                        repository.findById(LoanFacilityId.of(command.loanFacilityId())),
                        () -> FailureCause.notFound(Notification.ofError(
                                TradeLoanApplicationServiceErrors.FACILITY_NOT_FOUND, command.loanFacilityId())))
                .flatMap(facility -> facility.revertContractIssuance(clock).map(transactionNumber -> {
                    prepared.reversals().set(transactionNumber);
                    return facility;
                }))
                .onSuccess(repository::save)
                .map(TradeLoanFacility::domainEvents);
    }

    record Data(CompensateContractIssuanceCommand command, ReversalPreparation prepared) {}

    record ReversalPreparation(AtomicReference<TrackedTransactionNumber> reversals) {}

    private final TradeLoanFacilityRepository repository;
    private final FcbTransactionReverser fcbTransactionReverser;
    private final Clock clock;

    CompensateContractIssuanceCommandHandler(
            WorkflowEngine engine,
            TradeLoanFacilityRepository repository,
            FcbTransactionReverser fcbTransactionReverser,
            Clock clock) {
        super(engine);
        this.repository = repository;
        this.fcbTransactionReverser = fcbTransactionReverser;
        this.clock = clock;
    }
}
