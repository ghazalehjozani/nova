package ir.dotin.loan.trade.core.application.ports.outbound.query.repository;

import java.util.Optional;
import java.util.UUID;

import ir.dotin.loan.trade.core.application.ports.outbound.query.dto.TradeLoanArrangementQueryDto;

public interface TradeLoanArrangementQueryPort {

    Optional<TradeLoanArrangementQueryDto> findById(UUID id);
}
