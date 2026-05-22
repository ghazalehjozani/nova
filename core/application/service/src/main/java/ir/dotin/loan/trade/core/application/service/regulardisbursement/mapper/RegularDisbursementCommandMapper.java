package ir.dotin.loan.trade.core.application.service.regulardisbursement.mapper;

import org.mapstruct.Mapper;

import ir.dotin.platform.pangaea.commons.domain.vo.CurrencyType;
import ir.dotin.platform.pangaea.commons.domain.vo.Money;
import ir.dotin.loan.trade.core.application.ports.inbound.dto.MoneyDto;
import ir.dotin.loan.trade.core.application.service.BaseMapperConfig;

@Mapper(config = BaseMapperConfig.class)
public interface RegularDisbursementCommandMapper {

    Money toMoney(MoneyDto dto);

    default CurrencyType map(String currencyCode) {
        return CurrencyType.valueOf(currencyCode.toUpperCase()).unwrap();
    }
}
