package ir.dotin.loan.trade.core.application.query.loantype.repository;

import java.util.Optional;
import java.util.UUID;

import ir.dotin.loan.trade.core.application.query.loantype.dto.TradeLoanTypeQueryDto;
import ir.dotin.loan.trade.core.application.query.loantype.request.LoanTypeFilterQuery;
import ir.dotin.loan.trade.core.application.query.shared.pagination.CursorPage;
import ir.dotin.loan.trade.core.application.query.shared.pagination.CursorPageRequest;
import ir.dotin.loan.trade.core.application.query.shared.pagination.OffsetPage;

public interface TradeLoanTypeQueryRepository {

    Optional<TradeLoanTypeQueryDto> findById(UUID id);

    CursorPage<TradeLoanTypeQueryDto> findAll(CursorPageRequest pageRequest);

    OffsetPage<TradeLoanTypeQueryDto> findByFilter(LoanTypeFilterQuery filter);
}
