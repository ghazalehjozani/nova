package ir.dotin.loan.trade.adapters.driven.persistance.loanfacility;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.FacilityStatus;
import ir.dotin.loan.trade.adapters.driven.persistance.loanfacility.mapper.TradeLoanFacilityPersistenceMapper;
import ir.dotin.loan.trade.adapters.driven.persistance.loanfacility.repository.TradeLoanFacilityJpaRepository;
import ir.dotin.loan.trade.core.application.ports.driven.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;

import static java.util.Objects.requireNonNull;

@Repository
@Transactional(readOnly = true)
public class TradeLoanFacilityRepositoryAdapter implements TradeLoanFacilityRepository {

    private final TradeLoanFacilityJpaRepository jpaRepository;
    private final TradeLoanFacilityPersistenceMapper mapper;

    public TradeLoanFacilityRepositoryAdapter(
            TradeLoanFacilityJpaRepository jpaRepository, TradeLoanFacilityPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public TradeLoanFacility save(TradeLoanFacility facility) {
        requireNonNull(facility, "TradeLoanFacility cannot be null");
        var entity = mapper.map(facility);
        var saved = jpaRepository.save(entity);
        return mapper.map(saved);
    }

    @Override
    public Optional<TradeLoanFacility> findById(UUID id) {
        requireNonNull(id, "ID cannot be null");
        return jpaRepository.findById(id).map(mapper::map);
    }

    @Override
    public Optional<TradeLoanFacility> findByCustomerId(String customerId) {
        requireNonNull(customerId, "Customer ID cannot be null");
        return jpaRepository.findByCustomerId(customerId).map(mapper::map);
    }

    @Override
    public List<TradeLoanFacility> findByLoanArrangementId(UUID loanArrangementId) {
        requireNonNull(loanArrangementId, "Loan Arrangement ID cannot be null");
        return jpaRepository.findByLoanArrangementId(loanArrangementId).stream()
                .map(mapper::map)
                .toList();
    }

    @Override
    public List<TradeLoanFacility> findByCurrentState(FacilityStatus status) {
        requireNonNull(status, "Facility status cannot be null");
        return jpaRepository.findByCurrentState(status).stream()
                .map(mapper::map)
                .toList();
    }

    @Override
    public List<TradeLoanFacility> findByCustomerIdAndStatus(String customerId, FacilityStatus status) {
        requireNonNull(customerId, "Customer ID cannot be null");
        requireNonNull(status, "Facility status cannot be null");
        return jpaRepository.findByCustomerIdAndStatus(customerId, status).stream()
                .map(mapper::map)
                .toList();
    }

    @Override
    public List<TradeLoanFacility> findByCustomerIdAndStatusIn(String customerId, List<FacilityStatus> statuses) {
        requireNonNull(customerId, "Customer ID cannot be null");
        requireNonNull(statuses, "Statuses cannot be null");
        return jpaRepository.findByCustomerIdAndStatusIn(customerId, statuses).stream()
                .map(mapper::map)
                .toList();
    }

    @Override
    public List<TradeLoanFacility> findByArrangementIdAndStatusIn(UUID arrangementId, List<FacilityStatus> statuses) {
        requireNonNull(arrangementId, "Arrangement ID cannot be null");
        requireNonNull(statuses, "Statuses cannot be null");
        return jpaRepository.findByArrangementIdAndStatusIn(arrangementId, statuses).stream()
                .map(mapper::map)
                .toList();
    }

    @Override
    public List<TradeLoanFacility> findByStatusIn(List<FacilityStatus> statuses) {
        requireNonNull(statuses, "Statuses cannot be null");
        return jpaRepository.findByStatusIn(statuses).stream().map(mapper::map).toList();
    }

    @Override
    public long countByCustomerIdAndStatus(String customerId, FacilityStatus status) {
        requireNonNull(customerId, "Customer ID cannot be null");
        requireNonNull(status, "Facility status cannot be null");
        return jpaRepository.countByCustomerIdAndStatus(customerId, status);
    }

    @Override
    public Optional<TradeLoanFacility> findBySanctionedLoanId(UUID sanctionedLoanId) {
        requireNonNull(sanctionedLoanId, "Sanctioned Loan ID cannot be null");
        return jpaRepository.findBySanctionedLoanId(sanctionedLoanId).map(mapper::map);
    }

    @Override
    public boolean existsByCustomerIdAndCurrentStateNotIn(String customerId, List<FacilityStatus> excludedStatuses) {
        requireNonNull(customerId, "Customer ID cannot be null");
        requireNonNull(excludedStatuses, "Excluded statuses cannot be null");
        return jpaRepository.existsByCustomerIdAndCurrentStateNotIn(customerId, excludedStatuses);
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        requireNonNull(id, "ID cannot be null");
        jpaRepository.deleteById(id);
    }
}
