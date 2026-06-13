package ir.dotin.loan.trade.adapters.driven.persistence.loanfacility.mapper;

import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

import ir.dotin.platform.pangaea.commons.domain.entity.AbstractAggregateRoot;
import ir.dotin.platform.pangaea.outbox.api.OutboxEvent;
import ir.dotin.platform.pangaea.outbox.jpa.mapper.BaseOutboxEventMapper;
import ir.dotin.loan.trade.adapters.driven.persistence.loanfacility.entity.TradeLoanFacilityOutboxEventEntity;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;

@Mapper(config = BaseOutboxEventMapper.class)
public interface LoanApplicationOutboxEventMapper extends BaseOutboxEventMapper<TradeLoanFacilityOutboxEventEntity> {

    @Override
    @InheritConfiguration
    TradeLoanFacilityOutboxEventEntity toEntity(OutboxEvent domain);

    @Override
    @InheritConfiguration
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
