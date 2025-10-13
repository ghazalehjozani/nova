package ir.dotin.loan.trade.adapters.driven.persistence.loanarrangement.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import ir.dotin.platform.adapter.messaging.outbox.model.OutboxEvent;
import ir.dotin.platform.adapter.messaging.persistence.mapper.BaseOutboxEventMapper;
import ir.dotin.platform.commons.domain.entity.AbstractAggregateRoot;
import ir.dotin.loan.trade.adapters.driven.persistence.loanarrangement.entity.TradeLoanArrangementOutboxEventEntity;
import ir.dotin.loan.trade.core.domain.loanarrangement.entity.TradeLoanArrangement;

@Mapper(config = BaseOutboxEventMapper.class)
public interface TradeLoanArrangementOutboxEventMapper
        extends BaseOutboxEventMapper<TradeLoanArrangementOutboxEventEntity> {

    @Override
    @Mapping(target = "aggregateType", source = "aggregateType", qualifiedByName = "classToString")
    TradeLoanArrangementOutboxEventEntity toEntity(OutboxEvent domain);

    @Override
    @Mapping(target = "aggregateType", source = "aggregateType", qualifiedByName = "stringToClass")
    OutboxEvent toDomain(TradeLoanArrangementOutboxEventEntity entity);

    @Named("classToString")
    default String classToString(Class<? extends AbstractAggregateRoot<?>> type) {
        return TradeLoanArrangement.class.getSimpleName();
    }

    @Named("stringToClass")
    default Class<? extends AbstractAggregateRoot<?>> stringToClass(String type) {
        return TradeLoanArrangement.class;
    }
}
