package ir.dotin.loan.trade.core.application.service.originateloanfacility.compensation.commandhandler;

import org.springframework.stereotype.Service;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.platform.pangaea.workflow.api.command.WorkflowCommandHandler;
import ir.dotin.platform.pangaea.workflow.api.definition.Workflow;
import ir.dotin.loan.trade.core.application.ports.inbound.command.CompensateOriginationCommand;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.compensation.step.RevertOriginationData;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.compensation.step.RevertOriginationStep;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static ir.dotin.platform.pangaea.workflow.api.definition.Steps.writePublishing;

@Slf4j
@Service
@RequiredArgsConstructor
final class CompensateOriginationCommandHandler
        implements WorkflowCommandHandler<CompensateOriginationCommand, RevertOriginationData> {

    @Override
    public Workflow<RevertOriginationData> definition() {
        // @formatter:off
        return Workflow.singleWrite("compensate-origination", writePublishing(revertOriginationStep));
        // @formatter:on
    }

    @Override
    public Result<RevertOriginationData> seed(CompensateOriginationCommand command) {
        log.warn("Compensating origination for facility: {}", command.loanFacilityId());
        return Result.success(new RevertOriginationData(command, Unit.INSTANCE));
    }

    private final RevertOriginationStep revertOriginationStep;
}
