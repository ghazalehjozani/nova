package ir.dotin.loan.trade.adapters.driven.persistance.loanarrangement.repository;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import ir.dotin.platform.adapter.persistence.repository.PersistentRepository;
import ir.dotin.loan.trade.adapters.driven.persistance.loanarrangement.entity.TradeLoanArrangementEntity;

@Repository
public interface TradeLoanArrangementJpaRepository extends PersistentRepository<TradeLoanArrangementEntity> {

    boolean existsByCode(String code);

    Optional<TradeLoanArrangementEntity> findByCode(String code);
}
