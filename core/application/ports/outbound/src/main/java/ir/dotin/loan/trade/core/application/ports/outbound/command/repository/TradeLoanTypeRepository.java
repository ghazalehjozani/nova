package ir.dotin.loan.trade.core.application.ports.outbound.command.repository;

import java.util.Optional;

import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.LoanTypeCode;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTypeId;
import ir.dotin.loan.trade.core.domain.loantype.entity.TradeLoanType;

public interface TradeLoanTypeRepository {

    TradeLoanType save(TradeLoanType loanType);

    TradeLoanType save(TradeLoanType loanType, long expectedVersion);

    Optional<TradeLoanType> findById(LoanTypeId id);

    Optional<TradeLoanType> findByCode(LoanTypeCode code);

    Boolean existsById(LoanTypeId id);

    Boolean existsByCode(LoanTypeCode code);

    Optional<String> getLoanTypeCode(LoanTypeId id);
}
