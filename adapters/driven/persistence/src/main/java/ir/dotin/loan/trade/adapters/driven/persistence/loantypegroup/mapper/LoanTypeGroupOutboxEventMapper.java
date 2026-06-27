package ir.dotin.loan.trade.adapters.driven.persistence.loantypegroup.mapper;

import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

import ir.dotin.platform.pangaea.commons.domain.entity.AbstractAggregateRoot;
import ir.dotin.platform.pangaea.outbox.api.OutboxEvent;
import ir.dotin.platform.pangaea.outbox.jpa.mapper.BaseOutboxEventMapper;
import ir.dotin.loan.baseloan.core.domain.loantypegroup.aggregate.LoanTypeGroup;
import ir.dotin.loan.trade.adapters.driven.persistence.loantypegroup.entity.LoanTypeGroupOutboxEventEntity;

@Mapper(config = BaseOutboxEventMapper.class)
public interface LoanTypeGroupOutboxEventMapper extends BaseOutboxEventMapper<LoanTypeGroupOutboxEventEntity> {

    @Override
    @InheritConfiguration
    LoanTypeGroupOutboxEventEntity toEntity(OutboxEvent domain);

    @Override
    @InheritConfiguration
    OutboxEvent toDomain(LoanTypeGroupOutboxEventEntity entity);

    @Named("classToString")
    default String classToString(Class<? extends AbstractAggregateRoot<?>> type) {
        return LoanTypeGroup.class.getSimpleName();
    }

    @Named("stringToClass")
    default Class<? extends AbstractAggregateRoot<?>> stringToClass(String type) {
        return LoanTypeGroup.class;
    }
}
