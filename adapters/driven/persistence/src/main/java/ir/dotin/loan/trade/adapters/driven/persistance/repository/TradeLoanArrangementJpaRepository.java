package ir.dotin.loan.trade.adapters.driven.persistance.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ir.dotin.loan.trade.adapters.driven.persistance.entity.TradeLoanArrangementEntity;

@Repository
public interface TradeLoanArrangementJpaRepository
        extends JpaRepository<TradeLoanArrangementEntity, UUID>, JpaSpecificationExecutor<TradeLoanArrangementEntity> {

    boolean existsByCode(String code);

    Optional<TradeLoanArrangementEntity> findByCode(String code);

    @Query("SELECT t FROM TradeLoanArrangementEntity t WHERE t.active = true AND t.disabled = false")
    List<TradeLoanArrangementEntity> findAllActive();

    @Query("SELECT t FROM TradeLoanArrangementEntity t WHERE t.code = :code AND t.active = true")
    Optional<TradeLoanArrangementEntity> findActiveByCode(@Param("code") String code);
}
