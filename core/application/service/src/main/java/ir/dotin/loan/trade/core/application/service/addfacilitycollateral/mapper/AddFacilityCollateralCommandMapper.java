package ir.dotin.loan.trade.core.application.service.addfacilitycollateral.mapper;

import org.mapstruct.Mapper;

import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.commons.domain.vo.CurrencyType;
import ir.dotin.platform.commons.domain.vo.Money;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.CollateralSerial;
import ir.dotin.loan.trade.core.application.ports.inbound.command.AddFacilityCollateralCommand;
import ir.dotin.loan.trade.core.application.service.BaseMapperConfig;

@Mapper(config = BaseMapperConfig.class)
public interface AddFacilityCollateralCommandMapper {

    CollateralSerial toCollateralSerial(AddFacilityCollateralCommand.CollateralSerialDto dto);

    default Money toMoney(AddFacilityCollateralCommand.MoneyDto dto) {
        if (dto == null) {
            return null;
        }

        CurrencyType currency = mapCurrency(dto.currency());

        Result<Money> moneyResult = Money.valueOf(dto.value(), currency);

        return moneyResult.orElseThrow(() -> new IllegalArgumentException(
                "Invalid money value: " + moneyResult.notification().errors()));
    }

    default CurrencyType mapCurrency(String currencyCode) {
        if (currencyCode == null || currencyCode.isBlank()) {
            throw new IllegalArgumentException("Currency code is required in MoneyDto");
        }
        try {
            return CurrencyType.valueOf(currencyCode.toUpperCase()).orElseThrow();
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid currency code: " + currencyCode);
        }
    }

    default CurrencyType map(AddFacilityCollateralCommand.CurrencyTypeDto dto) {
        return mapCurrency(dto.value());
    }
}
