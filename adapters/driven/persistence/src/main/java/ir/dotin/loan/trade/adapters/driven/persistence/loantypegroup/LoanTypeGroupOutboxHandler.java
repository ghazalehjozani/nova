package ir.dotin.loan.trade.adapters.driven.persistence.loantypegroup;

import org.springframework.stereotype.Service;

import ir.dotin.platform.pangaea.commons.domain.entity.AbstractAggregateRoot;
import ir.dotin.platform.pangaea.outbox.jpa.handler.OutboxHandler;
import ir.dotin.loan.baseloan.core.domain.loantypegroup.aggregate.LoanTypeGroup;
import ir.dotin.loan.trade.adapters.driven.persistence.loantypegroup.entity.LoanTypeGroupOutboxEventEntity;
import ir.dotin.loan.trade.adapters.driven.persistence.loantypegroup.mapper.LoanTypeGroupOutboxEventMapper;
import ir.dotin.loan.trade.adapters.driven.persistence.loantypegroup.repository.LoanTypeGroupOutboxEventRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LoanTypeGroupOutboxHandler
        implements OutboxHandler<LoanTypeGroupOutboxEventEntity, LoanTypeGroupOutboxEventMapper> {

    private final LoanTypeGroupOutboxEventRepository repository;
    private final LoanTypeGroupOutboxEventMapper mapper;

    @Override
    public Class<? extends AbstractAggregateRoot<?>> aggregateType() {
        return LoanTypeGroup.class;
    }

    @Override
    public LoanTypeGroupOutboxEventRepository repository() {
        return repository;
    }

    @Override
    public LoanTypeGroupOutboxEventMapper mapper() {
        return mapper;
    }
}
