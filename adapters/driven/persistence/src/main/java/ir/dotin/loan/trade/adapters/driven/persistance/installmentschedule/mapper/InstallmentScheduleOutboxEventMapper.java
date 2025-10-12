package ir.dotin.loan.trade.adapters.driven.persistance.installmentschedule.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import ir.dotin.platform.adapter.messaging.outbox.model.OutboxEvent;
import ir.dotin.platform.adapter.messaging.persistence.mapper.BaseOutboxEventMapper;
import ir.dotin.platform.commons.domain.entity.AbstractAggregateRoot;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.entity.InstallmentSchedule;
import ir.dotin.loan.trade.adapters.driven.persistance.installmentschedule.entity.InstallmentScheduleOutboxEventEntity;

@Mapper(config = BaseOutboxEventMapper.class)
public interface InstallmentScheduleOutboxEventMapper
        extends BaseOutboxEventMapper<InstallmentScheduleOutboxEventEntity> {

    @Override
    @Mapping(target = "aggregateType", source = "aggregateType", qualifiedByName = "classToString")
    InstallmentScheduleOutboxEventEntity toEntity(OutboxEvent domain);

    @Override
    @Mapping(target = "aggregateType", source = "aggregateType", qualifiedByName = "stringToClass")
    OutboxEvent toDomain(InstallmentScheduleOutboxEventEntity entity);

    @Named("classToString")
    default String classToString(Class<? extends AbstractAggregateRoot<?>> type) {
        return InstallmentSchedule.class.getSimpleName();
    }

    @Named("stringToClass")
    default Class<? extends AbstractAggregateRoot<?>> stringToClass(String type) {
        return InstallmentSchedule.class;
    }
}
