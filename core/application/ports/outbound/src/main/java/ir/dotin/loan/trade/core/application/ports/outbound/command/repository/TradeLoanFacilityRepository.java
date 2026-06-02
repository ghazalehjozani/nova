package ir.dotin.loan.trade.core.application.ports.outbound.command.repository;

import java.util.Optional;
import jakarta.validation.constraints.NotNull;

import ir.dotin.platform.accounting.document.api.model.BranchCode;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.ApplicationNumber;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.LoanTypeCode;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;

public interface TradeLoanFacilityRepository {

    TradeLoanFacility save(TradeLoanFacility facility);

    /**
     * Persists an already-existing facility under optimistic-lock control. {@code expectedVersion} is the JPA
     * {@code @Version} the caller observed when it read the aggregate (carried on the mutating command). The
     * implementation rejects the write when the persisted version has moved on, surfacing a CONFLICT (HTTP 409).
     * Use only for state-changing commands; creation goes through {@link #save(TradeLoanFacility)}.
     */
    TradeLoanFacility save(TradeLoanFacility facility, long expectedVersion);

    Optional<TradeLoanFacility> findById(LoanFacilityId id);

    Result<Boolean> existsById(@NotNull LoanFacilityId id);

    long countByBranchCodeAndLoanTypeCodeAndCustomerNumber(
            BranchCode branchCode, LoanTypeCode loanTypeCode, String customerNumber);

    boolean existsByApplicationNumber(ApplicationNumber applicationNumber);

    /**
     * Whether a facility with this application number exists in a non-terminal (active) state. Used to tell a true
     * duplicate (active facility + live FCB loan file) apart from a ghost/stale allocation (facility reverted to a
     * terminal state, FCB loan file never materialised or already cancelled).
     */
    boolean existsActiveByApplicationNumber(ApplicationNumber applicationNumber);

    Optional<TradeLoanFacility> findByApplicationNumber(String applicationNumber);
}
