package ir.dotin.loan.trade.adapters.driven.persistence.installmentschedule.mapper;

import java.time.Clock;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

import ir.dotin.loan.baseloan.core.domain.installmentschedule.entity.InstallmentSchedule;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.event.InstallmentScheduleEventFactory;
import ir.dotin.loan.trade.adapters.driven.persistence.installmentschedule.entity.InstallmentScheduleEntity;
import ir.dotin.loan.trade.adapters.driven.persistence.mapper.BaseMapperConfig;
import ir.dotin.loan.trade.adapters.driven.persistence.mapper.ValueObjectMapper;

@Mapper(
        config = BaseMapperConfig.class,
        uses = {InstallmentPersistenceMapper.class, ValueObjectMapper.class})
public abstract class InstallmentSchedulePersistenceMapper {

    @Autowired
    private InstallmentScheduleEventFactory eventFactory;

    @Autowired
    private Clock clock;

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "modifiedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    public abstract InstallmentScheduleEntity map(InstallmentSchedule domain);

    @Mapping(target = "clock", ignore = true)
    @Mapping(target = "eventFactory", ignore = true)
    public abstract InstallmentSchedule.Builder map(InstallmentScheduleEntity entity);

    @AfterMapping
    protected void setFields(InstallmentScheduleEntity entity, @MappingTarget InstallmentSchedule.Builder builder) {
        builder.clock(clock);
        builder.eventFactory(eventFactory);
    }
}
