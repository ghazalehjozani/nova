package ir.dotin.loan.trade.core.application.ports.driven.repository;

import java.util.List;
import java.util.Optional;

import ir.dotin.loan.baseloan.core.domain.installmentschedule.entity.InstallmentSchedule;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.enums.InstallmentScheduleStatus;
import ir.dotin.loan.baseloan.core.domain.shared.vo.InstallmentScheduleId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;

/**
 * Repository interface for managing InstallmentSchedule entities. This interface defines the contract for persistence
 * operations on installment schedules.
 */
public interface InstallmentScheduleRepository {

    /**
     * Save an installment schedule.
     *
     * @param installmentSchedule the installment schedule to save
     * @return the saved installment schedule
     */
    InstallmentSchedule save(InstallmentSchedule installmentSchedule);

    /**
     * Find an installment schedule by its ID.
     *
     * @param id the installment schedule ID
     * @return an Optional containing the installment schedule if found, empty otherwise
     */
    Optional<InstallmentSchedule> findById(InstallmentScheduleId id);

    /**
     * Find an installment schedule by its schedule number.
     *
     * @param scheduleNumber the schedule number
     * @return an Optional containing the installment schedule if found, empty otherwise
     */
    Optional<InstallmentSchedule> findByScheduleNumber(String scheduleNumber);

    /**
     * Find all installment schedules associated with a specific loan facility.
     *
     * @param loanFacilityId the loan facility ID
     * @return list of installment schedules for the specified loan facility
     */
    List<InstallmentSchedule> findByLoanFacilityId(LoanFacilityId loanFacilityId);

    /**
     * Find the active installment schedule for a specific loan facility.
     *
     * @param loanFacilityId the loan facility ID
     * @return an Optional containing the active installment schedule if found, empty otherwise
     */
    Optional<InstallmentSchedule> findActiveByLoanFacilityId(LoanFacilityId loanFacilityId);

    /**
     * Find all installment schedules with a specific status.
     *
     * @param status the installment schedule status
     * @return list of installment schedules with the specified status
     */
    List<InstallmentSchedule> findByStatus(InstallmentScheduleStatus status);

    /**
     * Find all active installment schedules.
     *
     * @return list of all active installment schedules
     */
    List<InstallmentSchedule> findAllActiveSchedules();

    /**
     * Delete an installment schedule by its ID.
     *
     * @param id the installment schedule ID to delete
     */
    void delete(InstallmentScheduleId id);

    /**
     * Check if an installment schedule with the given schedule number exists.
     *
     * @param scheduleNumber the schedule number to check
     * @return true if an installment schedule with the given schedule number exists, false otherwise
     */
    boolean existsByScheduleNumber(String scheduleNumber);

    /**
     * Check if any installment schedule exists for the given loan facility.
     *
     * @param loanFacilityId the loan facility ID to check
     * @return true if any installment schedule exists for the loan facility, false otherwise
     */
    boolean existsByLoanFacilityId(LoanFacilityId loanFacilityId);
}
