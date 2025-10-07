package ir.dotin.loan.trade.adapters.driven.persistance.loanfacility.query.mapper;

import org.mapstruct.Mapper;

import ir.dotin.loan.trade.adapters.driven.persistance.loanfacility.entity.TradeLoanFacilityEntity;
import ir.dotin.loan.trade.adapters.driven.persistance.mapper.BaseMapperConfig;
import ir.dotin.loan.trade.core.application.ports.outbound.query.dto.FacilityQueryDto;

@Mapper(config = BaseMapperConfig.class)
public interface FacilityQueryModelMapper {

    FacilityQueryDto toQueryModel(TradeLoanFacilityEntity entity);
}
