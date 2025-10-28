package ir.dotin.loan.trade.core.application.query.loanarrangement.repository;

import java.util.Optional;
import java.util.UUID;

import ir.dotin.loan.trade.core.application.query.loanarrangement.dto.TradeLoanArrangementQueryDto;

public interface TradeLoanArrangementQueryRepository {

    Optional<TradeLoanArrangementQueryDto> findById(UUID id);
}
