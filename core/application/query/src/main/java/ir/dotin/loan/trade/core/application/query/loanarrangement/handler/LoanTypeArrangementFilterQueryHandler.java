package ir.dotin.loan.trade.core.application.query.loanarrangement.handler;

import org.springframework.stereotype.Component;

import ir.dotin.platform.dispatcher.api.query.QueryHandler;
import ir.dotin.loan.trade.core.application.query.loanarrangement.dto.LoanArrangementQueryResult;
import ir.dotin.loan.trade.core.application.query.loanarrangement.dto.TradeLoanArrangementQueryDto;
import ir.dotin.loan.trade.core.application.query.loanarrangement.repository.TradeLoanArrangementQueryRepository;
import ir.dotin.loan.trade.core.application.query.loanarrangement.request.LoanTypeArrangementFilterQuery;
import ir.dotin.loan.trade.core.application.query.shared.pagination.OffsetPage;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LoanTypeArrangementFilterQueryHandler
        implements QueryHandler<LoanTypeArrangementFilterQuery, LoanArrangementQueryResult> {

    private final TradeLoanArrangementQueryRepository queryRepository;

    @Override
    public LoanArrangementQueryResult handle(LoanTypeArrangementFilterQuery query) {
        OffsetPage<TradeLoanArrangementQueryDto> page = queryRepository.findByFilter(query);

        return LoanArrangementQueryResult.forOffset(
                page.content(),
                page.currentPage(),
                page.pageSize(),
                page.totalElements(),
                page.totalPages(),
                page.hasNext(),
                page.hasPrevious());
    }
}
