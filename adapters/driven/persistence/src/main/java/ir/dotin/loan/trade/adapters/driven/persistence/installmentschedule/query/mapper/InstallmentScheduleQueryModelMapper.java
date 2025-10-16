package ir.dotin.loan.trade.adapters.driven.persistence.installmentschedule.query.mapper;

import org.mapstruct.Mapper;

import ir.dotin.loan.trade.adapters.driven.persistence.installmentschedule.entity.InstallmentScheduleEntity;
import ir.dotin.loan.trade.adapters.driven.persistence.mapper.BaseMapperConfig;
import ir.dotin.loan.trade.core.application.ports.outbound.query.request.TradeInstallmentScheduleQueryDto;

@Mapper(config = BaseMapperConfig.class, uses = InstallmentQueryModelMapper.class)
public interface InstallmentScheduleQueryModelMapper {

    TradeInstallmentScheduleQueryDto toDto(InstallmentScheduleEntity installmentScheduleEntity);
}
