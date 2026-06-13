package ir.dotin.loan.trade.core.application.service.issuefacilitycontract.compensation.commandhandler;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.stereotype.Service;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.domain.event.DomainEvent;
import ir.dotin.platform.pangaea.workflow.api.command.WorkflowCommandHandler;
import ir.dotin.platform.pangaea.workflow.api.definition.Workflow;
import ir.dotin.loan.baseloan.core.domain.shared.vo.TrackedTransactionNumber;
import ir.dotin.loan.trade.core.application.ports.inbound.command.CompensateContractIssuanceCommand;
import ir.dotin.loan.trade.core.application.service.issuefacilitycontract.compensation.step.ReversalPreparation;
import ir.dotin.loan.trade.core.application.service.issuefacilitycontract.compensation.step.RevertContractIssuanceData;
import ir.dotin.loan.trade.core.application.service.issuefacilitycontract.compensation.step.RevertContractIssuanceStep;
import ir.dotin.loan.trade.core.application.service.shared.account.FcbTransactionReverser;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static ir.dotin.platform.pangaea.workflow.api.definition.Steps.writePublishing;

@Slf4j
@Service
@RequiredArgsConstructor
final class CompensateContractIssuanceCommandHandler
        implements WorkflowCommandHandler<CompensateContractIssuanceCommand, RevertContractIssuanceData> {

    @Override
    public Workflow<RevertContractIssuanceData> definition() {
        // @formatter:off
        return Workflow.singleWrite("compensate-contract-issuance", writePublishing(revertContractIssuanceStep));
        // @formatter:on
    }

    @Override
    public Result<RevertContractIssuanceData> seed(CompensateContractIssuanceCommand command) {
        log.warn("Compensating contract issuance for facility: {}", command.loanFacilityId());
        return Result.success(
                new RevertContractIssuanceData(command, new ReversalPreparation(new AtomicReference<>())));
    }

    @Override
    public void afterCompleted(
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
}
