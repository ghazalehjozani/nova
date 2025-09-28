package ir.dotin.loan.trade.core.application.ports.driven.repository;

import java.util.Optional;

import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;

public interface TradeLoanFacilityRepository {

    TradeLoanFacility save(TradeLoanFacility facility);

    Optional<TradeLoanFacility> findById(LoanFacilityId id);
}
