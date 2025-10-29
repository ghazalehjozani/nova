package ir.dotin.loan.trade.core.application.ports.outbound.command.repository;

import java.util.Optional;
import jakarta.validation.constraints.NotNull;

import ir.dotin.platform.commons.core.Result;
import ir.dotin.loan.baseloan.core.domain.shared.vo.BranchCode;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTypeId;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;

public interface TradeLoanFacilityRepository {

    TradeLoanFacility save(TradeLoanFacility facility);

    Optional<TradeLoanFacility> findById(LoanFacilityId id);

    Result<Boolean> existsById(@NotNull LoanFacilityId id);

    long countByBranchCodeAndLoanTypeIdAndCustomerNumber(
            BranchCode branchCode, LoanTypeId loanTypeId, String customerNumber);
}
