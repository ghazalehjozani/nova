package ir.dotin.loan.trade.adapters.driven.persistence.loantype;

import java.time.Clock;

import org.springframework.stereotype.Service;

import ir.dotin.platform.pangaea.commons.domain.entity.AbstractAggregateRoot;
import ir.dotin.platform.pangaea.outbox.jpa.handler.OutboxHandler;
import ir.dotin.loan.trade.adapters.driven.persistence.loantype.entity.TradeLoanTypeOutboxEventEntity;
import ir.dotin.loan.trade.adapters.driven.persistence.loantype.mapper.TradeLoanTypeOutboxEventMapper;
import ir.dotin.loan.trade.adapters.driven.persistence.loantype.repository.TradeLoanTypeOutboxEventRepository;
import ir.dotin.loan.trade.core.domain.loantype.entity.TradeLoanType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TradeLoanTypeOutboxHandler
        implements OutboxHandler<TradeLoanTypeOutboxEventEntity, TradeLoanTypeOutboxEventMapper> {

    private final TradeLoanTypeOutboxEventRepository repository;
    private final TradeLoanTypeOutboxEventMapper mapper;
    private final Clock clock;

    @Override
    public Class<? extends AbstractAggregateRoot<?>> aggregateType() {
        return TradeLoanType.class;
    }

    @Override
    public Clock clock() {
        return clock;
    }

    @Override
    public TradeLoanTypeOutboxEventRepository repository() {
        return repository;
    }

    @Override
    public TradeLoanTypeOutboxEventMapper mapper() {
        return mapper;
    }
}
