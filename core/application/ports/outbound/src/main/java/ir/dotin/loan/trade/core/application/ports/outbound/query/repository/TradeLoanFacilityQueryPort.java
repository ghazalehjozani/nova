package ir.dotin.loan.trade.core.application.ports.outbound.query.repository;

import java.util.Optional;
import java.util.UUID;

import ir.dotin.loan.trade.core.application.ports.outbound.query.request.TradeFacilityQueryDto;

public interface TradeLoanFacilityQueryPort {

    Optional<TradeFacilityQueryDto> findById(UUID id);
}
