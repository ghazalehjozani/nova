package ir.dotin.loan.trade.adapters.driven.persistence.loantype.mapper;

import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

import ir.dotin.platform.pangaea.commons.domain.entity.AbstractAggregateRoot;
import ir.dotin.platform.pangaea.messaging.persistence.mapper.BaseOutboxEventMapper;
import ir.dotin.platform.pangaea.outbox.api.OutboxEvent;
import ir.dotin.loan.trade.adapters.driven.persistence.loantype.entity.TradeLoanTypeOutboxEventEntity;
import ir.dotin.loan.trade.core.domain.loantype.entity.TradeLoanType;

@Mapper(config = BaseOutboxEventMapper.class)
public interface TradeLoanTypeOutboxEventMapper extends BaseOutboxEventMapper<TradeLoanTypeOutboxEventEntity> {

    @Override
    @InheritConfiguration
    TradeLoanTypeOutboxEventEntity toEntity(OutboxEvent domain);

    @Override
    @InheritConfiguration
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
