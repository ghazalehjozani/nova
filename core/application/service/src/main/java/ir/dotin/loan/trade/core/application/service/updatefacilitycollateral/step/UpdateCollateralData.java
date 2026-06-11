package ir.dotin.loan.trade.core.application.service.updatefacilitycollateral.step;

import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.loan.trade.core.application.ports.inbound.command.UpdateFacilityCollateralCommand;

public record UpdateCollateralData(UpdateFacilityCollateralCommand command, Unit prepared) {}
