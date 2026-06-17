package ir.dotin.loan.trade.core.application.service.originateloanfacility.component.validation;

import java.util.Objects;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.Samat;
import ir.dotin.loan.trade.core.application.ports.inbound.command.OriginateLoanFacilityCommand;
import ir.dotin.loan.trade.core.application.ports.inbound.dto.EconomicSectorDto;
import ir.dotin.loan.trade.core.application.ports.inbound.dto.SamatDto;
import ir.dotin.loan.trade.core.application.ports.outbound.client.samat.ValidateSamatPort;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SamatValidationRule {

    private final ValidateSamatPort validateSamatPort;

    public Result<Unit> validateSamat(OriginateLoanFacilityCommand command) {
        SamatDto samatDto = command.loanApplication().samat();
        EconomicSectorDto economicSectorDto = command.loanApplication().economicSector();
        Samat samat = samatDtoToSamat(samatDto);
        if (samat == null) {
            return Result.success();
        }
        return validateSamatPort.validateSamat(samat, command.loanTypeCode(), economicSectorDto.code());
    }

    protected @Nullable Samat samatDtoToSamat(@Nullable SamatDto samatDto) {
        if (samatDto == null) {
            return null;
        }

        String isicEconomicSector = null;
        String subIsicEconomicSector = null;
        String useType = null;
        String exceptionCode = null;
        String consumptionPlaceCode = null;

        String trackingNumber = samatDto.trackingNumber();
        if (samatDto.isicEconomicSector() != null) {
            isicEconomicSector = samatDto.isicEconomicSector();
        }
        if (samatDto.subIsicEconomicSector() != null) {
            subIsicEconomicSector = samatDto.subIsicEconomicSector();
        }
        if (samatDto.useType() != null) {
            useType = samatDto.useType();
        }
        if (samatDto.exceptionCode() != null) {
            exceptionCode = samatDto.exceptionCode();
        }
        if (samatDto.consumptionPlaceCode() != null) {
            consumptionPlaceCode = samatDto.consumptionPlaceCode();
        }

        return new Samat(
                trackingNumber,
                Objects.requireNonNullElse(isicEconomicSector, ""),
                Objects.requireNonNullElse(subIsicEconomicSector, ""),
                Objects.requireNonNullElse(useType, ""),
                Objects.requireNonNullElse(exceptionCode, ""),
                Objects.requireNonNullElse(consumptionPlaceCode, ""));
    }
}
