package ir.dotin.loan.trade.core.application.service.originateloanfacility.compensation.step;

import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.loan.trade.core.application.ports.inbound.command.CompensateOriginationCommand;

public record RevertOriginationData(CompensateOriginationCommand command, Unit prepared) {}
