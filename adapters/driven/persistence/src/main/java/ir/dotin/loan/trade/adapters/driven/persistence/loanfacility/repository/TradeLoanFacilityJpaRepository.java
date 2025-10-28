package ir.dotin.loan.trade.adapters.driven.persistence.loanfacility.repository;

import org.springframework.data.domain.Limit;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Window;
import org.springframework.stereotype.Repository;

import ir.dotin.platform.adapter.persistence.repository.PersistentRepository;
import ir.dotin.loan.trade.adapters.driven.persistence.loanfacility.entity.TradeLoanFacilityEntity;

@Repository
public interface TradeLoanFacilityJpaRepository extends PersistentRepository<TradeLoanFacilityEntity> {

    Window<TradeLoanFacilityEntity> findAllBy(ScrollPosition position, Limit limit, Sort sort);
}
