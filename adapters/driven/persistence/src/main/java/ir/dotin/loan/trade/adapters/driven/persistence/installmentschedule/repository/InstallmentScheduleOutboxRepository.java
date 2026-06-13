package ir.dotin.loan.trade.adapters.driven.persistence.installmentschedule.repository;

import org.springframework.stereotype.Repository;

import ir.dotin.platform.pangaea.outbox.jpa.repository.BaseOutboxRepository;
import ir.dotin.loan.trade.adapters.driven.persistence.installmentschedule.entity.InstallmentScheduleOutboxEventEntity;

@Repository
public interface InstallmentScheduleOutboxRepository
        extends BaseOutboxRepository<InstallmentScheduleOutboxEventEntity> {}
