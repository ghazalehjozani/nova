package ir.dotin.loan.trade.adapters.driven.persistence.installmentschedule.mapper;

import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

import ir.dotin.platform.adapter.messaging.persistence.mapper.BaseOutboxEventMapper;
import ir.dotin.platform.commons.domain.entity.AbstractAggregateRoot;
import ir.dotin.platform.outbox.api.OutboxEvent;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.entity.InstallmentSchedule;
import ir.dotin.loan.trade.adapters.driven.persistence.installmentschedule.entity.InstallmentScheduleOutboxEventEntity;

@Mapper(config = BaseOutboxEventMapper.class)
public interface InstallmentScheduleOutboxEventMapper
        extends BaseOutboxEventMapper<InstallmentScheduleOutboxEventEntity> {

    @Override
    @InheritConfiguration
    InstallmentScheduleOutboxEventEntity toEntity(OutboxEvent domain);

    @Override
    @InheritConfiguration
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
