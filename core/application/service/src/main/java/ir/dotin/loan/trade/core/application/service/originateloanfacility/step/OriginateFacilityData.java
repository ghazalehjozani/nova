package ir.dotin.loan.trade.core.application.service.originateloanfacility.step;

import ir.dotin.loan.trade.core.application.ports.inbound.command.OriginateLoanFacilityCommand;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.component.OriginationPreparation;

public record OriginateFacilityData(OriginateLoanFacilityCommand command, OriginationPreparation prepared) {}
