package ir.dotin.loan.trade.adapters.driven.persistence.loantypegroup.repository;

import org.springframework.stereotype.Repository;

import ir.dotin.platform.pangaea.persistence.jpa.repository.PersistentRepository;
import ir.dotin.loan.trade.adapters.driven.persistence.loantypegroup.entity.LoanTypeGroupEntity;

@Repository
public interface LoanTypeGroupJpaRepository extends PersistentRepository<LoanTypeGroupEntity> {

    boolean existsByCode(String code);
}
