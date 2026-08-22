package ir.dotin.loan.trade.core.application.service.originateloanfacility.commandhandler;

import org.springframework.stereotype.Service;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.workflow.api.command.WorkflowCommandHandler;
import ir.dotin.platform.pangaea.workflow.api.definition.Workflow;
import ir.dotin.loan.trade.core.application.ports.inbound.command.OriginateUnequalInstallmentFacilityCommand;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.component.FacilityOriginationPreparer;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.step.OriginateUnequalInstallmentFacilityData;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.step.OriginateUnequalInstallmentFacilityStep;

import lombok.RequiredArgsConstructor;

import static ir.dotin.platform.pangaea.workflow.api.definition.Steps.writePublishing;

@Service
@RequiredArgsConstructor
public final class OriginateUnequalInstallmentFacilityCommandHandler
        implements WorkflowCommandHandler<
                OriginateUnequalInstallmentFacilityCommand, OriginateUnequalInstallmentFacilityData> {

    @Override
    public Workflow<OriginateUnequalInstallmentFacilityData> definition() {
        // @formatter:off
        return Workflow.singleWrite("originate-unequal-installment-facility", writePublishing(originateStep));
        // @formatter:on
    }

    @Override
    public Result<OriginateUnequalInstallmentFacilityData> seed(OriginateUnequalInstallmentFacilityCommand command) {
        return originationPreparer
                .prepare(command)
                .map(prepared -> new OriginateUnequalInstallmentFacilityData(command, prepared));
    }

    private final FacilityOriginationPreparer originationPreparer;
    private final OriginateUnequalInstallmentFacilityStep originateStep;
}
