package ir.dotin.loan.trade.adapters.driven.persistence.loantype.repository;

import org.springframework.data.domain.Limit;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Window;
import org.springframework.stereotype.Repository;

import ir.dotin.platform.adapter.persistence.repository.PersistentRepository;
import ir.dotin.loan.trade.adapters.driven.persistence.loantype.entity.TradeLoanTypeEntity;

@Repository
public interface TradeLoanTypeJpaRepository extends PersistentRepository<TradeLoanTypeEntity> {
    boolean existsByCode_Value(String value);

    Window<TradeLoanTypeEntity> findAllBy(ScrollPosition position, Limit limit, Sort sort);
}
