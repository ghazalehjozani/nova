package ir.dotin.loan.trade.adapters.driven.persistence.loanfacility;

import org.springframework.stereotype.Component;

import ir.dotin.platform.pangaea.commons.domain.entity.AbstractAggregateRoot;
import ir.dotin.platform.pangaea.messaging.persistence.handler.OutboxHandler;
import ir.dotin.loan.trade.adapters.driven.persistence.loanfacility.entity.TradeLoanFacilityOutboxEventEntity;
import ir.dotin.loan.trade.adapters.driven.persistence.loanfacility.mapper.LoanApplicationOutboxEventMapper;
import ir.dotin.loan.trade.adapters.driven.persistence.loanfacility.repository.LoanFacilityOutboxRepository;
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
