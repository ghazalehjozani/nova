package ir.dotin.loan.trade.core.application.service.restructuringfacility.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import ir.dotin.platform.pangaea.commons.domain.vo.CurrencyType;
import ir.dotin.platform.pangaea.commons.domain.vo.Money;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.vo.InstallmentSpec;
import ir.dotin.loan.trade.core.application.ports.inbound.command.LoanFacilityRestructuringCommand;

@Component
public class LoanFacilityRestructuringInstallmentSchedulePlanMapper {

    public List<InstallmentSpec> mapSpecs(
            List<LoanFacilityRestructuringCommand.InstallmentDetailsItem> dtos, CurrencyType currency) {
        return dtos.stream().map(dto -> mapSpec(dto, currency)).toList();
    }

    private InstallmentSpec mapSpec(
            LoanFacilityRestructuringCommand.InstallmentDetailsItem dto, CurrencyType currency) {
        return InstallmentSpec.builder()
                .sequenceNumber(dto.sequenceNumber())
                .dueDate(dto.dueDate())
                .principalAmount(
                        Money.valueOf(dto.principalAmount().value(), currency).unwrap())
                .interestAmount(
                        Money.valueOf(dto.interestAmount().value(), currency).unwrap())
                .build();
    }
}
