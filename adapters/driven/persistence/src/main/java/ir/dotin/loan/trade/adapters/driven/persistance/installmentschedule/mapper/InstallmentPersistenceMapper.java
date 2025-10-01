package ir.dotin.loan.trade.adapters.driven.persistance.installmentschedule.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import ir.dotin.loan.baseloan.core.domain.installmentschedule.entity.Installment;
import ir.dotin.loan.trade.adapters.driven.persistance.installmentschedule.entity.InstallmentEntity;
import ir.dotin.loan.trade.adapters.driven.persistance.mapper.BaseMapperConfig;
import ir.dotin.loan.trade.adapters.driven.persistance.mapper.ValueObjectMapper;

@Mapper(config = BaseMapperConfig.class, uses = ValueObjectMapper.class)
public interface InstallmentPersistenceMapper {

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "modifiedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    @Mapping(target = "installmentSchedule", ignore = true)
    InstallmentEntity map(Installment domain);

    Installment map(InstallmentEntity entity);
}
