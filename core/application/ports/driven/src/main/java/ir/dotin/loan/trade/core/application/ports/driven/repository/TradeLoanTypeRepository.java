package ir.dotin.loan.trade.core.application.ports.driven.repository;

import java.util.Optional;

import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.LoanTypeCode;
import ir.dotin.loan.trade.core.domain.loantype.entity.TradeLoanType;

public interface TradeLoanTypeRepository {

    TradeLoanType save(TradeLoanType loanType);

    Optional<TradeLoanType> findByCode(LoanTypeCode code);
}
