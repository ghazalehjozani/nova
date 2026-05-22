package ir.dotin.loan.trade.adapters.driven.persistence.loanarrangement;

import org.springframework.stereotype.Service;

import ir.dotin.platform.pangaea.commons.domain.entity.AbstractAggregateRoot;
import ir.dotin.platform.pangaea.messaging.persistence.handler.OutboxHandler;
import ir.dotin.loan.trade.adapters.driven.persistence.loanarrangement.entity.TradeLoanArrangementOutboxEventEntity;
import ir.dotin.loan.trade.adapters.driven.persistence.loanarrangement.mapper.TradeLoanArrangementOutboxEventMapper;
import ir.dotin.loan.trade.adapters.driven.persistence.loanarrangement.repository.TradeLoanArrangementOutboxRepository;
import ir.dotin.loan.trade.core.domain.loanarrangement.entity.TradeLoanArrangement;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TradeLoanArrangementOutboxHandler
        implements OutboxHandler<TradeLoanArrangementOutboxEventEntity, TradeLoanArrangementOutboxEventMapper> {

    private final TradeLoanArrangementOutboxRepository repository;
    private final TradeLoanArrangementOutboxEventMapper mapper;

    @Override
    public Class<? extends AbstractAggregateRoot<?>> aggregateType() {
        return TradeLoanArrangement.class;
    }

    @Override
    public TradeLoanArrangementOutboxRepository repository() {
        return repository;
    }

    @Override
    public TradeLoanArrangementOutboxEventMapper mapper() {
        return mapper;
    }
}
