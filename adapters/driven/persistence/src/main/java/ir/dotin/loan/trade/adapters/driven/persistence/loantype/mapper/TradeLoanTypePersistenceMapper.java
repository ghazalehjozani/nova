package ir.dotin.loan.trade.adapters.driven.persistence.loantype.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import ir.dotin.loan.trade.adapters.driven.persistence.loantype.entity.TradeLoanTypeEntity;
import ir.dotin.loan.trade.adapters.driven.persistence.mapper.BaseMapperConfig;
import ir.dotin.loan.trade.adapters.driven.persistence.mapper.ValueObjectMapper;
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
