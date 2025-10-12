package ir.dotin.loan.trade.adapters.driven.persistance.loanarrangement.repository;

import org.springframework.stereotype.Repository;

import ir.dotin.platform.adapter.messaging.persistence.repository.BaseOutboxRepository;
import ir.dotin.loan.trade.adapters.driven.persistance.loanarrangement.entity.TradeLoanArrangementOutboxEventEntity;

@Repository
public interface TradeLoanArrangementOutboxRepository
        extends BaseOutboxRepository<TradeLoanArrangementOutboxEventEntity> {}
