package ir.dotin.loan.trade.adapters.driven.persistence.loanarrangement.repository;

import org.springframework.data.domain.Limit;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Window;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import ir.dotin.platform.adapter.persistence.repository.PersistentRepository;
import ir.dotin.loan.trade.adapters.driven.persistence.loanarrangement.entity.TradeLoanArrangementEntity;

@Repository
public interface TradeLoanArrangementJpaRepository extends PersistentRepository<TradeLoanArrangementEntity> {

    boolean existsByCode(@NonNull String code);

    Window<TradeLoanArrangementEntity> findAllBy(ScrollPosition position, Limit limit, Sort sort);
}
