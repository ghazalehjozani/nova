package ir.dotin.loan.trade.adapters.driven.persistance.installmentschedule.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ir.dotin.loan.trade.adapters.driven.persistance.installmentschedule.entity.InstallmentEntity;

@Repository
public interface InstallmentJpaRepository extends JpaRepository<InstallmentEntity, UUID> {

    Optional<InstallmentEntity> findByInstallmentNumberInstallmentNumber(String installmentNumber);

    List<InstallmentEntity> findByInstallmentScheduleId(UUID installmentScheduleId);

    @Query(
            "SELECT i FROM InstallmentEntity i WHERE i.installmentScheduleId = :scheduleId ORDER BY i.installmentNumber.sequenceNumber ASC")
    List<InstallmentEntity> findByInstallmentScheduleIdOrderBySequence(@Param("scheduleId") UUID scheduleId);

    @Query(
            "SELECT i FROM InstallmentEntity i WHERE i.installmentScheduleId = :scheduleId AND i.status.isPaid = false ORDER BY i.dates.dueDate ASC")
    List<InstallmentEntity> findUnpaidInstallmentsByScheduleId(@Param("scheduleId") UUID scheduleId);

    @Query(
            "SELECT i FROM InstallmentEntity i WHERE i.installmentScheduleId = :scheduleId AND i.status.isOverdue = true ORDER BY i.dates.dueDate ASC")
    List<InstallmentEntity> findOverdueInstallmentsByScheduleId(@Param("scheduleId") UUID scheduleId);

    @Query(
            "SELECT i FROM InstallmentEntity i WHERE i.dates.dueDate <= :date AND i.status.isPaid = false AND i.installmentSchedule.isActive = true")
    List<InstallmentEntity> findDueInstallments(@Param("date") LocalDate date);

    @Query(
            "SELECT i FROM InstallmentEntity i WHERE i.dates.dueDate BETWEEN :startDate AND :endDate AND i.status.isPaid = false")
    List<InstallmentEntity> findInstallmentsDueBetweenDates(
            @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query(
            "SELECT i FROM InstallmentEntity i WHERE i.loanFacilityId.value = :loanFacilityId ORDER BY i.dates.dueDate ASC")
    Page<InstallmentEntity> findByLoanFacilityIdPaginated(
            @Param("loanFacilityId") UUID loanFacilityId, Pageable pageable);

    @Query("SELECT i FROM InstallmentEntity i WHERE i.status.paymentStatus = :paymentStatus")
    List<InstallmentEntity> findByPaymentStatus(@Param("paymentStatus") String paymentStatus);

    @Query(
            "SELECT i FROM InstallmentEntity i WHERE i.installmentScheduleId = :scheduleId AND i.dates.dueDate = :dueDate")
    Optional<InstallmentEntity> findByScheduleIdAndDueDate(
            @Param("scheduleId") UUID scheduleId, @Param("dueDate") LocalDate dueDate);

    boolean existsByInstallmentNumberInstallmentNumber(String installmentNumber);

    long countByInstallmentScheduleIdAndStatusIsPaid(UUID installmentScheduleId, Boolean isPaid);

    @Query(
            "SELECT COUNT(i) FROM InstallmentEntity i WHERE i.installmentScheduleId = :scheduleId AND i.status.isOverdue = true")
    long countOverdueInstallmentsByScheduleId(@Param("scheduleId") UUID scheduleId);
}
