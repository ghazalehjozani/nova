package ir.dotin.loan.trade.adapters.driven.persistence.loanfacility.query.mapper;

import org.mapstruct.Mapper;

import ir.dotin.loan.trade.adapters.driven.persistence.loanfacility.entity.TradeLoanFacilityEntity;
import ir.dotin.loan.trade.adapters.driven.persistence.mapper.BaseMapperConfig;
import ir.dotin.loan.trade.core.application.ports.outbound.query.dto.TradeFacilityQueryDto;

@Mapper(config = BaseMapperConfig.class)
public interface FacilityQueryModelMapper {

    TradeFacilityQueryDto toQueryModel(TradeLoanFacilityEntity entity);
}
