package ir.dotin.loan.trade.core.application.service.originateloanfacility.step;

import ir.dotin.loan.trade.core.application.ports.inbound.command.OriginateUnequalInstallmentFacilityCommand;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.component.OriginationPreparation;

public record OriginateUnequalInstallmentFacilityData(
        OriginateUnequalInstallmentFacilityCommand command, OriginationPreparation prepared) {}
