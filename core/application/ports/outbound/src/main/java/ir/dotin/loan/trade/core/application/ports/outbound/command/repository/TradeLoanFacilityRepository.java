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
