package ir.dotin.loan.trade.adapters.driven.persistence.loanfacility.repository;

import org.springframework.stereotype.Repository;

import ir.dotin.platform.adapter.persistence.repository.PersistentRepository;
import ir.dotin.loan.trade.adapters.driven.persistence.loanfacility.entity.TradeLoanFacilityEntity;

@Repository
public interface TradeLoanFacilityJpaRepository extends PersistentRepository<TradeLoanFacilityEntity> {}
