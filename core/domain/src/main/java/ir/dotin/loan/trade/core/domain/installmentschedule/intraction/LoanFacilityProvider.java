package ir.dotin.loan.trade.core.domain.installmentschedule.intraction;

import org.jspecify.annotations.NonNull;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;

public interface LoanFacilityProvider {
    Result<TradeLoanFacility> findLoanFacilityById(@NonNull LoanFacilityId id);
}
