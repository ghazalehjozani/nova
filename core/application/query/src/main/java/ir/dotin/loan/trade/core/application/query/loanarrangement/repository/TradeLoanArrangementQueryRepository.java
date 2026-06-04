package ir.dotin.loan.trade.core.application.query.loanarrangement.repository;

import java.util.Optional;
import java.util.UUID;

import ir.dotin.loan.trade.core.application.query.loanarrangement.dto.TradeLoanArrangementQueryDto;
import ir.dotin.loan.trade.core.application.query.loanarrangement.request.LoanTypeArrangementFilterQuery;
import ir.dotin.loan.trade.core.application.query.shared.pagination.CursorPage;
import ir.dotin.loan.trade.core.application.query.shared.pagination.CursorPageRequest;
import ir.dotin.loan.trade.core.application.query.shared.pagination.OffsetPage;

public interface TradeLoanArrangementQueryRepository {

    Optional<TradeLoanArrangementQueryDto> findById(UUID id);

    Optional<TradeLoanArrangementQueryDto> findByCode(String code);

    CursorPage<TradeLoanArrangementQueryDto> findAll(CursorPageRequest pageRequest);

    OffsetPage<TradeLoanArrangementQueryDto> findByFilter(LoanTypeArrangementFilterQuery filter);
}
