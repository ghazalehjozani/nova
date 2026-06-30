package ir.dotin.loan.trade.core.application.service.updatefacilitycollateral.mapper;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

import org.jspecify.annotations.Nullable;
import org.mapstruct.Mapper;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.domain.vo.CurrencyType;
import ir.dotin.platform.pangaea.commons.domain.vo.Money;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.enums.CollateralType;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.Collateral;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.CollateralSerial;
import ir.dotin.loan.trade.core.application.ports.inbound.command.UpdateCollateralCommand;
import ir.dotin.loan.trade.core.application.service.BaseMapperConfig;

@Mapper(config = BaseMapperConfig.class)
public interface UpdateCollateralCommandMapper {

    List<Collateral> toCollaterals(List<UpdateCollateralCommand.CollateralDto> dtos);

    default @Nullable Collateral toCollateral(UpdateCollateralCommand.CollateralDto dto) {
        if (dto == null) {
            return null;
        }

        CollateralSerial serial = CollateralSerial.of(dto.collateralSerial()).unwrap();
        CollateralType type = dto.collateralTypeCode();
        Money usedAmount = Objects.requireNonNull(toMoney(dto.usedAmount()), "usedAmount is required");

        return Collateral.valueOf(type, dto.description(), serial, usedAmount)
                .unwrapOrThrow(c -> new IllegalArgumentException("Invalid collateral data"));
    }

    default @Nullable Money toMoney(UpdateCollateralCommand.MoneyDto dto) {
        if (dto == null) {
            return null;
        }
        CurrencyType currency = mapCurrency(dto.currency());
        Result<Money> moneyResult = Money.valueOf(dto.value(), currency);
        return moneyResult.unwrapOrThrow(c -> new IllegalArgumentException(
                "Invalid money value: " + c.notification().errors()));
    }

    default CurrencyType mapCurrency(String currencyCode) {
        if (currencyCode == null || currencyCode.isBlank()) {
            throw new IllegalArgumentException("Currency code is required in MoneyDto");
        }
        try {
            return CurrencyType.valueOf(currencyCode.toUpperCase(Locale.ROOT)).unwrap();
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid currency code: " + currencyCode);
        }
    }
}
