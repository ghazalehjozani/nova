package ir.dotin.loan.trade.core.application.service.lumpsumdisbursement.compensation.commandhandler;

import java.util.ArrayList;
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
import ir.dotin.loan.trade.core.application.ports.inbound.command.CompensateLumpSumDisbursementCommand;
import ir.dotin.loan.trade.core.application.service.lumpsumdisbursement.compensation.step.ReversalPreparation;
import ir.dotin.loan.trade.core.application.service.lumpsumdisbursement.compensation.step.RevertLumpSumData;
import ir.dotin.loan.trade.core.application.service.lumpsumdisbursement.compensation.step.RevertLumpSumDisbursementStep;
import ir.dotin.loan.trade.core.application.service.shared.account.FcbTransactionReverser;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
final class CompensateLumpSumDisbursementCommandHandler
        extends WorkflowCommandHandler<CompensateLumpSumDisbursementCommand, RevertLumpSumData> {

    @Override
    protected Workflow<RevertLumpSumData> route(WorkflowRoute<RevertLumpSumData> route) {
        // @formatter:off
        return route.singleWrite("compensate-lump-sum-disbursement", revertLumpSumDisbursementStep);
        // @formatter:on
    }

    @Override
    protected Result<RevertLumpSumData> seed(CompensateLumpSumDisbursementCommand command) {
        log.warn("Compensating lump sum disbursement for facility: {}", command.loanFacilityId());
        return Result.success(
                new RevertLumpSumData(command, new ReversalPreparation(new AtomicReference<>(new ArrayList<>()))));
    }

    @Override
    protected void afterCompleted(
            CompensateLumpSumDisbursementCommand command,
            RevertLumpSumData data,
            List<DomainEvent<?>> publishedEvents) {
        List<TrackedTransactionNumber> reversed = data.prepared().reversals().get();
        if (reversed != null && !reversed.isEmpty()) {
            fcbTransactionReverser.reverseAllBestEffort(reversed);
        }
    }

    private final RevertLumpSumDisbursementStep revertLumpSumDisbursementStep;
    private final FcbTransactionReverser fcbTransactionReverser;

    CompensateLumpSumDisbursementCommandHandler(
            WorkflowEngine engine,
            RevertLumpSumDisbursementStep revertLumpSumDisbursementStep,
            FcbTransactionReverser fcbTransactionReverser) {
        super(engine);
        this.revertLumpSumDisbursementStep = revertLumpSumDisbursementStep;
        this.fcbTransactionReverser = fcbTransactionReverser;
    }
}
