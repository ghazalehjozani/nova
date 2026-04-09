package ir.dotin.loan.trade.core.application.ports.outbound.command.repository;

import java.util.Optional;
import jakarta.validation.constraints.NotNull;

import ir.dotin.platform.accounting.document.api.model.BranchCode;
import ir.dotin.platform.commons.core.Result;
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
}
