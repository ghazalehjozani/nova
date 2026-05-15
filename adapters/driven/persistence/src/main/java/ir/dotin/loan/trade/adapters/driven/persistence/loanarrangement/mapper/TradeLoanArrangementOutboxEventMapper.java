package ir.dotin.loan.trade.adapters.driven.persistence.loanarrangement.mapper;

import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

import ir.dotin.platform.adapter.messaging.persistence.mapper.BaseOutboxEventMapper;
import ir.dotin.platform.commons.domain.entity.AbstractAggregateRoot;
import ir.dotin.platform.outbox.api.OutboxEvent;
import ir.dotin.loan.trade.adapters.driven.persistence.loanarrangement.entity.TradeLoanArrangementOutboxEventEntity;
import ir.dotin.loan.trade.core.domain.loanarrangement.entity.TradeLoanArrangement;

@Mapper(config = BaseOutboxEventMapper.class)
public interface TradeLoanArrangementOutboxEventMapper
        extends BaseOutboxEventMapper<TradeLoanArrangementOutboxEventEntity> {

    @Override
    @InheritConfiguration
    TradeLoanArrangementOutboxEventEntity toEntity(OutboxEvent domain);

    @Override
    @InheritConfiguration
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
