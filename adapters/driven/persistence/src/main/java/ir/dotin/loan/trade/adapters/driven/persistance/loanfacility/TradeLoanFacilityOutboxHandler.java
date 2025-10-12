package ir.dotin.loan.trade.adapters.driven.persistance.loanfacility;

import org.springframework.stereotype.Component;

import ir.dotin.platform.adapter.messaging.persistence.handler.OutboxHandler;
import ir.dotin.platform.commons.domain.entity.AbstractAggregateRoot;
import ir.dotin.loan.trade.adapters.driven.persistance.loanfacility.entity.TradeLoanFacilityOutboxEventEntity;
import ir.dotin.loan.trade.adapters.driven.persistance.loanfacility.mapper.LoanApplicationOutboxEventMapper;
import ir.dotin.loan.trade.adapters.driven.persistance.loanfacility.repository.LoanFacilityOutboxRepository;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TradeLoanFacilityOutboxHandler
        implements OutboxHandler<TradeLoanFacilityOutboxEventEntity, LoanApplicationOutboxEventMapper> {

    private final LoanFacilityOutboxRepository repository;
    private final LoanApplicationOutboxEventMapper mapper;

    @Override
    public Class<? extends AbstractAggregateRoot<?>> aggregateType() {
        return TradeLoanFacility.class;
    }

    @Override
    public LoanFacilityOutboxRepository repository() {
        return repository;
    }

    @Override
    public LoanApplicationOutboxEventMapper mapper() {
        return mapper;
    }
}
