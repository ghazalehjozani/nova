package ir.dotin.loan.trade.adapters.driven.persistence.loantype.repository;

import org.springframework.stereotype.Repository;

import ir.dotin.platform.adapter.messaging.persistence.repository.BaseOutboxRepository;
import ir.dotin.loan.trade.adapters.driven.persistence.loantype.entity.TradeLoanTypeOutboxEventEntity;

@Repository
public interface TradeLoanTypeOutboxEventRepository extends BaseOutboxRepository<TradeLoanTypeOutboxEventEntity> {}
