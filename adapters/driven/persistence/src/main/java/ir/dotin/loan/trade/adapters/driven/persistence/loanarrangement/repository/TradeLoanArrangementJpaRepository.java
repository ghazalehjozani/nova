package ir.dotin.loan.trade.adapters.driven.persistence.loanarrangement.repository;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import ir.dotin.platform.adapter.persistence.repository.PersistentRepository;
import ir.dotin.loan.trade.adapters.driven.persistence.loanarrangement.entity.TradeLoanArrangementEntity;

@Repository
public interface TradeLoanArrangementJpaRepository extends PersistentRepository<TradeLoanArrangementEntity> {

    boolean existsByCode(@NonNull String code);
}
