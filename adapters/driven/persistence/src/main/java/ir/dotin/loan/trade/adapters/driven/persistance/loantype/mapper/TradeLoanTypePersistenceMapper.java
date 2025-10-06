package ir.dotin.loan.trade.adapters.driven.persistance.loantype.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import ir.dotin.loan.trade.adapters.driven.persistance.loantype.entity.TradeLoanTypeEntity;
import ir.dotin.loan.trade.adapters.driven.persistance.mapper.BaseMapperConfig;
import ir.dotin.loan.trade.adapters.driven.persistance.mapper.ValueObjectMapper;
import ir.dotin.loan.trade.core.domain.loantype.entity.TradeLoanType;

@Mapper(config = BaseMapperConfig.class, uses = ValueObjectMapper.class)
public interface TradeLoanTypePersistenceMapper {

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "modifiedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    @Mapping(
            source = "relationTypeLoanTopics",
            target = "relationTypeLoanTopics",
            qualifiedByName = "mapRelationTypeLoanTopicsEmbs")
    TradeLoanTypeEntity map(TradeLoanType domain);

    @Mapping(
            source = "relationTypeLoanTopics",
            target = "relationTypeLoanTopics",
            qualifiedByName = "mapEmbsRelationTypeLoanTopics")
    TradeLoanType map(TradeLoanTypeEntity entity);
}
