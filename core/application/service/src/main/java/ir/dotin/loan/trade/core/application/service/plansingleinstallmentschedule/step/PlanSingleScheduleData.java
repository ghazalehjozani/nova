package ir.dotin.loan.trade.core.application.service.plansingleinstallmentschedule.step;

import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.loan.trade.core.application.ports.inbound.command.PlanSingleInstallmentScheduleCommand;

public record PlanSingleScheduleData(PlanSingleInstallmentScheduleCommand command, Unit prepared) {}
