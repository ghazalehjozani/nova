package ir.dotin.loan.trade.adapters.driven.persistance.loanfacility.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import ir.dotin.loan.trade.adapters.driven.persistance.loanfacility.entity.TradeLoanApplicationEntity;
import ir.dotin.loan.trade.adapters.driven.persistance.mapper.BaseMapperConfig;
import ir.dotin.loan.trade.adapters.driven.persistance.mapper.ValueObjectMapper;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanApplication;

@Mapper(config = BaseMapperConfig.class, uses = ValueObjectMapper.class)
public interface TradeLoanApplicationPersistenceMapper {

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "modifiedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    TradeLoanApplicationEntity map(TradeLoanApplication domain);

    TradeLoanApplication map(TradeLoanApplicationEntity entity);
}
