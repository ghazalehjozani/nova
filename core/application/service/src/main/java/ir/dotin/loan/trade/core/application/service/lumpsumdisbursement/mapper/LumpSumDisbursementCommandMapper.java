package ir.dotin.loan.trade.core.application.service.lumpsumdisbursement.mapper;

import org.mapstruct.Mapper;

import ir.dotin.platform.commons.domain.vo.Money;
import ir.dotin.loan.trade.core.application.ports.driven.dto.MoneyDto;

@Mapper
public interface LumpSumDisbursementCommandMapper {

    Money toMoney(MoneyDto dto);
}
