package ir.dotin.loan.trade.adapters.driven.persistance.loanfacility.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ir.dotin.platform.adapter.persistence.repository.PersistentRepository;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.FacilityStatus;
import ir.dotin.loan.trade.adapters.driven.persistance.loanfacility.entity.TradeLoanFacilityEntity;

@Repository
public interface TradeLoanFacilityJpaRepository extends PersistentRepository<TradeLoanFacilityEntity> {

    boolean existsByCustomerIdAndCurrentStateNotIn(String customerId, List<FacilityStatus> excludedStatuses);

    Optional<TradeLoanFacilityEntity> findByCustomerId(String customerId);

    List<TradeLoanFacilityEntity> findByLoanArrangementId(UUID loanArrangementId);

    List<TradeLoanFacilityEntity> findByCurrentState(FacilityStatus status);

    @Query("SELECT t FROM TradeLoanFacilityEntity t WHERE t.customerId = :customerId AND t.currentState = :status")
    List<TradeLoanFacilityEntity> findByCustomerIdAndStatus(
            @Param("customerId") String customerId, @Param("status") FacilityStatus status);

    @Query("SELECT t FROM TradeLoanFacilityEntity t WHERE t.customerId = :customerId AND t.currentState IN :statuses")
    List<TradeLoanFacilityEntity> findByCustomerIdAndStatusIn(
            @Param("customerId") String customerId, @Param("statuses") List<FacilityStatus> statuses);

    @Query(
            "SELECT t FROM TradeLoanFacilityEntity t WHERE t.loanArrangementId = :arrangementId AND t.currentState IN :statuses")
    List<TradeLoanFacilityEntity> findByArrangementIdAndStatusIn(
            @Param("arrangementId") UUID arrangementId, @Param("statuses") List<FacilityStatus> statuses);

    @Query("SELECT t FROM TradeLoanFacilityEntity t WHERE t.currentState IN :statuses")
    List<TradeLoanFacilityEntity> findByStatusIn(@Param("statuses") List<FacilityStatus> statuses);

    @Query(
            "SELECT COUNT(t) FROM TradeLoanFacilityEntity t WHERE t.customerId = :customerId AND t.currentState = :status")
    long countByCustomerIdAndStatus(@Param("customerId") String customerId, @Param("status") FacilityStatus status);

    @Query("SELECT t FROM TradeLoanFacilityEntity t WHERE t.sanctionedLoanId = :sanctionedLoanId")
    Optional<TradeLoanFacilityEntity> findBySanctionedLoanId(@Param("sanctionedLoanId") UUID sanctionedLoanId);
}
