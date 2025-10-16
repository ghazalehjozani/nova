package ir.dotin.loan.trade.adapters.driven.persistence.installmentschedule.query.mapper;

import org.mapstruct.Mapper;

import ir.dotin.loan.trade.adapters.driven.persistence.installmentschedule.entity.InstallmentEntity;
import ir.dotin.loan.trade.adapters.driven.persistence.mapper.BaseMapperConfig;
import ir.dotin.loan.trade.core.application.ports.outbound.query.request.TradeInstallmentQueryDto;

@Mapper(config = BaseMapperConfig.class)
public interface InstallmentQueryModelMapper {

    TradeInstallmentQueryDto toDto(InstallmentEntity installmentEntity);
}
