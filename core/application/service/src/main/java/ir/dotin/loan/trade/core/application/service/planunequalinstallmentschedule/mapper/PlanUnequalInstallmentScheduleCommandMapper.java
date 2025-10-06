package ir.dotin.loan.trade.core.application.service.planunequalinstallmentschedule.mapper;

import java.util.List;

import ir.dotin.loan.baseloan.core.domain.installmentschedule.vo.InstallmentSpec;
import ir.dotin.loan.trade.core.application.ports.inbound.command.PlanUnequalInstallmentScheduleCommand;

public interface PlanUnequalInstallmentScheduleCommandMapper {
    List<InstallmentSpec> mapSpec(List<PlanUnequalInstallmentScheduleCommand.InstallmentSpecDto> installments);
}
