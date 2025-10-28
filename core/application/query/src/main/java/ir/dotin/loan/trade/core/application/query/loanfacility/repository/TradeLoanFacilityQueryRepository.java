package ir.dotin.loan.trade.core.application.query.loanfacility.repository;

import java.util.Optional;
import java.util.UUID;

import ir.dotin.loan.trade.core.application.query.loanfacility.dto.TradeFacilityQueryDto;
import ir.dotin.loan.trade.core.application.query.loanfacility.request.LoanFacilityFilterQuery;
import ir.dotin.loan.trade.core.application.query.shared.pagination.CursorPage;
import ir.dotin.loan.trade.core.application.query.shared.pagination.CursorPageRequest;
import ir.dotin.loan.trade.core.application.query.shared.pagination.OffsetPage;

public interface TradeLoanFacilityQueryRepository {

    Optional<TradeFacilityQueryDto> findById(UUID id);

    CursorPage<TradeFacilityQueryDto> findAll(CursorPageRequest pageRequest);

    OffsetPage<TradeFacilityQueryDto> findByFilter(LoanFacilityFilterQuery filter);
}
