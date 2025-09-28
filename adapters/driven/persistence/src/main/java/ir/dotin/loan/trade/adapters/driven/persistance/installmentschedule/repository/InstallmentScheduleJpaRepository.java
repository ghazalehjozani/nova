package ir.dotin.loan.trade.adapters.driven.persistance.installmentschedule.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ir.dotin.loan.trade.adapters.driven.persistance.installmentschedule.entity.InstallmentScheduleEntity;

@Repository
public interface InstallmentScheduleJpaRepository extends JpaRepository<InstallmentScheduleEntity, UUID> {

    Optional<InstallmentScheduleEntity> findByScheduleNumber(String scheduleNumber);

    List<InstallmentScheduleEntity> findByLoanFacilityIdValue(UUID loanFacilityId);

    @Query(
            "SELECT s FROM InstallmentScheduleEntity s WHERE s.loanFacilityId.value = :loanFacilityId AND s.isActive = true")
    Optional<InstallmentScheduleEntity> findActiveByLoanFacilityId(@Param("loanFacilityId") UUID loanFacilityId);

    @Query("SELECT s FROM InstallmentScheduleEntity s WHERE s.scheduleStatus = :status")
    List<InstallmentScheduleEntity> findByStatus(@Param("status") String status);

    @Query(
            "SELECT s FROM InstallmentScheduleEntity s WHERE s.loanFacilityId.value = :loanFacilityId ORDER BY s.createdAt DESC")
    Page<InstallmentScheduleEntity> findByLoanFacilityIdPaginated(
            @Param("loanFacilityId") UUID loanFacilityId, Pageable pageable);

    @Query("SELECT s FROM InstallmentScheduleEntity s WHERE s.isCompleted = false AND s.isActive = true")
    List<InstallmentScheduleEntity> findAllActiveSchedules();

    @Query(
            "SELECT s FROM InstallmentScheduleEntity s WHERE s.firstDueDate <= :date AND s.isCompleted = false AND s.isActive = true")
    List<InstallmentScheduleEntity> findSchedulesWithDueInstallments(@Param("date") java.time.LocalDate date);

    boolean existsByScheduleNumber(String scheduleNumber);

    boolean existsByLoanFacilityIdValue(UUID loanFacilityId);
}
