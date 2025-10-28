package ir.dotin.loan.trade.adapters.driven.persistence.loanfacility.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Window;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ir.dotin.platform.adapter.persistence.repository.PersistentRepository;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.FacilityStatus;
import ir.dotin.loan.trade.adapters.driven.persistence.loanfacility.entity.TradeLoanFacilityEntity;

@Repository
public interface TradeLoanFacilityJpaRepository extends PersistentRepository<TradeLoanFacilityEntity> {

    Window<TradeLoanFacilityEntity> findAllBy(ScrollPosition position, Limit limit, Sort sort);

    @Query(
            """
            SELECT f FROM TradeLoanFacilityEntity f
            WHERE (:loanTypeId IS NULL OR f.loanTypeId = :loanTypeId)
            AND (:customerNumber IS NULL OR f.loanApplication.customer.customerNumber = :customerNumber)
            AND (:createDateFrom IS NULL OR f.createdAt >= :createDateFrom)
            AND (:createDateTo IS NULL OR f.createdAt <= :createDateTo)
            AND (:minAmount IS NULL OR f.loanApplication.requestedAmount.amount >= :minAmount)
            AND (:maxAmount IS NULL OR f.loanApplication.requestedAmount.amount <= :maxAmount)
            AND (:status IS NULL OR f.currentState = :status)
            """)
    Page<TradeLoanFacilityEntity> findByFilter(
            @Param("loanTypeId") UUID loanTypeId,
            @Param("customerNumber") String customerNumber,
            @Param("createDateFrom") LocalDateTime createDateFrom,
            @Param("createDateTo") LocalDateTime createDateTo,
            @Param("minAmount") BigDecimal requestAmountMin,
            @Param("maxAmount") BigDecimal requestAmountMax,
            @Param("status") FacilityStatus status,
            Pageable pageable);
}
