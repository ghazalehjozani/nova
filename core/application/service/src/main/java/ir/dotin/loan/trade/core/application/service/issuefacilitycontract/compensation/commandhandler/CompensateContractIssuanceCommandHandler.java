package ir.dotin.loan.trade.core.application.service.issuefacilitycontract.compensation.commandhandler;

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
import ir.dotin.loan.trade.core.application.ports.inbound.command.CompensateContractIssuanceCommand;
import ir.dotin.loan.trade.core.application.service.issuefacilitycontract.compensation.step.ReversalPreparation;
import ir.dotin.loan.trade.core.application.service.issuefacilitycontract.compensation.step.RevertContractIssuanceData;
import ir.dotin.loan.trade.core.application.service.issuefacilitycontract.compensation.step.RevertContractIssuanceStep;
import ir.dotin.loan.trade.core.application.service.shared.account.FcbTransactionReverser;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
final class CompensateContractIssuanceCommandHandler
        extends WorkflowCommandHandler<CompensateContractIssuanceCommand, RevertContractIssuanceData> {

    @Override
    protected Workflow<RevertContractIssuanceData> route(WorkflowRoute<RevertContractIssuanceData> route) {
        // @formatter:off
        return route.singleWrite("compensate-contract-issuance", revertContractIssuanceStep);
        // @formatter:on
    }

    @Override
    protected Result<RevertContractIssuanceData> seed(CompensateContractIssuanceCommand command) {
        log.warn("Compensating contract issuance for facility: {}", command.loanFacilityId());
        return Result.success(
                new RevertContractIssuanceData(command, new ReversalPreparation(new AtomicReference<>())));
    }

    @Override
    protected void afterCompleted(
            CompensateContractIssuanceCommand command,
            RevertContractIssuanceData data,
            List<DomainEvent<?>> publishedEvents) {
        TrackedTransactionNumber reversed = data.prepared().reversals().get();
        if (reversed != null) {
            fcbTransactionReverser.reverseBestEffort(reversed);
        }
    }

    private final RevertContractIssuanceStep revertContractIssuanceStep;
    private final FcbTransactionReverser fcbTransactionReverser;

    CompensateContractIssuanceCommandHandler(
            WorkflowEngine engine,
            RevertContractIssuanceStep revertContractIssuanceStep,
            FcbTransactionReverser fcbTransactionReverser) {
        super(engine);
        this.revertContractIssuanceStep = revertContractIssuanceStep;
        this.fcbTransactionReverser = fcbTransactionReverser;
    }
}
