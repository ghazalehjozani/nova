package ir.dotin.loan.trade.core.application.ports.driven.repository;

import java.util.Optional;

import ir.dotin.platform.commons.core.Result;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTypeId;
import ir.dotin.loan.trade.core.domain.loantype.entity.TradeLoanType;

public interface TradeLoanTypeRepository {

    TradeLoanType save(TradeLoanType loanType);

    Optional<TradeLoanType> findById(LoanTypeId id);

    Result<Boolean> existsById(LoanTypeId id);
}
