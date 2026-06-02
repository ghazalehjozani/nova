package ir.dotin.loan.trade.adapters.driven.persistence.loanfacility;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import ir.dotin.platform.accounting.document.api.model.BranchCode;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.FacilityStatus;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.ApplicationNumber;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.LoanTypeCode;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.trade.adapters.driven.persistence.loanfacility.entity.TradeLoanFacilityEntity;
import ir.dotin.loan.trade.adapters.driven.persistence.loanfacility.mapper.TradeLoanFacilityPersistenceMapper;
import ir.dotin.loan.trade.adapters.driven.persistence.loanfacility.repository.TradeLoanFacilityJpaRepository;
import ir.dotin.loan.trade.adapters.driven.persistence.loantype.projection.TradeLoanTypeIdProjection;
import ir.dotin.loan.trade.adapters.driven.persistence.loantype.repository.TradeLoanTypeJpaRepository;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;

import lombok.RequiredArgsConstructor;

import static java.util.Objects.requireNonNull;

@Repository
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TradeLoanFacilityRepositoryAdapter implements TradeLoanFacilityRepository {

    private static final Set<FacilityStatus> TERMINAL_STATES = EnumSet.copyOf(Arrays.stream(FacilityStatus.values())
            .filter(FacilityStatus::isTerminal)
            .collect(Collectors.toSet()));

    private final TradeLoanFacilityJpaRepository jpaRepository;
    private final TradeLoanTypeJpaRepository tradeLoanTypeJpaRepository;
    private final TradeLoanFacilityPersistenceMapper mapper;

    @Override
    @Transactional
    public TradeLoanFacility save(TradeLoanFacility facility) {
        requireNonNull(facility, "TradeLoanFacility cannot be null");
        var entity = mapper.map(facility);
        var saved = jpaRepository.save(entity);
        return mapper.map(saved);
    }

    @Override
    @Transactional
    public TradeLoanFacility save(TradeLoanFacility facility, long expectedVersion) {
        requireNonNull(facility, "TradeLoanFacility cannot be null");
        UUID id = facility.getId().value();

        // Fast pre-check against the currently-persisted row so a stale write is rejected before the merge. The merge
        // below still carries expectedVersion onto the detached entity, so Hibernate performs the authoritative
        // optimistic-lock check and closes the race between this read and the flush.
        TradeLoanFacilityEntity managed = jpaRepository
                .findById(id)
                .orElseThrow(() -> new OptimisticLockingFailureException(
                        "TradeLoanFacility not found for optimistic save: " + id));
        Long currentVersion = managed.getVersion();
        if (currentVersion == null || currentVersion != expectedVersion) {
            // OptimisticLockingFailureException (and Hibernate's ObjectOptimisticLockingFailureException subclass thrown
            // by the merge below) is mapped to ErrorCode.CONFLICT (HTTP 409) by the platform
            // JpaExceptionMapperContributor — i.e. the domain CONFLICT FailureCause.
            throw new OptimisticLockingFailureException("Optimistic lock conflict on TradeLoanFacility " + id
                    + ": expected version " + expectedVersion + " but found " + currentVersion);
        }

        var entity = mapper.map(facility);
        entity.setVersion(expectedVersion);
        var saved = jpaRepository.save(entity);
        return mapper.map(saved);
    }

    @Override
    public Optional<TradeLoanFacility> findById(LoanFacilityId id) {
        requireNonNull(id, "ID cannot be null");
        return jpaRepository.findById(id.value()).map(mapper::map);
    }

    @Override
    public Result<Boolean> existsById(LoanFacilityId id) {
        return Result.success(jpaRepository.existsById(id.value()));
    }

    @Override
    public long countByBranchCodeAndLoanTypeCodeAndCustomerNumber(
            BranchCode branchCode, LoanTypeCode loanTypeCode, String customerNumber) {
        Optional<TradeLoanTypeIdProjection> byCodeValue =
                tradeLoanTypeJpaRepository.findByCode_Value(loanTypeCode.value());
        UUID loanTypeId = byCodeValue.map(TradeLoanTypeIdProjection::getId).orElse(null);
        return jpaRepository.countByBranchCodeAndLoanTypeIdAndCustomerNumber(
                branchCode.value(), loanTypeId, customerNumber);
    }

    @Override
    public boolean existsByApplicationNumber(ApplicationNumber applicationNumber) {
        return jpaRepository.existsByApplicationNumber(
                applicationNumber.branch().code().value(),
                applicationNumber.loanTypeCode().value(),
                applicationNumber.party().customerNumber(),
                applicationNumber.derivedValue());
    }

    @Override
    public boolean existsActiveByApplicationNumber(ApplicationNumber applicationNumber) {
        requireNonNull(applicationNumber, "ApplicationNumber cannot be null");
        return jpaRepository.existsActiveByApplicationNumber(
                applicationNumber.branch().code().value(),
                applicationNumber.loanTypeCode().value(),
                applicationNumber.party().customerNumber(),
                applicationNumber.derivedValue(),
                TERMINAL_STATES);
    }

    @Override
    public Optional<TradeLoanFacility> findByApplicationNumber(String applicationNumber) {
        requireNonNull(applicationNumber, "ApplicationNumber cannot be null");
        return jpaRepository.findByApplicationNumber(applicationNumber).map(mapper::map);
    }
}
