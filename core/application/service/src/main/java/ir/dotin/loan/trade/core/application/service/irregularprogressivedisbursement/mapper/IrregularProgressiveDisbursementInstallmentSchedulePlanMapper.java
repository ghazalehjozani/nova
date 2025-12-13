package ir.dotin.loan.trade.core.application.service.irregularprogressivedisbursement.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import ir.dotin.platform.commons.domain.vo.CurrencyType;
import ir.dotin.platform.commons.domain.vo.Money;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.vo.InstallmentSpec;
import ir.dotin.loan.trade.core.application.ports.inbound.command.IrregularProgressiveDisbursementCommand.InstallmentSpecDto;

@Component
public class IrregularProgressiveDisbursementInstallmentSchedulePlanMapper {

    public List<InstallmentSpec> mapSpecs(List<InstallmentSpecDto> dtos, CurrencyType currency) {
        return dtos.stream().map(dto -> mapSpec(dto, currency)).toList();
    }

    private InstallmentSpec mapSpec(InstallmentSpecDto dto, CurrencyType currency) {
        return InstallmentSpec.builder()
                .sequenceNumber(dto.sequenceNumber())
                .dueDate(dto.dueDate())
                .principalAmount(
                        Money.valueOf(dto.principalAmount().value(), currency).value())
                .interestAmount(
                        Money.valueOf(dto.interestAmount().value(), currency).value())
                .build();
    }
}
