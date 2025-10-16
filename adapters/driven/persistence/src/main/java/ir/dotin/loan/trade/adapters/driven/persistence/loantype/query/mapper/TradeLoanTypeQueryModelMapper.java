package ir.dotin.loan.trade.adapters.driven.persistence.loantype.query.mapper;

import org.mapstruct.Mapper;

import ir.dotin.loan.trade.adapters.driven.persistence.loantype.entity.TradeLoanTypeEntity;
import ir.dotin.loan.trade.adapters.driven.persistence.mapper.BaseMapperConfig;
import ir.dotin.loan.trade.core.application.ports.outbound.query.request.TradeLoanTypeQueryDto;

@Mapper(config = BaseMapperConfig.class)
public interface TradeLoanTypeQueryModelMapper {
    TradeLoanTypeQueryDto toQueryModel(TradeLoanTypeEntity entity);
}
