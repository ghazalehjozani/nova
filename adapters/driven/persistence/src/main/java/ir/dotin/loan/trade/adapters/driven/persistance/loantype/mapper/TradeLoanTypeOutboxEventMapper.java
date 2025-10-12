package ir.dotin.loan.trade.adapters.driven.persistance.loantype.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import ir.dotin.platform.adapter.messaging.outbox.model.OutboxEvent;
import ir.dotin.platform.adapter.messaging.persistence.mapper.BaseOutboxEventMapper;
import ir.dotin.platform.commons.domain.entity.AbstractAggregateRoot;
import ir.dotin.loan.trade.adapters.driven.persistance.loantype.entity.TradeLoanTypeOutboxEventEntity;
import ir.dotin.loan.trade.core.domain.loantype.entity.TradeLoanType;

@Mapper(config = BaseOutboxEventMapper.class)
public interface TradeLoanTypeOutboxEventMapper extends BaseOutboxEventMapper<TradeLoanTypeOutboxEventEntity> {

    @Override
    @Mapping(target = "aggregateType", source = "aggregateType", qualifiedByName = "classToString")
    TradeLoanTypeOutboxEventEntity toEntity(OutboxEvent domain);

    @Override
    @Mapping(target = "aggregateType", source = "aggregateType", qualifiedByName = "stringToClass")
    OutboxEvent toDomain(TradeLoanTypeOutboxEventEntity entity);

    @Named("classToString")
    default String classToString(Class<? extends AbstractAggregateRoot<?>> type) {
        return TradeLoanType.class.getSimpleName();
    }

    @Named("stringToClass")
    default Class<? extends AbstractAggregateRoot<?>> stringToClass(String type) {
        return TradeLoanType.class;
    }
}
