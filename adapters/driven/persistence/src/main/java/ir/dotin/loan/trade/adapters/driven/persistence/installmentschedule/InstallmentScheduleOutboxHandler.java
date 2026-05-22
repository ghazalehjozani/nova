package ir.dotin.loan.trade.adapters.driven.persistence.installmentschedule;

import org.springframework.stereotype.Service;

import ir.dotin.platform.pangaea.adapter.messaging.persistence.handler.OutboxHandler;
import ir.dotin.platform.pangaea.adapter.messaging.persistence.repository.BaseOutboxRepository;
import ir.dotin.platform.pangaea.commons.domain.entity.AbstractAggregateRoot;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.entity.InstallmentSchedule;
import ir.dotin.loan.trade.adapters.driven.persistence.installmentschedule.entity.InstallmentScheduleOutboxEventEntity;
import ir.dotin.loan.trade.adapters.driven.persistence.installmentschedule.mapper.InstallmentScheduleOutboxEventMapper;
import ir.dotin.loan.trade.adapters.driven.persistence.installmentschedule.repository.InstallmentScheduleOutboxRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InstallmentScheduleOutboxHandler
        implements OutboxHandler<InstallmentScheduleOutboxEventEntity, InstallmentScheduleOutboxEventMapper> {

    private final InstallmentScheduleOutboxRepository repository;
    private final InstallmentScheduleOutboxEventMapper mapper;

    @Override
    public Class<? extends AbstractAggregateRoot<?>> aggregateType() {
        return InstallmentSchedule.class;
    }

    @Override
    public BaseOutboxRepository<InstallmentScheduleOutboxEventEntity> repository() {
        return repository;
    }

    @Override
    public InstallmentScheduleOutboxEventMapper mapper() {
        return mapper;
    }
}
