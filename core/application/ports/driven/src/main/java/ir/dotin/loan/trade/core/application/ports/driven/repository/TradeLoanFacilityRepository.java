package ir.dotin.loan.trade.core.application.ports.driven.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.FacilityStatus;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;

public interface TradeLoanFacilityRepository {

    TradeLoanFacility save(TradeLoanFacility facility);

    Optional<TradeLoanFacility> findById(UUID id);

    Optional<TradeLoanFacility> findByCustomerId(String customerId);

    List<TradeLoanFacility> findByLoanArrangementId(UUID loanArrangementId);

    List<TradeLoanFacility> findByCurrentState(FacilityStatus status);

    List<TradeLoanFacility> findByCustomerIdAndStatus(String customerId, FacilityStatus status);

    List<TradeLoanFacility> findByCustomerIdAndStatusIn(String customerId, List<FacilityStatus> statuses);

    List<TradeLoanFacility> findByArrangementIdAndStatusIn(UUID arrangementId, List<FacilityStatus> statuses);

    List<TradeLoanFacility> findByStatusIn(List<FacilityStatus> statuses);

    long countByCustomerIdAndStatus(String customerId, FacilityStatus status);

    Optional<TradeLoanFacility> findBySanctionedLoanId(UUID sanctionedLoanId);

    boolean existsByCustomerIdAndCurrentStateNotIn(String customerId, List<FacilityStatus> excludedStatuses);

    void deleteById(UUID id);
}
