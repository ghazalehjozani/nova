package ir.dotin.loan.trade.core.application.service.addfacilitycollateral.compensation.step;

import ir.dotin.loan.trade.core.application.ports.inbound.command.CompensateCollateralCommand;

public record RevertCollateralData(CompensateCollateralCommand command, ReleasePreparation prepared) {}
