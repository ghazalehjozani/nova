package ir.dotin.loan.trade.adapters.driven.persistance.installmentschedule.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import ir.dotin.loan.baseloan.core.domain.installmentschedule.entity.InstallmentSchedule;
import ir.dotin.loan.trade.adapters.driven.persistance.installmentschedule.entity.InstallmentScheduleEntity;
import ir.dotin.loan.trade.adapters.driven.persistance.mapper.BaseMapperConfig;
import ir.dotin.loan.trade.adapters.driven.persistance.mapper.ValueObjectMapper;

@Mapper(
        config = BaseMapperConfig.class,
        uses = {InstallmentPersistenceMapper.class, ValueObjectMapper.class})
public interface InstallmentSchedulePersistenceMapper {

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "modifiedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    InstallmentScheduleEntity map(InstallmentSchedule domain);

    @Mapping(target = "clock", ignore = true)
    @Mapping(target = "eventFactory", ignore = true)
    InstallmentSchedule map(InstallmentScheduleEntity entity);
}
