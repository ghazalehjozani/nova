package ir.dotin.loan.trade.adapters.driven.persistence.installmentschedule.query;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ir.dotin.loan.trade.adapters.driven.persistence.installmentschedule.entity.InstallmentEntity;

/** Spring Data JPA repository for installment queries */
@Repository
public interface SpringDataInstallmentQueryRepository extends JpaRepository<InstallmentEntity, UUID> {

    /** Find installments by facility ID and due date before specified date */
    @Query("""
        SELECT i FROM InstallmentEntity i
        WHERE i.installmentSchedule.loanFacilityId = :facilityId
        AND i.dueDate < :dueDate
        ORDER BY i.dueDate ASC
        """)
    List<InstallmentEntity> findByFacilityIdAndDueDateBeforeOrderByDueDateAsc(
            @Param("facilityId") UUID facilityId, @Param("dueDate") LocalDate dueDate);

    /** Find installments by facility ID */
    @Query("""
        SELECT i FROM InstallmentEntity i
        WHERE i.installmentSchedule.loanFacilityId = :facilityId
        ORDER BY i.dueDate ASC
        """)
    List<InstallmentEntity> findByFacilityIdOrderByDueDateAsc(@Param("facilityId") UUID facilityId);

    /** Find unpaid installments by facility ID */
    @Query("""
        SELECT i FROM InstallmentEntity i
        WHERE i.installmentSchedule.loanFacilityId = :facilityId
        AND i.status != :status
        ORDER BY i.dueDate ASC
        """)
    List<InstallmentEntity> findByFacilityIdAndStatusNotOrderByDueDateAsc(
            @Param("facilityId") UUID facilityId, @Param("status") String status);

    /** Find overdue installments */
    @Query("""
        SELECT i FROM InstallmentEntity i
        WHERE i.dueDate < :asOfDate
        AND i.status != 'PAID'
        AND i.installmentSchedule.loanFacilityId = :facilityId
        ORDER BY i.dueDate ASC
        """)
    List<InstallmentEntity> findOverdueInstallments(
            @Param("facilityId") UUID facilityId, @Param("asOfDate") LocalDate asOfDate);

    /** Find next payment installment */
    @Query("""
        SELECT i FROM InstallmentEntity i
        WHERE i.installmentSchedule.loanFacilityId = :facilityId
        AND i.status != 'PAID'
        AND i.dueDate >= :currentDate
        ORDER BY i.dueDate ASC
        LIMIT 1
        """)
    InstallmentEntity findNextPaymentInstallment(
            @Param("facilityId") UUID facilityId, @Param("currentDate") LocalDate currentDate);

    /** Count installments by status and facility */
    @Query("""
        SELECT COUNT(i) FROM InstallmentEntity i
        WHERE i.installmentSchedule.loanFacilityId = :facilityId
        AND i.status = :status
        """)
    long countByFacilityIdAndStatus(@Param("facilityId") UUID facilityId, @Param("status") String status);

    /** Count all installments by facility */
    @Query("""
        SELECT COUNT(i) FROM InstallmentEntity i
        WHERE i.installmentSchedule.loanFacilityId = :facilityId
        """)
    long countByFacilityId(@Param("facilityId") UUID facilityId);

    /** Calculate total outstanding amounts by facility */
    @Query("""
        SELECT
            COALESCE(SUM(i.scheduledAmount.principalAmount.amount), 0) as totalPrincipal,
            COALESCE(SUM(i.scheduledAmount.interestAmount.amount), 0) as totalInterest,
            COALESCE(SUM(i.paidAmount.amount), 0) as totalPaid
        FROM InstallmentEntity i
        WHERE i.installmentSchedule.loanFacilityId = :facilityId
        AND i.dueDate <= :asOfDate
        AND i.status != 'PAID'
        """)
    Object[] calculateOutstandingAmounts(@Param("facilityId") UUID facilityId, @Param("asOfDate") LocalDate asOfDate);

    /** Find installments due in date range for multiple facilities */
    @Query("""
        SELECT i FROM InstallmentEntity i
        WHERE i.installmentSchedule.loanFacilityId IN :facilityIds
        AND i.dueDate BETWEEN :fromDate AND :toDate
        AND i.status != 'PAID'
        ORDER BY i.dueDate ASC
        """)
    List<InstallmentEntity> findInstallmentsDueInDateRange(
            @Param("facilityIds") List<UUID> facilityIds,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate);
}
