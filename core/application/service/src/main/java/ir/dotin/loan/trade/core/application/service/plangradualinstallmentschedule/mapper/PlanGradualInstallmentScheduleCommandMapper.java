package ir.dotin.loan.trade.core.application.service.plangradualinstallmentschedule.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import ir.dotin.platform.commons.domain.vo.CurrencyType;
import ir.dotin.platform.commons.domain.vo.Money;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.vo.InstallmentSpec;
import ir.dotin.loan.trade.core.application.ports.inbound.command.PlanGradualInstallmentScheduleCommand;
import ir.dotin.loan.trade.core.application.ports.inbound.dto.MoneyDto;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE, // TODO: remove
        unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface PlanGradualInstallmentScheduleCommandMapper {
    List<InstallmentSpec> mapSpec(List<PlanGradualInstallmentScheduleCommand.InstallmentSpecDto> installments);

    Money toMoney(MoneyDto dto);

    default CurrencyType map(String currencyCode) {
        return CurrencyType.valueOf(currencyCode.toUpperCase()).orElseThrow();
    }
}
