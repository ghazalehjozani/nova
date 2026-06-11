package ir.dotin.loan.trade.core.application.service.irregularprogressivedisbursement.compensation.commandhandler;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.stereotype.Service;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.domain.event.DomainEvent;
import ir.dotin.platform.pangaea.workflow.api.command.WorkflowCommandHandler;
import ir.dotin.platform.pangaea.workflow.api.definition.Workflow;
import ir.dotin.platform.pangaea.workflow.api.definition.WorkflowRoute;
import ir.dotin.platform.pangaea.workflow.api.engine.WorkflowEngine;
import ir.dotin.loan.baseloan.core.domain.shared.vo.TrackedTransactionNumber;
import ir.dotin.loan.trade.core.application.ports.inbound.command.CompensateIrregularDisbursementCommand;
import ir.dotin.loan.trade.core.application.service.irregularprogressivedisbursement.compensation.step.ReversalPreparation;
import ir.dotin.loan.trade.core.application.service.irregularprogressivedisbursement.compensation.step.RevertIrregularDisbursementData;
import ir.dotin.loan.trade.core.application.service.irregularprogressivedisbursement.compensation.step.RevertIrregularDisbursementStep;
import ir.dotin.loan.trade.core.application.service.shared.account.FcbTransactionReverser;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
final class CompensateIrregularDisbursementCommandHandler
        extends WorkflowCommandHandler<CompensateIrregularDisbursementCommand, RevertIrregularDisbursementData> {

    @Override
    protected Workflow<RevertIrregularDisbursementData> route(WorkflowRoute<RevertIrregularDisbursementData> route) {
        // @formatter:off
        return route.singleWrite("compensate-irregular-disbursement", revertIrregularDisbursementStep);
        // @formatter:on
    }

    @Override
    protected Result<RevertIrregularDisbursementData> seed(CompensateIrregularDisbursementCommand command) {
        return prepare(command).map(prepared -> new RevertIrregularDisbursementData(command, prepared));
    }

    @Override
    protected void afterCompleted(
            CompensateIrregularDisbursementCommand command,
            RevertIrregularDisbursementData data,
            List<DomainEvent<?>> publishedEvents) {
        TrackedTransactionNumber removed = data.prepared().reversals().get();
        if (removed != null) {
            reverseTransaction(removed);
        }
    }

    private Result<ReversalPreparation> prepare(CompensateIrregularDisbursementCommand command) {
        log.warn("Compensating irregular disbursement for facility: {}", command.loanFacilityId());
        return Result.success(new ReversalPreparation(new AtomicReference<>()));
    }

    private void reverseTransaction(TrackedTransactionNumber trackedNumber) {
        log.info("Reversing irregular disbursement transaction: {}", trackedNumber.value());
        fcbTransactionReverser.reverseBestEffort(trackedNumber);
    }

    private final RevertIrregularDisbursementStep revertIrregularDisbursementStep;
    private final FcbTransactionReverser fcbTransactionReverser;

    CompensateIrregularDisbursementCommandHandler(
            WorkflowEngine engine,
            RevertIrregularDisbursementStep revertIrregularDisbursementStep,
            FcbTransactionReverser fcbTransactionReverser) {
        super(engine);
        this.revertIrregularDisbursementStep = revertIrregularDisbursementStep;
        this.fcbTransactionReverser = fcbTransactionReverser;
    }
}
