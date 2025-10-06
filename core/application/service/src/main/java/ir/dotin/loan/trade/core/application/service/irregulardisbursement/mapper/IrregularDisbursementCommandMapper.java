package ir.dotin.loan.trade.core.application.service.irregulardisbursement.mapper;

import org.mapstruct.Mapper;

import ir.dotin.platform.commons.domain.vo.Money;
import ir.dotin.loan.trade.core.application.ports.inbound.dto.MoneyDto;

@Mapper
public interface IrregularDisbursementCommandMapper {

    Money toMoney(MoneyDto dto);
}
