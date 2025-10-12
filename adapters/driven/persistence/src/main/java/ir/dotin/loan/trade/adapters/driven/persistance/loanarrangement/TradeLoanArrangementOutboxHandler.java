package ir.dotin.loan.trade.adapters.driven.persistance.loanarrangement;

import org.springframework.stereotype.Service;

import ir.dotin.platform.adapter.messaging.persistence.handler.OutboxHandler;
import ir.dotin.platform.commons.domain.entity.AbstractAggregateRoot;
import ir.dotin.loan.trade.adapters.driven.persistance.loanarrangement.entity.TradeLoanArrangementOutboxEventEntity;
import ir.dotin.loan.trade.adapters.driven.persistance.loanarrangement.mapper.TradeLoanArrangementEventMapper;
import ir.dotin.loan.trade.adapters.driven.persistance.loanarrangement.repository.TradeLoanArrangementOutboxRepository;
import ir.dotin.loan.trade.core.domain.loanarrangement.entity.TradeLoanArrangement;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TradeLoanArrangementOutboxHandler
        implements OutboxHandler<TradeLoanArrangementOutboxEventEntity, TradeLoanArrangementEventMapper> {

    private final TradeLoanArrangementOutboxRepository repository;
    private final TradeLoanArrangementEventMapper mapper;

    @Override
    public Class<? extends AbstractAggregateRoot<?>> aggregateType() {
        return TradeLoanArrangement.class;
    }

    @Override
    public TradeLoanArrangementOutboxRepository repository() {
        return repository;
    }

    @Override
    public TradeLoanArrangementEventMapper mapper() {
        return mapper;
    }
}
