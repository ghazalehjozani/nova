package ir.dotin.loan.trade.adapters.driven.persistence.loantype.repository;

import org.springframework.stereotype.Repository;

import ir.dotin.platform.adapter.persistence.repository.PersistentRepository;
import ir.dotin.loan.trade.adapters.driven.persistence.loantype.entity.TradeLoanTypeEntity;

@Repository
public interface TradeLoanTypeJpaRepository extends PersistentRepository<TradeLoanTypeEntity> {}
