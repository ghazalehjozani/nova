package ir.dotin.loan.trade.adapters.driven.persistence.loanfacility.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import ir.dotin.platform.adapter.messaging.outbox.model.OutboxEvent;
import ir.dotin.platform.adapter.messaging.persistence.mapper.BaseOutboxEventMapper;
import ir.dotin.platform.commons.domain.entity.AbstractAggregateRoot;
import ir.dotin.loan.trade.adapters.driven.persistence.loanfacility.entity.TradeLoanFacilityOutboxEventEntity;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;

@Mapper(config = BaseOutboxEventMapper.class)
public interface LoanApplicationOutboxEventMapper extends BaseOutboxEventMapper<TradeLoanFacilityOutboxEventEntity> {

    @Override
    @Mapping(target = "aggregateType", source = "aggregateType", qualifiedByName = "classToString")
    TradeLoanFacilityOutboxEventEntity toEntity(OutboxEvent domain);

    @Override
    @Mapping(target = "aggregateType", source = "aggregateType", qualifiedByName = "stringToClass")
    OutboxEvent toDomain(TradeLoanFacilityOutboxEventEntity entity);

    @Named("classToString")
    default String classToString(Class<? extends AbstractAggregateRoot<?>> type) {
        return TradeLoanFacility.class.getSimpleName();
    }

    @Named("stringToClass")
    default Class<? extends AbstractAggregateRoot<?>> stringToClass(String type) {
        return TradeLoanFacility.class;
    }
}
