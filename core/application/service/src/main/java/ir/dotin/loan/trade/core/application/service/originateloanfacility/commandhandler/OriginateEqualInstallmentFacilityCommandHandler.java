package ir.dotin.loan.trade.core.application.service.originateloanfacility.commandhandler;

import org.springframework.stereotype.Service;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.workflow.api.command.WorkflowCommandHandler;
import ir.dotin.platform.pangaea.workflow.api.definition.Workflow;
import ir.dotin.loan.trade.core.application.ports.inbound.command.OriginateEqualInstallmentFacilityCommand;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.component.FacilityOriginationPreparer;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.step.OriginateEqualInstallmentFacilityData;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.step.OriginateEqualInstallmentFacilityStep;

import lombok.RequiredArgsConstructor;

import static ir.dotin.platform.pangaea.workflow.api.definition.Steps.writePublishing;

@Service
@RequiredArgsConstructor
public final class OriginateEqualInstallmentFacilityCommandHandler
        implements WorkflowCommandHandler<
                OriginateEqualInstallmentFacilityCommand, OriginateEqualInstallmentFacilityData> {

    @Override
    public Workflow<OriginateEqualInstallmentFacilityData> definition() {
        // @formatter:off
        return Workflow.singleWrite("originate-equal-installment-facility", writePublishing(originateStep));
        // @formatter:on
    }

    @Override
    public Result<OriginateEqualInstallmentFacilityData> seed(OriginateEqualInstallmentFacilityCommand command) {
        return originationPreparer
                .prepare(command)
                .map(prepared -> new OriginateEqualInstallmentFacilityData(command, prepared));
    }

    private final FacilityOriginationPreparer originationPreparer;
    private final OriginateEqualInstallmentFacilityStep originateStep;
}
