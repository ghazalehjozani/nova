package ir.dotin.loan.trade.adapters.driven.persistence.loanfacility.repository;

import org.springframework.stereotype.Repository;

import ir.dotin.platform.pangaea.outbox.jpa.repository.BaseOutboxRepository;
import ir.dotin.loan.trade.adapters.driven.persistence.loanfacility.entity.TradeLoanFacilityOutboxEventEntity;

@Repository
public interface LoanFacilityOutboxRepository extends BaseOutboxRepository<TradeLoanFacilityOutboxEventEntity> {}
