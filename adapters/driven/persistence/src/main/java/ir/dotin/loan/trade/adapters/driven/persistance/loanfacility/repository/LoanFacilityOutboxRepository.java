package ir.dotin.loan.trade.adapters.driven.persistance.loanfacility.repository;

import org.springframework.stereotype.Repository;

import ir.dotin.platform.adapter.messaging.persistence.repository.BaseOutboxRepository;
import ir.dotin.loan.trade.adapters.driven.persistance.loanfacility.entity.TradeLoanFacilityOutboxEventEntity;

@Repository
public interface LoanFacilityOutboxRepository extends BaseOutboxRepository<TradeLoanFacilityOutboxEventEntity> {}
