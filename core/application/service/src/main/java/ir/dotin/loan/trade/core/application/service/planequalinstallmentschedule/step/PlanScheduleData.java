package ir.dotin.loan.trade.core.application.service.planequalinstallmentschedule.step;

import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.loan.trade.core.application.ports.inbound.command.PlanEqualInstallmentScheduleCommand;

public record PlanScheduleData(PlanEqualInstallmentScheduleCommand command, Unit prepared) {}
