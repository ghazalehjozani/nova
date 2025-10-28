package ir.dotin.loan.trade.core.application.query.loantype.repository;

import java.util.Optional;
import java.util.UUID;

import ir.dotin.loan.trade.core.application.query.loantype.dto.TradeLoanTypeQueryDto;

public interface TradeLoanTypeQueryRepository {

    Optional<TradeLoanTypeQueryDto> findById(UUID id);
}
